package com.aml.common.dto.request;
import jakarta.validation.constraints.NotBlank;
public class CreateInvestigationCaseRequest {
    @NotBlank private String transactionId; private String owner; private String priority;
    public String getTransactionId(){return transactionId;} public void setTransactionId(String v){transactionId=v;}
    public String getOwner(){return owner;} public void setOwner(String v){owner=v;}
    public String getPriority(){return priority;} public void setPriority(String v){priority=v;}
}
