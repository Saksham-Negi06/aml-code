package com.aml.common.dto.request;
import java.math.BigDecimal;
public class UpdateRuleRequest {
    private String name; private BigDecimal amountThreshold; private String severity; private String status;
    public String getName(){return name;} public void setName(String v){name=v;}
    public BigDecimal getAmountThreshold(){return amountThreshold;} public void setAmountThreshold(BigDecimal v){amountThreshold=v;}
    public String getSeverity(){return severity;} public void setSeverity(String v){severity=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
