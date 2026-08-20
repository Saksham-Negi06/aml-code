package com.aml.common.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RiskAssessmentResponse {

    private String transactionId;
    private String requestId;
    private BigDecimal riskScore;
    private String riskCategory;
    private BigDecimal modelConfidence;
    private Boolean shouldFlag;
    private BigDecimal decisionThreshold;
    private String ensembleMethod;
    private String explanationJson;
    private String oneLineExplanation;
    private String recommendation;
    private String historyContext;
    private BigDecimal processingTimeMs;
    private LocalDateTime createdAt;

    public RiskAssessmentResponse() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}