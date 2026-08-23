package com.aml.common.dto.request;

import jakarta.validation.constraints.NotBlank;

public class GenerateReportRequest {
    @NotBlank private String caseId;
    private String reportFormat = "TEXT";
    private boolean includeAnalysis = true;
    private String llmProvider = "TEMPLATE";
    private String customInstructions;
    public String getCaseId() { return caseId; }
    public void setCaseId(String value) { caseId = value; }
    public String getReportFormat() { return reportFormat; }
    public void setReportFormat(String value) { reportFormat = value; }
    public boolean isIncludeAnalysis() { return includeAnalysis; }
    public void setIncludeAnalysis(boolean value) { includeAnalysis = value; }
    public String getLlmProvider() { return llmProvider; }
    public void setLlmProvider(String value) { llmProvider = value; }
    public String getCustomInstructions() { return customInstructions; }
    public void setCustomInstructions(String value) { customInstructions = value; }
}
