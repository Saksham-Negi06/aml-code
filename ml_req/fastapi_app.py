"""FastAPI inference service for the trained AML ensemble.

Expected training artifacts (paths can be overridden with environment variables):

output/split_info.json
output/metrics_report.json
output/boosting_arm_metrics.json
models/bagging_arm_model.joblib
models/boosting_arm_model.json OR models/boosting_arm_model.joblib
models/meta_learner.joblib (only when a stacking method won)
splits/train.parquet (used as the LIME background dataset)

Run:
    uvicorn fastapi_app:app --host localhost --port 8500 --reload --env-file .env
"""

from __future__ import annotations

import asyncio
import json
import logging
import os
import time
import uuid
from contextlib import asynccontextmanager
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Literal

import joblib
import numpy as np
import pandas as pd
import shap
import xgboost as xgb
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from lime.lime_tabular import LimeTabularExplainer
from pydantic import BaseModel, ConfigDict, Field, field_validator

try:
    from google import genai
    from google.genai import types as genai_types
except ImportError:  # Gemini is optional.
    genai = None
    genai_types = None


LOG = logging.getLogger("aml-inference-api")
logging.basicConfig(
    level=os.getenv("LOG_LEVEL", "INFO"),
    format="%(asctime)s %(levelname)s %(name)s %(message)s",
)

BASE_DIR = Path(__file__).resolve().parent
load_dotenv(BASE_DIR / ".env", override=True)
OUTPUT_DIR = Path(os.getenv("AML_OUTPUT_DIR", BASE_DIR / "output"))
MODELS_DIR = Path(os.getenv("AML_MODELS_DIR", BASE_DIR / "models"))
SPLITS_DIR = Path(os.getenv("AML_SPLITS_DIR", BASE_DIR / "splits"))
GEMINI_MODEL = os.getenv("GEMINI_MODEL", "gemini-3.5-flash-lite")
MAX_EXPLANATION_FEATURES = int(os.getenv("MAX_EXPLANATION_FEATURES", "5"))
LIME_BACKGROUND_SIZE = int(os.getenv("LIME_BACKGROUND_SIZE", "2000"))


class AccountHistory(BaseModel):
    """Historical aggregates calculated from transactions before this transaction."""

    transaction_count: float = Field(0, ge=0)
    average_amount: float = Field(0, ge=0)
    amount_stddev: float = Field(0, ge=0)
    minimum_amount: float = Field(0, ge=0)
    maximum_amount: float = Field(0, ge=0)
    unique_counterparties: float = Field(0, ge=0)


class TransactionInput(BaseModel):
    """Raw transaction and point-in-time historical context.

    Account history is optional for cold-start accounts, but predictions are more
    reliable when it is populated from a feature store or transaction database.
    """

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "transaction_id": "TXN-8F91A2",
                "sender_account": "ACC-SENDER-001",
                "receiver_account": "ACC-RECEIVER-009",
                "amount": 84250.00,
                "payment_currency": "USD",
                "received_currency": "USD",
                "sender_bank_location": "IN",
                "receiver_bank_location": "Nigeria",
                "payment_type": "Cross-border",
                "transaction_datetime": "2026-08-18T10:30:00Z",
                "sender_history": {
                    "transaction_count": 42,
                    "average_amount": 5800,
                    "amount_stddev": 2100,
                    "minimum_amount": 120,
                    "maximum_amount": 15000,
                    "unique_counterparties": 9,
                },
                "receiver_history": {
                    "transaction_count": 18,
                    "average_amount": 4200,
                    "unique_counterparties": 6,
                },
            }
        }
    )

    transaction_id: str = Field(default_factory=lambda: str(uuid.uuid4()), max_length=100)
    sender_account: str = Field(..., min_length=1, max_length=255)
    receiver_account: str = Field(..., min_length=1, max_length=255)
    amount: float = Field(..., gt=0)
    payment_currency: str = Field(..., min_length=3, max_length=10)
    received_currency: str = Field(..., min_length=3, max_length=10)
    sender_bank_location: str = Field(..., min_length=2, max_length=100)
    receiver_bank_location: str = Field(..., min_length=2, max_length=100)
    payment_type: str = Field(..., min_length=1, max_length=100)
    transaction_datetime: datetime
    sender_history: AccountHistory | None = None
    receiver_history: AccountHistory | None = None
    use_gemini: bool = True

    @field_validator(
        "payment_currency",
        "received_currency",
        "sender_bank_location",
        "receiver_bank_location",
        "payment_type",
        mode="before",
    )
    @classmethod
    def normalize_categories(cls, value: Any) -> str:
        return str(value).strip().upper()

    @field_validator("transaction_datetime")
    @classmethod
    def require_timezone(cls, value: datetime) -> datetime:
        if value.tzinfo is None or value.utcoffset() is None:
            raise ValueError("transaction_datetime must include a timezone")
        return value.astimezone(timezone.utc)


