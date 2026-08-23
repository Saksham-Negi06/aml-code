package com.aml.common.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "investigation_cases", uniqueConstraints = @UniqueConstraint(name = "uk_case_transaction", columnNames = "transaction_id"))
public class InvestigationCase {
    @Id @Column(length = 36) private String id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "transaction_id", nullable = false) private Transaction transaction;
    @Column(nullable = false, length = 30) private String status;
    @Column(nullable = false, length = 30) private String priority;
    @Column(name = "case_type", nullable = false, length = 40) private String caseType;
    @Column(name = "expert_type", nullable = false, length = 50) private String expertType;
    @Column(name = "expert_id", length = 120) private String expertId;
    @Column(length = 120) private String owner;
    @Column(length = 2000) private String notes;
    @Column(length = 2000) private String resolution;
    @Column(name = "recommendation", length = 2000) private String recommendation;
    @Column(name = "report_id", length = 36) private String reportId;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @PrePersist void createDates() { createdAt = createdAt == null ? LocalDateTime.now() : createdAt; updatedAt = createdAt; }
    @PreUpdate void updateDate() { updatedAt = LocalDateTime.now(); }
    public String getId(){return id;} public void setId(String v){id=v;}
    public Transaction getTransaction(){return transaction;} public void setTransaction(Transaction v){transaction=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getPriority(){return priority;} public void setPriority(String v){priority=v;}
    public String getCaseType(){return caseType;} public void setCaseType(String v){caseType=v;}
    public String getExpertType(){return expertType;} public void setExpertType(String v){expertType=v;}
    public String getExpertId(){return expertId;} public void setExpertId(String v){expertId=v;}
    public String getOwner(){return owner;} public void setOwner(String v){owner=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
    public String getResolution(){return resolution;} public void setResolution(String v){resolution=v;}
    public String getRecommendation(){return recommendation;} public void setRecommendation(String v){recommendation=v;}
    public String getReportId(){return reportId;} public void setReportId(String v){reportId=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
