package com.aml.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "generated_reports")
public class GeneratedReport {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "case_id", nullable = false, length = 36)
    private String caseId;
    @Column(nullable = false, length = 20)
    private String format;
    @Column(nullable = false, length = 30)
    private String provider;
    @Column(name = "model_name", length = 100)
    private String modelName;
    @Lob
    @Column(name = "report_content", nullable = false)
    private String content;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
}