class FeatureFactor(BaseModel):
    feature: str
    display_name: str
    feature_value: float
    impact: float
    direction: Literal["increases_risk", "decreases_risk", "neutral"]


class LimeFactor(BaseModel):
    condition: str
    display_condition: str
    weight: float
    direction: Literal["increases_risk", "decreases_risk", "neutral"]


class ExplanationOutput(BaseModel):
    scope: str
    shap_factors: list[FeatureFactor]
    lime_factors: list[LimeFactor]
    warnings: list[str] = Field(default_factory=list)


class RiskResponse(BaseModel):
    request_id: str
    transaction_id: str
    risk_score: float
    raw_model_output: float
    risk_category: Literal["LOW", "MEDIUM", "HIGH", "CRITICAL"]
    model_confidence: float
    should_flag: bool
    decision_threshold: float
    ensemble_method: str
    explanation: ExplanationOutput
    one_line_explanation: str
    recommendation: str
    explanation_source: Literal["gemini", "deterministic_fallback"]
    explanation_warning: str | None = None
    history_context: Literal["provided", "cold_start_defaults"]
    input_warnings: list[str]
    processing_time_ms: float


class GeminiOutput(BaseModel):
    one_line_explanation: str = Field(
        description="One factual sentence explaining the main model factors."
    )
    recommendation: str = Field(
        description="One concise compliance next step; do not allege criminal activity."
    )


@dataclass
class ModelArtifacts:
    feature_columns: list[str]
    label_encodings: dict[str, dict[str, int]]
    threshold: float
    ensemble_method: str
    boosting_winner: str
    bagging_model: Any | None
    boosting_model: Any
    meta_learner: Any | None
    shap_explainer: Any
    lime_explainer: LimeTabularExplainer


ARTIFACTS: ModelArtifacts | None = None
GEMINI_CLIENT: Any | None = None
ARTIFACT_LOAD_ERROR: str | None = None


def _load_json(path: Path) -> dict[str, Any]:
    if not path.exists():
        raise FileNotFoundError(f"Required artifact not found: {path}")
    with path.open("r", encoding="utf-8") as stream:
        return json.load(stream)


def load_artifacts() -> ModelArtifacts:
    split_info = _load_json(OUTPUT_DIR / "split_info.json")
    report = _load_json(OUTPUT_DIR / "metrics_report.json")
    boosting_metrics = _load_json(OUTPUT_DIR / "boosting_arm_metrics.json")

    feature_columns = split_info["feature_columns"]
    label_encodings = split_info["label_encodings"]
    pipeline = report["pipeline_summary"]
    threshold = float(pipeline["chosen_threshold"])
    ensemble_method = str(pipeline["ensemble_method"])
    boosting_winner = str(boosting_metrics["winner"])

    bagging_model = None
    if any(name in ensemble_method for name in ("Stacking", "Soft", "Bagging")):
        bagging_path = MODELS_DIR / "bagging_arm_model.joblib"
        if not bagging_path.exists():
            raise FileNotFoundError(f"Required artifact not found: {bagging_path}")
        bagging_model = joblib.load(bagging_path)

    if boosting_winner.startswith("XGBoost"):
        boosting_path = MODELS_DIR / "boosting_arm_model.json"
        if not boosting_path.exists():
            raise FileNotFoundError(f"Required artifact not found: {boosting_path}")
        boosting_model = xgb.Booster()
        boosting_model.load_model(boosting_path)
    else:
        boosting_path = MODELS_DIR / "boosting_arm_model.joblib"
        if not boosting_path.exists():
            raise FileNotFoundError(f"Required artifact not found: {boosting_path}")
        boosting_model = joblib.load(boosting_path)

    meta_learner = None
    if "Stacking" in ensemble_method:
        meta_path = MODELS_DIR / "meta_learner.joblib"
        if not meta_path.exists():
            raise FileNotFoundError(
                f"{ensemble_method} requires the missing artifact: {meta_path}"
            )
        meta_learner = joblib.load(meta_path)

    train_path = SPLITS_DIR / "train.parquet"
    if not train_path.exists():
        raise FileNotFoundError(
            f"LIME requires a training background dataset: {train_path}"
        )
    train_df = pd.read_parquet(train_path, columns=feature_columns)
    if len(train_df) > LIME_BACKGROUND_SIZE:
        train_df = train_df.sample(LIME_BACKGROUND_SIZE, random_state=42)
    background = train_df.to_numpy(dtype=np.float64)

    shap_explainer = shap.TreeExplainer(boosting_model)
    lime_explainer = LimeTabularExplainer(
        background,
        feature_names=feature_columns,
        class_names=["legitimate", "laundering_risk"],
        mode="classification",
        discretize_continuous=True,
        random_state=42,
    )
    LOG.info(
        "Loaded %s with threshold %.4f and %d features",
        ensemble_method,
        threshold,
        len(feature_columns),
    )
    return ModelArtifacts(
        feature_columns=feature_columns,
        label_encodings=label_encodings,
        threshold=threshold,
        ensemble_method=ensemble_method,
        boosting_winner=boosting_winner,
        bagging_model=bagging_model,
        boosting_model=boosting_model,
        meta_learner=meta_learner,
        shap_explainer=shap_explainer,
        lime_explainer=lime_explainer,
    )


