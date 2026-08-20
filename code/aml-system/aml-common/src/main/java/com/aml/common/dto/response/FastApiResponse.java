package com.aml.common.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class FastApiResponse {

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("risk_score")
    private BigDecimal riskScore;

    @JsonProperty("risk_category")
    private String riskCategory;

    @JsonProperty("model_confidence")
    private BigDecimal modelConfidence;

    @JsonProperty("should_flag")
    private Boolean shouldFlag;

    @JsonProperty("decision_threshold")
    private BigDecimal decisionThreshold;

    @JsonProperty("ensemble_method")
    private String ensembleMethod;

    private Explanation explanation;

    @JsonProperty("one_line_explanation")
    private String oneLineExplanation;

    private String recommendation;

    @JsonProperty("history_context")
    private String historyContext;

    @JsonProperty("processing_time_ms")
    private BigDecimal processingTimeMs;

    public FastApiResponse() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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

    public Explanation getExplanation() {
        return explanation;
    }

    public void setExplanation(Explanation explanation) {
        this.explanation = explanation;
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

    public static class Explanation {

        private String scope;

        @JsonProperty("shap_factors")
        private List<ShapFactor> shapFactors;

        @JsonProperty("lime_factors")
        private List<LimeFactor> limeFactors;

        public Explanation() {
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public List<ShapFactor> getShapFactors() {
            return shapFactors;
        }

        public void setShapFactors(
                List<ShapFactor> shapFactors
        ) {
            this.shapFactors = shapFactors;
        }

        public List<LimeFactor> getLimeFactors() {
            return limeFactors;
        }

        public void setLimeFactors(
                List<LimeFactor> limeFactors
        ) {
            this.limeFactors = limeFactors;
        }
    }

    public static class ShapFactor {

        private String feature;

        @JsonProperty("feature_value")
        private BigDecimal featureValue;

        private BigDecimal impact;

        private String direction;

        public ShapFactor() {
        }

        public String getFeature() {
            return feature;
        }

        public void setFeature(String feature) {
            this.feature = feature;
        }

        public BigDecimal getFeatureValue() {
            return featureValue;
        }

        public void setFeatureValue(BigDecimal featureValue) {
            this.featureValue = featureValue;
        }

        public BigDecimal getImpact() {
            return impact;
        }

        public void setImpact(BigDecimal impact) {
            this.impact = impact;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }
    }

    public static class LimeFactor {

        private String condition;
        private BigDecimal weight;
        private String direction;

        public LimeFactor() {
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public BigDecimal getWeight() {
            return weight;
        }

        public void setWeight(BigDecimal weight) {
            this.weight = weight;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }
    }
}