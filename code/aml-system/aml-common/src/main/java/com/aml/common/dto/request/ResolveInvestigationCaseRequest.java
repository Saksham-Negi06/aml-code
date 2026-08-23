package com.aml.common.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ResolveInvestigationCaseRequest {
    @NotBlank private String resolution;
    private String resolutionNotes;
    private String recommendation;
    private String resolvedBy;
    public String getResolution() { return resolution; }
    public void setResolution(String value) { resolution = value; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String value) { resolutionNotes = value; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String value) { recommendation = value; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String value) { resolvedBy = value; }
}