def category_code(artifacts: ModelArtifacts, column: str, value: str) -> int:
    """Use the exact sorted factorization learned in phase 2; unseen values become -1."""
    aliases = {
        "Payment_currency": {
            "USD": "US dollar", "EUR": "Euro", "GBP": "UK pounds",
            "INR": "Indian rupee", "JPY": "Yen", "CHF": "Swiss franc",
            "AED": "Dirham", "PKR": "Pakistani rupee", "NGN": "Naira",
            "TRY": "Turkish lira", "MAD": "Moroccan dirham",
            "MXN": "Mexican Peso", "ALL": "Albanian lek",
        },
        "Received_currency": {
            "USD": "US dollar", "EUR": "Euro", "GBP": "UK pounds",
            "INR": "Indian rupee", "JPY": "Yen", "CHF": "Swiss franc",
            "AED": "Dirham", "PKR": "Pakistani rupee", "NGN": "Naira",
            "TRY": "Turkish lira", "MAD": "Moroccan dirham",
            "MXN": "Mexican Peso", "ALL": "Albanian lek",
        },
        "Sender_bank_location": {
            "US": "USA", "GB": "UK", "IN": "India", "DE": "Germany",
            "FR": "France", "AT": "Austria", "IT": "Italy", "JP": "Japan",
            "MX": "Mexico", "MA": "Morocco", "NL": "Netherlands",
            "NG": "Nigeria", "PK": "Pakistan", "ES": "Spain",
            "CH": "Switzerland", "TR": "Turkey", "AE": "UAE", "AL": "Albania",
        },
        "Receiver_bank_location": {
            "US": "USA", "GB": "UK", "IN": "India", "DE": "Germany",
            "FR": "France", "AT": "Austria", "IT": "Italy", "JP": "Japan",
            "MX": "Mexico", "MA": "Morocco", "NL": "Netherlands",
            "NG": "Nigeria", "PK": "Pakistan", "ES": "Spain",
            "CH": "Switzerland", "TR": "Turkey", "AE": "UAE", "AL": "Albania",
        },
        "Payment_type": {
            "CASH_DEPOSIT": "Cash Deposit", "CASH_WITHDRAWAL": "Cash Withdrawal",
            "CREDIT_CARD": "Credit card", "DEBIT_CARD": "Debit card",
            "CROSS_BORDER": "Cross-border",
        },
    }
    mapping = artifacts.label_encodings.get(column, {})
    canonical = aliases.get(column, {}).get(value.upper(), value)
    if canonical in mapping:
        return int(mapping[canonical])
    # Training data may have preserved mixed case.
    normalized = {str(k).upper(): int(v) for k, v in mapping.items()}
    return normalized.get(canonical.upper(), -1)


