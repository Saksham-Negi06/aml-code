package com.aml.common.dto.response;

import java.time.LocalDateTime;

public class GeneratedReportResponse {
    private String id;
    private String caseId;
    private String format;
    private String provider;
    private String modelName;
    private String content;
    private String downloadUrl;
    private LocalDateTime createdAt;
    public String getId() { return id; }
    public void setId(String value) { id = value; }
    public String getCaseId() { return caseId; }
    public void setCaseId(String value) { caseId = value; }
    public String getFormat() { return format; }
    public void setFormat(String value) { format = value; }
    public String getProvider() { return provider; }
    public void setProvider(String value) { provider = value; }
    public String getModelName() { return modelName; }
    public void setModelName(String value) { modelName = value; }
    public String getContent() { return content; }
    public void setContent(String value) { content = value; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String value) { downloadUrl = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
}
