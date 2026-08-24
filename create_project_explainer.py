from __future__ import annotations

import textwrap
from pathlib import Path

import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch


ROOT = Path(__file__).resolve().parent
OUTPUT = ROOT / "AML_project_explainer.pdf"

NAVY = "#102A43"
BLUE = "#1D6FA5"
TEAL = "#168F8B"
GOLD = "#D18B22"
RED = "#B84A39"
INK = "#243B53"
MUTED = "#627D98"
PALE = "#F4F7FA"
LINE = "#D9E2EC"


def page(pdf: PdfPages, title: str, subtitle: str = ""):
    fig = plt.figure(figsize=(11.69, 8.27), facecolor="white")
    fig.text(0.065, 0.925, title, fontsize=24, fontweight="bold", color=NAVY)
    if subtitle:
        fig.text(0.067, 0.887, subtitle, fontsize=10.5, color=MUTED)
    fig.add_artist(plt.Line2D([0.065, 0.935], [0.86, 0.86], color=TEAL, linewidth=2))
    fig.text(0.065, 0.035, "AML Transaction Risk API | Project explainer", fontsize=8, color=MUTED)
    fig.text(0.935, 0.035, str(len(pdf.get_pages())) if False else "", ha="right")
    return fig


def block(fig, x, y, w, h, heading, body, color=BLUE, fontsize=10):
    fig.patches.append(
        FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.012,rounding_size=0.008",
                       transform=fig.transFigure, facecolor=PALE, edgecolor=LINE, linewidth=1)
    )
    fig.text(x + 0.018, y + h - 0.035, heading, fontsize=12, fontweight="bold", color=color)
    lines = []
    for paragraph in body.split("\n"):
        lines.extend(textwrap.wrap(paragraph, width=max(25, int(w * 150)), break_long_words=False))
        lines.append("")
    fig.text(x + 0.018, y + h - 0.075, "\n".join(lines[:-1]), fontsize=fontsize, color=INK, va="top", linespacing=1.35)


def bullet_text(items):
    return "\n".join(f"- {item}" for item in items)


def flow_box(fig, x, y, w, h, label, color):
    fig.patches.append(FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.012,rounding_size=0.01",
                                      transform=fig.transFigure, facecolor=color, edgecolor=color))
    fig.text(x + w / 2, y + h / 2, label, ha="center", va="center", fontsize=11,
             color="white", fontweight="bold", wrap=True)


def arrow(fig, x1, y1, x2, y2):
    fig.patches.append(FancyArrowPatch((x1, y1), (x2, y2), transform=fig.transFigure,
                                       arrowstyle="-|>", mutation_scale=15, linewidth=1.5, color=MUTED))