def input_warnings(tx: TransactionInput, artifacts: ModelArtifacts) -> list[str]:
    checks = [
        ("payment_currency", "Payment_currency", tx.payment_currency),
        ("received_currency", "Received_currency", tx.received_currency),
        ("sender_bank_location", "Sender_bank_location", tx.sender_bank_location),
        ("receiver_bank_location", "Receiver_bank_location", tx.receiver_bank_location),
        ("payment_type", "Payment_type", tx.payment_type),
    ]
    warnings = []
    for request_field, model_column, value in checks:
        if category_code(artifacts, model_column, value) == -1:
            warnings.append(
                f"{request_field}={value!r} was unseen during training and used code -1"
            )
    if tx.sender_history is None or tx.receiver_history is None:
        warnings.append(
            "Account history was incomplete; cold-start zeros were used for missing aggregates"
        )
    return warnings


def build_features(tx: TransactionInput, artifacts: ModelArtifacts) -> np.ndarray:
    dt = tx.transaction_datetime
    sender = tx.sender_history or AccountHistory()
    receiver = tx.receiver_history or AccountHistory()
    sender_avg_ratio = tx.amount / sender.average_amount if sender.average_amount > 0 else 1.0
    sender_max_ratio = tx.amount / sender.maximum_amount if sender.maximum_amount > 0 else 1.0

    values: dict[str, float] = {
        "Amount": tx.amount,
        "log_amount": float(np.log1p(tx.amount)),
        "is_round_100": float(tx.amount % 100 == 0),
        "is_round_1000": float(tx.amount % 1000 == 0),
        "hour": float(dt.hour),
        "day_of_week": float(dt.weekday()),
        "is_weekend": float(dt.weekday() >= 5),
        "is_night": float(dt.hour < 6 or dt.hour >= 22),
        "day_of_month": float(dt.day),
        "is_cross_border": float(
            tx.sender_bank_location != tx.receiver_bank_location
        ),
        "currency_mismatch": float(
            tx.payment_currency != tx.received_currency
        ),
        "Payment_currency_encoded": float(
            category_code(artifacts, "Payment_currency", tx.payment_currency)
        ),
        "Received_currency_encoded": float(
            category_code(artifacts, "Received_currency", tx.received_currency)
        ),
        "Sender_bank_location_encoded": float(
            category_code(artifacts, "Sender_bank_location", tx.sender_bank_location)
        ),
        "Receiver_bank_location_encoded": float(
            category_code(artifacts, "Receiver_bank_location", tx.receiver_bank_location)
        ),
        "Payment_type_encoded": float(
            category_code(artifacts, "Payment_type", tx.payment_type)
        ),
        "sender_tx_count": sender.transaction_count,
        "sender_avg_amount": sender.average_amount,
        "sender_std_amount": sender.amount_stddev,
        "sender_min_amount": sender.minimum_amount,
        "sender_max_amount": sender.maximum_amount,
        "sender_unique_receivers": sender.unique_counterparties,
        "receiver_tx_count": receiver.transaction_count,
        "receiver_avg_amount": receiver.average_amount,
        "receiver_unique_senders": receiver.unique_counterparties,
        "amount_to_sender_avg_ratio": sender_avg_ratio,
        "amount_to_sender_max_ratio": sender_max_ratio,
    }
    missing = [name for name in artifacts.feature_columns if name not in values]
    if missing:
        raise RuntimeError(f"Inference preprocessing is missing features: {missing}")
    vector = np.array(
        [[values[name] for name in artifacts.feature_columns]], dtype=np.float64
    )
    if not np.isfinite(vector).all():
        raise ValueError("Feature vector contains a non-finite value")
    return vector


def boosting_probability(artifacts: ModelArtifacts, matrix: np.ndarray) -> np.ndarray:
    if artifacts.boosting_winner.startswith("XGBoost"):
        return np.asarray(artifacts.boosting_model.predict(xgb.DMatrix(matrix)))
    return np.asarray(artifacts.boosting_model.predict_proba(matrix)[:, 1])


def ensemble_probability(artifacts: ModelArtifacts, matrix: np.ndarray) -> np.ndarray:
    method = artifacts.ensemble_method
    if "Boosting" in method and "Stacking" not in method:
        return boosting_probability(artifacts, matrix)
    if artifacts.bagging_model is None:
        raise RuntimeError(f"{method} requires a bagging model")
    bagging = np.asarray(artifacts.bagging_model.predict_proba(matrix)[:, 1])
    if "Bagging" in method and "Stacking" not in method:
        return bagging
    boosting = boosting_probability(artifacts, matrix)
    if "Stacking" in method:
        meta_input = np.column_stack([bagging, boosting])
        return np.asarray(artifacts.meta_learner.predict_proba(meta_input)[:, 1])
    if "Soft" in method:
        return (bagging + boosting) / 2.0
    return boosting


