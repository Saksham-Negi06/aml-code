package com.aml.common.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rules")
public class Rule {
    @Id @Column(length = 36) private String id;
    @Column(nullable = false, length = 120) private String name;
    @Column(name = "amount_threshold", nullable = false, precision = 18, scale = 2) private BigDecimal amountThreshold;
    @Column(nullable = false, length = 30) private String severity;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @PrePersist void createDates() { createdAt = createdAt == null ? LocalDateTime.now() : createdAt; updatedAt = createdAt; }
    @PreUpdate void updateDate() { updatedAt = LocalDateTime.now(); }
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public BigDecimal getAmountThreshold(){return amountThreshold;} public void setAmountThreshold(BigDecimal v){amountThreshold=v;}
    public String getSeverity(){return severity;} public void setSeverity(String v){severity=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
