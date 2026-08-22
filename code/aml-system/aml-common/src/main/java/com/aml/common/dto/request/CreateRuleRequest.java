package com.aml.common.dto.request;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public class CreateRuleRequest {
    @NotBlank(message = "Name is required") private String name;
    @NotNull(message = "Amount threshold is required")
    @DecimalMin(value = "0.01", message = "Amount threshold must be greater than zero")
    private BigDecimal amountThreshold;
    @NotBlank(message = "Severity is required") private String severity;
    public String getName(){return name;} public void setName(String v){name=v;}
    public BigDecimal getAmountThreshold(){return amountThreshold;} public void setAmountThreshold(BigDecimal v){amountThreshold=v;}
    public String getSeverity(){return severity;} public void setSeverity(String v){severity=v;}
}
