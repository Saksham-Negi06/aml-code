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
    @Column(length = 120) private String owner;
    @Column(length = 2000) private String resolution;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @PrePersist void createDates() { createdAt = createdAt == null ? LocalDateTime.now() : createdAt; updatedAt = createdAt; }
    @PreUpdate void updateDate() { updatedAt = LocalDateTime.now(); }
    public String getId(){return id;} public void setId(String v){id=v;}
    public Transaction getTransaction(){return transaction;} public void setTransaction(Transaction v){transaction=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getPriority(){return priority;} public void setPriority(String v){priority=v;}
    public String getOwner(){return owner;} public void setOwner(String v){owner=v;}
    public String getResolution(){return resolution;} public void setResolution(String v){resolution=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