def make_pdf():
    with PdfPages(OUTPUT) as pdf:
        fig = page(pdf, "AML Transaction Risk API", "Presentation handout | What the system does and how one request moves through it")
        fig.text(0.07, 0.78, "Purpose", fontsize=13, fontweight="bold", color=TEAL)
        fig.text(0.07, 0.735, "Assess one financial transaction for potential money-laundering risk,\nthen return a review-ready decision with evidence.", fontsize=19, color=NAVY, linespacing=1.35)
        block(fig, 0.07, 0.39, 0.40, 0.23, "Input", bullet_text([
            "Transaction amount, currencies, locations, payment type, timestamp",
            "Optional sender and receiver account history",
            "Validated by Pydantic before inference",
        ]), TEAL)
        block(fig, 0.53, 0.39, 0.40, 0.23, "Output", bullet_text([
            "Risk score from 0 to 100 and LOW to CRITICAL category",
            "Flag decision, confidence, threshold, warnings",
            "SHAP, LIME, and a controlled explanation/recommendation",
        ]), GOLD)
        fig.text(0.07, 0.27, "The central design choice", fontsize=13, fontweight="bold", color=TEAL)
        fig.text(0.07, 0.225, "The model is recall-biased: missing a risky transaction is considered more costly than sending a false positive to human review.", fontsize=13, color=INK)
        fig.text(0.07, 0.15, "Winning deployed method: Boosting only | LightGBM_scale_pos_weight | threshold 0.9574", fontsize=12, color=RED, fontweight="bold")
        pdf.savefig(fig); plt.close(fig)

        fig = page(pdf, "System Architecture", "The FastAPI module is the orchestration layer between clients, model artifacts, explainers, and optional Gemini text generation")
        boxes = [
            (0.07, 0.65, 0.18, 0.10, "Client\n(frontend / Postman)", BLUE),
            (0.31, 0.65, 0.18, 0.10, "FastAPI\nendpoints", TEAL),
            (0.55, 0.65, 0.18, 0.10, "Feature\nengineering", GOLD),
            (0.79, 0.65, 0.14, 0.10, "Model\nscore", RED),
        ]
        for box in boxes:
            flow_box(fig, *box)
        for a, b in zip(boxes, boxes[1:]):
            arrow(fig, a[0] + a[2], a[1] + a[3] / 2, b[0], b[1] + b[3] / 2)
        flow_box(fig, 0.31, 0.42, 0.18, 0.10, "SHAP + LIME\nlocal evidence", TEAL)
        flow_box(fig, 0.55, 0.42, 0.18, 0.10, "Risk band +\nflag decision", GOLD)
        flow_box(fig, 0.79, 0.42, 0.14, 0.10, "JSON\nresponse", RED)
        arrow(fig, 0.88, 0.65, 0.86, 0.52)
        arrow(fig, 0.79, 0.47, 0.73, 0.47)
        arrow(fig, 0.55, 0.47, 0.49, 0.47)
        arrow(fig, 0.49, 0.47, 0.49, 0.65)
        block(fig, 0.07, 0.16, 0.86, 0.16, "Startup dependencies", "lifespan() loads output/*.json, models/*, and splits/train.parquet. The model state is kept in the global ARTIFACTS object. If loading fails, /health stays available but scoring returns HTTP 503.", BLUE)
        pdf.savefig(fig); plt.close(fig)

        fig = page(pdf, "API Request Flow", "One transaction from HTTP request to a compliance-oriented response")
        steps = [
            ("1", "Validate", "Pydantic checks required fields, positive amount, category lengths, and timezone-aware timestamp."),
            ("2", "Normalize", "Category values are uppercased; aliases map API values to the labels used during training."),
            ("3", "Build 27 features", "Time, amount, cross-border, currency mismatch, encoded categories, account history, and ratios."),
            ("4", "Score", "The selected ensemble returns a raw positive-class output. Current artifact configuration uses LightGBM boosting only."),
            ("5", "Decide", "Raw output is compared with 0.9574; it is also mapped to a 0-100 operational score and risk band."),
            ("6", "Explain", "SHAP explains the boosting arm; LIME approximates the final ensemble locally; Gemini is optional."),
            ("7", "Respond", "RiskResponse returns score, flag, evidence, recommendation, warnings, IDs, and processing time."),
        ]
        y = 0.77
        for number, heading, body in steps:
            fig.text(0.08, y, number, fontsize=16, color="white", ha="center", va="center", fontweight="bold",
                     bbox=dict(boxstyle="circle,pad=0.28", facecolor=TEAL, edgecolor=TEAL))
            fig.text(0.14, y + 0.012, heading, fontsize=12, fontweight="bold", color=NAVY)
            fig.text(0.24, y + 0.012, textwrap.fill(body, 95), fontsize=10, color=INK, va="top")
            y -= 0.095
        fig.text(0.08, 0.095, "CPU-bound work is sent through asyncio.to_thread(), so SHAP/LIME/model inference does not block the FastAPI event loop.", fontsize=11, color=RED, fontweight="bold")
        pdf.savefig(fig); plt.close(fig)

        fig = page(pdf, "Features and Model Decision", "The training contract is preserved at inference time")
        block(fig, 0.07, 0.55, 0.40, 0.25, "Feature groups", bullet_text([
            "Amount: raw amount, log amount, round-number indicators",
            "Time: hour, weekday, weekend, night, day of month",
            "Transaction: cross-border and currency mismatch",
            "Encoded categories: currencies, bank locations, payment type",
            "History: sender/receiver aggregates and amount ratios",
        ]), BLUE, 9.5)
        block(fig, 0.53, 0.55, 0.40, 0.25, "Imbalance strategy", bullet_text([
            "No SMOTE or synthetic oversampling",
            "Boosting uses scale_pos_weight = 983.44",
            "Threshold selected from validation PR curve",
            "Chosen objective: max recall at precision >= 0.5",
            "Flag when raw output >= 0.9574",
        ]), GOLD, 9.5)
        block(fig, 0.07, 0.20, 0.86, 0.20, "Risk score mapping", "The raw model output is not presented as a calibrated probability. It is clipped to [0, 1], compared with the validated flag threshold, and mapped into operational bands: below 50 LOW, 50-70 MEDIUM, 70-85 HIGH, and 85-100 CRITICAL. This keeps the displayed score, category, and binary flag aligned.", TEAL)
        pdf.savefig(fig); plt.close(fig)

        fig = page(pdf, "Explainability and Reliability", "The system separates model evidence from generated language")
        block(fig, 0.07, 0.57, 0.40, 0.24, "SHAP", "TreeExplainer runs against the winning boosting model. The response returns up to five strongest factors with feature value, impact, and direction. Positive impact means increases_risk; negative impact means decreases_risk.", TEAL)
        block(fig, 0.53, 0.57, 0.40, 0.24, "LIME", "A TabularExplainer is initialized from up to 2,000 sampled training rows. It perturbs the current vector and calls the full ensemble, so it describes the local final prediction.", BLUE)
        block(fig, 0.07, 0.24, 0.40, 0.22, "Gemini", "Optional. If GEMINI_API_KEY is configured and use_gemini is true, Gemini produces one concise explanation sentence. The recommendation remains policy-controlled by the API.", GOLD)
        block(fig, 0.53, 0.24, 0.40, 0.22, "Fallbacks", "Without Gemini, the deterministic formatter is used. SHAP/LIME failures become warnings and do not suppress a valid score. Missing artifacts produce degraded health and HTTP 503 for scoring.", RED)
        fig.text(0.07, 0.14, "Important framing: a risk flag is a review signal, not a finding that laundering occurred.", fontsize=12, color=NAVY, fontweight="bold")
        pdf.savefig(fig); plt.close(fig)

        fig = page(pdf, "Endpoints and Example Contract", "The service exposes health, a canonical assessment route, and two convenience/compatibility routes")
        block(fig, 0.07, 0.58, 0.86, 0.22, "Routes", bullet_text([
            "GET /health -> model status, artifact paths, Gemini status, loaded ensemble, timestamp",
            "POST /api/v1/risk-assessments/transactions -> canonical transaction assessment",
            "POST /predict -> alias for the canonical assessment route",
            "POST /api/v1/risk-assessments/transactions/{transaction_id} -> path ID overrides body ID",
            "Interactive documentation is available through FastAPI at /docs when the server is running",
        ]), BLUE, 10)
        block(fig, 0.07, 0.26, 0.40, 0.22, "Request essentials", "transaction_id (optional), sender_account, receiver_account, amount, payment_currency, received_currency, sender_bank_location, receiver_bank_location, payment_type, transaction_datetime, optional histories, use_gemini.", TEAL, 9.5)
        block(fig, 0.53, 0.26, 0.40, 0.22, "Response essentials", "request_id, transaction_id, risk_score, raw_model_output, risk_category, model_confidence, should_flag, decision_threshold, ensemble_method, explanation, recommendation, warnings, processing_time_ms.", GOLD, 9.5)
        fig.text(0.07, 0.13, "Run from ml_req: uvicorn fastapi_app:app --host localhost --port 8500 --reload --env-file .env", fontsize=10.5, color=INK, family="monospace")
        pdf.savefig(fig); plt.close(fig)

        fig = page(pdf, "Artifacts and Results to Present", "What is loaded, why it matters, and the measured held-out performance")
        block(fig, 0.07, 0.57, 0.40, 0.23, "Runtime artifacts", bullet_text([
            "output/split_info.json: 27 feature names and label encodings",
            "output/metrics_report.json: ensemble, threshold, metrics",
            "output/boosting_arm_metrics.json: winner and boosting configuration",
            "models/boosting_arm_model.joblib: deployed LightGBM model",
            "splits/train.parquet: LIME background data",
        ]), BLUE, 9.5)
        block(fig, 0.53, 0.57, 0.40, 0.23, "Dataset snapshot", bullet_text([
            "Train: 6,690,227 rows; 6,796 positive",
            "Validation: 1,429,816 rows; 1,436 positive",
            "Test: 1,384,809 rows; 1,641 positive",
            "Highly imbalanced target, handled with weighting",
        ]), TEAL, 9.5)
        block(fig, 0.07, 0.23, 0.86, 0.22, "Held-out test results", "PR-AUC 0.7595 | precision at chosen threshold 0.7655 | recall 0.7599 | F1 0.7627. The chosen validation operating point was threshold 0.9574 with precision 0.6349 and recall 0.7521. The test result is a single held-out test-set touch, as noted in the metrics report.", GOLD, 11)
        fig.text(0.07, 0.14, "Top global SHAP signals in the report: receiver_unique_senders, sender_min_amount, sender_unique_receivers, receiver_tx_count, sender_tx_count.", fontsize=10, color=INK)
        pdf.savefig(fig); plt.close(fig)

        fig = page(pdf, "A Simple Explanation to Say Out Loud", "Suggested 60-second project summary")
        summary = ("This project exposes an AML transaction-risk model through a FastAPI service. "
                   "A client sends transaction details and optional account history. The API validates and normalizes the input, reconstructs the same 27 features used in training, and sends them to the selected LightGBM model. "
                   "Because laundering examples are rare, the model uses class weighting and a validation-selected threshold of 0.9574 rather than the default 0.5. "
                   "The raw output is converted into an operational score and a LOW, MEDIUM, HIGH, or CRITICAL category, with a flag for analyst review. "
                   "SHAP shows the strongest feature contributions, LIME explains the local ensemble behavior, and Gemini can turn that evidence into a concise sentence. "
                   "The response is designed to support compliance review: it explains why a transaction was prioritized, but it does not claim that laundering occurred.")
        fig.text(0.09, 0.73, textwrap.fill(summary, 92), fontsize=16, color=NAVY, va="top", linespacing=1.6)
        fig.text(0.09, 0.30, "Three points worth emphasizing", fontsize=13, fontweight="bold", color=TEAL)
        fig.text(0.11, 0.24, bullet_text([
            "The API and model use the same feature/encoding contract.",
            "Threshold selection reflects the business cost of missed risk.",
            "Every assessment returns both a decision and supporting evidence.",
        ]), fontsize=13, color=INK, linespacing=1.8)
        pdf.savefig(fig); plt.close(fig)


if __name__ == "__main__":
    make_pdf()
    print(OUTPUT)