def direction(value: float, epsilon: float = 1e-10) -> str:
    if value > epsilon:
        return "increases_risk"
    if value < -epsilon:
        return "decreases_risk"
    return "neutral"


FEATURE_DISPLAY_NAMES = {
    "Amount": "transaction amount",
    "log_amount": "transaction amount magnitude",
    "is_round_100": "round-number amount",
    "is_round_1000": "large round-number amount",
    "hour": "transaction time",
    "day_of_week": "day of the week",
    "is_weekend": "weekend activity",
    "is_night": "late-night activity",
    "day_of_month": "day of the month",
    "is_cross_border": "cross-border transfer",
    "currency_mismatch": "currency mismatch",
    "Payment_currency_encoded": "payment currency",
    "Received_currency_encoded": "receiving currency",
    "Sender_bank_location_encoded": "sender bank location",
    "Receiver_bank_location_encoded": "receiver bank location",
    "Payment_type_encoded": "payment method",
    "sender_tx_count": "sender transaction volume",
    "sender_avg_amount": "sender's usual transaction amount",
    "sender_std_amount": "variation in sender transaction amounts",
    "sender_min_amount": "sender's historical minimum amount",
    "sender_max_amount": "sender's historical maximum amount",
    "sender_unique_receivers": "number of unique beneficiaries used by the sender",
    "receiver_tx_count": "receiver transaction volume",
    "receiver_avg_amount": "receiver's usual transaction amount",
    "receiver_unique_senders": "number of unique senders paying the receiver",
    "amount_to_sender_avg_ratio": "amount compared with the sender's usual amount",
    "amount_to_sender_max_ratio": "amount compared with the sender's historical maximum",
}


def human_feature_name(feature: str) -> str:
    return FEATURE_DISPLAY_NAMES.get(feature, feature.replace("_", " "))


def humanize_lime_condition(condition: str) -> str:
    readable = condition
    for feature in sorted(FEATURE_DISPLAY_NAMES, key=len, reverse=True):
        readable = readable.replace(feature, FEATURE_DISPLAY_NAMES[feature])
    return readable


def local_explanations(
    artifacts: ModelArtifacts, vector: np.ndarray
) -> ExplanationOutput:
    warnings: list[str] = []
    shap_factors: list[FeatureFactor] = []
    lime_factors: list[LimeFactor] = []

    # Explainability is supplementary: it must never suppress a valid score.
    try:
        shap_input: Any = (
            xgb.DMatrix(vector)
            if artifacts.boosting_winner.startswith("XGBoost")
            else vector
        )
        raw_shap = artifacts.shap_explainer.shap_values(shap_input)
        if isinstance(raw_shap, list):
            raw_shap = raw_shap[-1]
        shap_array = np.asarray(raw_shap)
        if shap_array.ndim == 3:
            shap_array = shap_array[:, :, -1]
        row = np.ravel(shap_array[0])
        if len(row) != len(artifacts.feature_columns):
            raise ValueError(
                f"SHAP returned {len(row)} values for "
                f"{len(artifacts.feature_columns)} features"
            )
        top_indices = np.argsort(np.abs(row))[::-1][:MAX_EXPLANATION_FEATURES]
        shap_factors = [
            FeatureFactor(
                feature=artifacts.feature_columns[index],
                display_name=human_feature_name(artifacts.feature_columns[index]),
                feature_value=round(float(vector[0, index]), 6),
                impact=round(float(row[index]), 6),
                direction=direction(float(row[index])),
            )
            for index in top_indices
        ]
    except Exception as exc:
        LOG.exception("SHAP explanation failed")
        warnings.append(f"SHAP unavailable: {type(exc).__name__}: {exc}")

    try:
        def lime_predict(samples: np.ndarray) -> np.ndarray:
            positive = ensemble_probability(artifacts, samples)
            return np.column_stack([1.0 - positive, positive])

        lime_result = artifacts.lime_explainer.explain_instance(
            vector[0],
            lime_predict,
            labels=(1,),
            num_features=MAX_EXPLANATION_FEATURES,
        )
        lime_factors = [
            LimeFactor(
                condition=condition,
                display_condition=humanize_lime_condition(condition),
                weight=round(float(weight), 6),
                direction=direction(float(weight)),
            )
            for condition, weight in lime_result.as_list(label=1)
        ]
    except Exception as exc:
        LOG.exception("LIME explanation failed")
        warnings.append(f"LIME unavailable: {type(exc).__name__}: {exc}")

    return ExplanationOutput(
        scope=(
            "SHAP explains the winning boosting arm used by the ensemble; "
            "LIME approximates the final ensemble prediction locally."
        ),
        shap_factors=shap_factors,
        lime_factors=lime_factors,
        warnings=warnings,
    )


