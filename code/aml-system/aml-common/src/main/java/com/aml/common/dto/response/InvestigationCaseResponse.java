package com.aml.common.dto.response;
import java.time.LocalDateTime;
public class InvestigationCaseResponse {
    private String id, transactionId, customerId, status, priority, caseType, expertType, expertId, owner, notes, resolution, recommendation, reportId;
    private java.math.BigDecimal riskScore; private String riskCategory, riskExplanation; private LocalDateTime createdAt, updatedAt;
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getTransactionId(){return transactionId;} public void setTransactionId(String v){transactionId=v;}
    public String getCustomerId(){return customerId;} public void setCustomerId(String v){customerId=v;}
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
    public java.math.BigDecimal getRiskScore(){return riskScore;} public void setRiskScore(java.math.BigDecimal v){riskScore=v;}
    public String getRiskCategory(){return riskCategory;} public void setRiskCategory(String v){riskCategory=v;}
    public String getRiskExplanation(){return riskExplanation;} public void setRiskExplanation(String v){riskExplanation=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
