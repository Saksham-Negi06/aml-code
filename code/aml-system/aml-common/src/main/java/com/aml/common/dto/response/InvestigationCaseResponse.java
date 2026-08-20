package com.aml.common.dto.response;
import java.time.LocalDateTime;
public class InvestigationCaseResponse {
    private String id, transactionId, customerId, status, priority, owner, resolution; private LocalDateTime createdAt, updatedAt;
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getTransactionId(){return transactionId;} public void setTransactionId(String v){transactionId=v;}
    public String getCustomerId(){return customerId;} public void setCustomerId(String v){customerId=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getPriority(){return priority;} public void setPriority(String v){priority=v;}
    public String getOwner(){return owner;} public void setOwner(String v){owner=v;}
    public String getResolution(){return resolution;} public void setResolution(String v){resolution=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