def risk_category(score: float) -> str:
    """Return the operational category represented by the displayed score."""
    if score < 50.0:
        return "LOW"
    if score < 70.0:
        return "MEDIUM"
    if score < 85.0:
        return "HIGH"
    return "CRITICAL"


def operational_risk_score(raw_output: float, threshold: float) -> float:
    """Map model output onto consistent LOW/MEDIUM/HIGH/CRITICAL bands.

    Class-weighted tree outputs are ranking scores, not calibrated probabilities.
    The validated flag threshold maps to 50, 0.965 maps to 70, and 0.971 maps
    to 85. This keeps the score, category, and binary flag decision aligned.
    """
    raw_output = float(np.clip(raw_output, 0.0, 1.0))
    threshold = float(np.clip(threshold, 1e-9, 1.0 - 1e-9))
    high_cutoff = 0.965
    critical_cutoff = 0.971

    if not threshold < high_cutoff < critical_cutoff < 1.0:
        raise ValueError(
            "Risk-score cutoffs require the model threshold to be below 0.965"
        )

    if raw_output < threshold:
        return 49.99 * raw_output / threshold
    if raw_output < high_cutoff:
        return 50.0 + 20.0 * (
            (raw_output - threshold) / (high_cutoff - threshold)
        )
    if raw_output < critical_cutoff:
        return 70.0 + 15.0 * (
            (raw_output - high_cutoff) / (critical_cutoff - high_cutoff)
        )
    return min(
        100.0,
        85.0 + 15.0 * (
            (raw_output - critical_cutoff) / (1.0 - critical_cutoff)
        ),
    )


def decision_confidence(raw_output: float, threshold: float) -> float:
    """Normalized distance from the operating threshold, expressed as 0-100."""
    if raw_output >= threshold:
        span = max(1.0 - threshold, 1e-9)
    else:
        span = max(threshold, 1e-9)
    return float(np.clip(abs(raw_output - threshold) / span * 100.0, 0.0, 100.0))


def deterministic_text(
    category: str, should_flag: bool, factors: list[FeatureFactor]
) -> GeminiOutput:
    risky = [f.display_name for f in factors if f.direction == "increases_risk"][:3]
    if len(risky) > 1:
        factor_text = ", ".join(risky[:-1]) + f", and {risky[-1]}"
    elif risky:
        factor_text = risky[0]
    else:
        factor_text = "the combined transaction and account signals"
    recommendation = contextual_recommendation(category, should_flag, factors)
    return GeminiOutput(
        one_line_explanation=(
            f"This transaction has a {category.lower()} risk rating; "
            f"the main contributing factors are {factor_text}."
        ),
        recommendation=recommendation,
    )


def contextual_recommendation(
    category: str, should_flag: bool, factors: list[FeatureFactor]
) -> str:
    """Combine a controlled AML action with transaction-specific model evidence."""
    policies = {
        "LOW": "Continue routine monitoring and retain the assessment for audit",
        "MEDIUM": "Route to an AML analyst to verify the transaction purpose and account context",
        "HIGH": "Perform enhanced due diligence and review related accounts and beneficiaries",
        "CRITICAL": "Immediately review the transaction and escalate it to senior compliance",
    }
    preferred_direction = "decreases_risk" if category == "LOW" else "increases_risk"
    relevant = [factor for factor in factors if factor.direction == preferred_direction][:2]
    if not relevant:
        relevant = factors[:2]
    focus = " and ".join(
        f"{factor.display_name} ({factor.feature_value:g})" for factor in relevant
    )
    policy = policies.get(
        category,
        "Route the transaction to an AML analyst for review"
        if should_flag
        else "Continue routine monitoring",
    )
    return f"{policy}; focus on {focus}." if focus else f"{policy}."


