package com.aml.common.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "risk_assessments",
        indexes = {
                @Index(
                        name = "idx_risk_assessment_transaction",
                        columnList = "transaction_id"
                ),
                @Index(
                        name = "idx_risk_assessment_request",
                        columnList = "request_id"
                )
        }
)
public class RiskAssessment {

    @Id
    @Column(length = 36)
    private String id;

    /*
     * One transaction can be assessed multiple times.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    /*
     * The feature snapshot used for this particular model run.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "feature_snapshot_id",
            nullable = false,
            unique = true
    )
    private TransactionFeatureSnapshot featureSnapshot;

    @Column(
            name = "request_id",
            nullable = false,
            unique = true,
            length = 100
    )
    private String requestId;

    @Column(name = "risk_score", precision = 8, scale = 4)
    private BigDecimal riskScore;

    @Column(name = "risk_category", length = 30)
    private String riskCategory;

    @Column(name = "model_confidence", precision = 8, scale = 4)
    private BigDecimal modelConfidence;

    @Column(name = "should_flag", nullable = false)
    private Boolean shouldFlag;

    @Column(name = "decision_threshold", precision = 10, scale = 6)
    private BigDecimal decisionThreshold;

    @Column(name = "ensemble_method", length = 100)
    private String ensembleMethod;

    /*
     * SHAP/LIME output is model-specific and variable-length.
     * Store the complete object as JSON text for now.
     */
    @Lob
    @Column(name = "explanation_json")
    private String explanationJson;

    @Column(name = "one_line_explanation", length = 1000)
    private String oneLineExplanation;

    @Column(name = "recommendation", length = 2000)
    private String recommendation;

    @Column(name = "history_context", length = 100)
    private String historyContext;

    @Column(name = "processing_time_ms", precision = 19, scale = 4)
    private BigDecimal processingTimeMs;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "model_version", length = 100)
    private String modelVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RiskAssessment() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (shouldFlag == null) {
            shouldFlag = false;
        }
    }

    // ------------------------------------------------------------
    // Getters / Setters
    // ------------------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public TransactionFeatureSnapshot getFeatureSnapshot() {
        return featureSnapshot;
    }

    public void setFeatureSnapshot(
            TransactionFeatureSnapshot featureSnapshot
    ) {
        this.featureSnapshot = featureSnapshot;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }

    public BigDecimal getModelConfidence() {
        return modelConfidence;
    }

    public void setModelConfidence(BigDecimal modelConfidence) {
        this.modelConfidence = modelConfidence;
    }

    public Boolean getShouldFlag() {
        return shouldFlag;
    }

    public void setShouldFlag(Boolean shouldFlag) {
        this.shouldFlag = shouldFlag;
    }

    public BigDecimal getDecisionThreshold() {
        return decisionThreshold;
    }

    public void setDecisionThreshold(BigDecimal decisionThreshold) {
        this.decisionThreshold = decisionThreshold;
    }

    public String getEnsembleMethod() {
        return ensembleMethod;
    }

    public void setEnsembleMethod(String ensembleMethod) {
        this.ensembleMethod = ensembleMethod;
    }

    public String getExplanationJson() {
        return explanationJson;
    }

    public void setExplanationJson(String explanationJson) {
        this.explanationJson = explanationJson;
    }

    public String getOneLineExplanation() {
        return oneLineExplanation;
    }

    public void setOneLineExplanation(String oneLineExplanation) {
        this.oneLineExplanation = oneLineExplanation;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getHistoryContext() {
        return historyContext;
    }

    public void setHistoryContext(String historyContext) {
        this.historyContext = historyContext;
    }

    public BigDecimal getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(BigDecimal processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}