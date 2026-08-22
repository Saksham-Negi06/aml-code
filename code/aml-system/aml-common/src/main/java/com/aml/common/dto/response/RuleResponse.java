package com.aml.common.dto.response;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class RuleResponse {
    private String id, name, severity, status;
    private BigDecimal amountThreshold;
    private LocalDateTime createdAt, updatedAt;
    public String getId(){return id;} 
    public void setId(String v){id=v;}
    public String getName(){return name;} 
    public void setName(String v){name=v;}
    public BigDecimal getAmountThreshold(){return amountThreshold;} 
    public void setAmountThreshold(BigDecimal v){amountThreshold=v;}
    public String getSeverity(){return severity;} 
    public void setSeverity(String v){severity=v;}
    public String getStatus(){return status;} 
    public void setStatus(String v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
     public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
     public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