def gemini_text(
    tx: TransactionInput,
    score: float,
    category: str,
    should_flag: bool,
    factors: list[FeatureFactor],
) -> tuple[GeminiOutput, Literal["gemini", "deterministic_fallback"], str | None]:
    fallback = deterministic_text(category, should_flag, factors)
    if not tx.use_gemini:
        return fallback, "deterministic_fallback", "Gemini was disabled for this request"
    if GEMINI_CLIENT is None:
        return fallback, "deterministic_fallback", "Gemini is not configured"

    safe_factors = [
        {
            "feature": item.display_name,
            "value": item.feature_value,
            "direction": item.direction,
            "impact": item.impact,
        }
        for item in factors
    ]
    required_actions = {
        "LOW": "Recommend routine monitoring without escalation.",
        "MEDIUM": "Recommend analyst review of transaction purpose and supporting context.",
        "HIGH": "Recommend enhanced due diligence and review of related accounts.",
        "CRITICAL": "Recommend immediate senior-compliance escalation and transaction review.",
    }
    prompt = (
        "You support an AML analyst. Do not claim that money laundering occurred. "
        "Use only the supplied model output. Produce one concise explanation sentence "
        "and one concise compliance recommendation. The explanation must explicitly "
        f"contain the words '{category.lower()} risk' and the score, and must describe "
        "the two strongest supplied SHAP factors using their actual numeric values. "
        "For LOW risk, prioritize risk-decreasing factors; for flagged categories, "
        "prioritize risk-increasing factors. Do not say only 'high', 'low', 'many', "
        "or 'numerous' without including the supplied value. "
        f"Required recommendation policy: {required_actions[category]} "
        f"Risk score: {score:.2f}/100; category: {category}; flagged: {should_flag}; "
        f"local SHAP factors: {json.dumps(safe_factors)}"
    )
    try:
        response = GEMINI_CLIENT.models.generate_content(
            model=GEMINI_MODEL,
            contents=prompt,
            config=genai_types.GenerateContentConfig(
                temperature=0.1,
                max_output_tokens=160,
                response_mime_type="application/json",
                response_schema=GeminiOutput,
            ),
        )
        generated = GeminiOutput.model_validate_json(response.text)
        if f"{category.lower()} risk" not in generated.one_line_explanation.lower():
            generated.one_line_explanation = (
                f"{category.title()} risk ({score:.2f}/100): "
                f"{generated.one_line_explanation}"
            )
        # Keep the action policy-controlled while tailoring it to this prediction.
        generated.recommendation = contextual_recommendation(
            category, should_flag, factors
        )
        return generated, "gemini", None
    except Exception as exc:
        LOG.exception("Gemini explanation failed; returning deterministic explanation")
        return (
            fallback,
            "deterministic_fallback",
            f"Gemini request failed: {type(exc).__name__}: {exc}",
        )


def score_transaction(tx: TransactionInput) -> RiskResponse:
    if ARTIFACTS is None:
        raise RuntimeError("Model artifacts are not loaded")
    started = time.perf_counter()
    vector = build_features(tx, ARTIFACTS)
    probability = float(ensemble_probability(ARTIFACTS, vector)[0])
    probability = float(np.clip(probability, 0.0, 1.0))
    score = operational_risk_score(probability, ARTIFACTS.threshold)
    should_flag = probability >= ARTIFACTS.threshold
    category = risk_category(score)
    explanations = local_explanations(ARTIFACTS, vector)
    generated, explanation_source, explanation_warning = gemini_text(
        tx, score, category, should_flag, explanations.shap_factors
    )
    confidence = decision_confidence(probability, ARTIFACTS.threshold)
    history_context = (
        "provided"
        if tx.sender_history is not None and tx.receiver_history is not None
        else "cold_start_defaults"
    )
    return RiskResponse(
        request_id=str(uuid.uuid4()),
        transaction_id=tx.transaction_id,
        risk_score=round(score, 2),
        raw_model_output=round(probability, 6),
        risk_category=category,
        model_confidence=round(confidence, 2),
        should_flag=should_flag,
        decision_threshold=round(ARTIFACTS.threshold, 6),
        ensemble_method=ARTIFACTS.ensemble_method,
        explanation=explanations,
        one_line_explanation=generated.one_line_explanation,
        recommendation=generated.recommendation,
        explanation_source=explanation_source,
        explanation_warning=explanation_warning,
        history_context=history_context,
        input_warnings=input_warnings(tx, ARTIFACTS),
        processing_time_ms=round((time.perf_counter() - started) * 1000.0, 2),
    )


@asynccontextmanager
async def lifespan(_: FastAPI):
    global ARTIFACTS, GEMINI_CLIENT, ARTIFACT_LOAD_ERROR
    try:
        ARTIFACTS = await asyncio.to_thread(load_artifacts)
        ARTIFACT_LOAD_ERROR = None
    except (FileNotFoundError, KeyError, ValueError) as exc:
        # Keep the API online so /health exposes the exact deployment problem.
        # Scoring remains unavailable and returns HTTP 503 until artifacts exist.
        ARTIFACTS = None
        ARTIFACT_LOAD_ERROR = str(exc)
        LOG.error("Model artifacts could not be loaded: %s", exc)
    gemini_key = os.getenv("GEMINI_API_KEY")
    if gemini_key and genai is not None:
        GEMINI_CLIENT = genai.Client(api_key=gemini_key)
        LOG.info("Gemini explanations enabled with model %s", GEMINI_MODEL)
    elif gemini_key:
        LOG.warning("GEMINI_API_KEY is set but google-genai is not installed")
    else:
        LOG.info("Gemini disabled; deterministic explanations will be returned")
    yield
    ARTIFACTS = None
    GEMINI_CLIENT = None
    ARTIFACT_LOAD_ERROR = None


app = FastAPI(
    title="AML Transaction Risk API",
    version="1.0.0",
    description="Ensemble AML scoring with SHAP, LIME and optional Gemini explanations.",
    lifespan=lifespan,
)

# allowed_origins = [
#     item.strip()
#     for item in os.getenv("CORS_ORIGINS", "http://localhost:3000").split(",")
#     if item.strip()
# ]
# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=allowed_origins,
#     allow_credentials=False,
#     allow_methods=["GET", "POST"],
#     allow_headers=["Authorization", "Content-Type", "X-Correlation-ID"],
# )


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):
    request_id = request.headers.get("X-Correlation-ID", str(uuid.uuid4()))
    LOG.exception("Unhandled request error request_id=%s", request_id)
    return JSONResponse(
        status_code=500,
        content={
            "error": "INTERNAL_SERVER_ERROR",
            "message": "The transaction could not be assessed.",
            "request_id": request_id,
        },
    )


@app.get("/health")
async def health() -> dict[str, Any]:
    return {
        "status": "healthy" if ARTIFACTS is not None else "degraded",
        "model_loaded": ARTIFACTS is not None,
        "model_error": ARTIFACT_LOAD_ERROR,
        "artifact_directories": {
            "output": str(OUTPUT_DIR),
            "models": str(MODELS_DIR),
            "splits": str(SPLITS_DIR),
        },
        "gemini_enabled": GEMINI_CLIENT is not None,
        "ensemble_method": ARTIFACTS.ensemble_method if ARTIFACTS else None,
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }


@app.post("/api/v1/risk-assessments/transactions", response_model=RiskResponse)
async def assess_transaction(transaction: TransactionInput) -> RiskResponse:
    if ARTIFACTS is None:
        raise HTTPException(
            status_code=503,
            detail={
                "message": "Model is unavailable. Check GET /health.",
                "model_error": ARTIFACT_LOAD_ERROR,
            },
        )
    try:
        # SHAP/LIME/model inference are CPU-bound and must not block the event loop.
        return await asyncio.to_thread(score_transaction, transaction)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from exc


@app.post(
    "/predict",
    response_model=RiskResponse,
    summary="Score one transaction",
    description="Convenience alias for /api/v1/risk-assessments/transactions.",
)
async def predict(transaction: TransactionInput) -> RiskResponse:
    return await assess_transaction(transaction)


@app.post(
    "/api/v1/risk-assessments/transactions/{transaction_id}",
    response_model=RiskResponse,
)
async def assess_transaction_by_id(
    transaction_id: str, transaction: TransactionInput
) -> RiskResponse:
    # The path value is authoritative and supports the endpoint shape in the AML spec.
    transaction.transaction_id = transaction_id
    return await assess_transaction(transaction)
