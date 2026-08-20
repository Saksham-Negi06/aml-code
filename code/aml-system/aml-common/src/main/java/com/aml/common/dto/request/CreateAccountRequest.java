package com.aml.common.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class CreateAccountRequest {

    @NotBlank(message = "Account ID is required")
    private String accountId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Account type is required")
    private String accountType;

    @NotBlank(message = "Bank location is required")
    private String bankLocation;

    private BigDecimal accountRiskScore;

    private BigDecimal avgTransactionAmount;

    private Integer avgTransactionsPerMonth;

    private Integer accountAgeDays;

    private Boolean isFlagged;

    public CreateAccountRequest() {
    }

    public CreateAccountRequest(
            String accountId,
            String customerId,
            String accountType,
            String bankLocation,
            BigDecimal accountRiskScore,
            BigDecimal avgTransactionAmount,
            Integer avgTransactionsPerMonth,
            Integer accountAgeDays,
            Boolean isFlagged
    ) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.accountType = accountType;
        this.bankLocation = bankLocation;
        this.accountRiskScore = accountRiskScore;
        this.avgTransactionAmount = avgTransactionAmount;
        this.avgTransactionsPerMonth = avgTransactionsPerMonth;
        this.accountAgeDays = accountAgeDays;
        this.isFlagged = isFlagged;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getBankLocation() {
        return bankLocation;
    }

    public void setBankLocation(String bankLocation) {
        this.bankLocation = bankLocation;
    }

    public BigDecimal getAccountRiskScore() {
        return accountRiskScore;
    }

    public void setAccountRiskScore(BigDecimal accountRiskScore) {
        this.accountRiskScore = accountRiskScore;
    }

    public BigDecimal getAvgTransactionAmount() {
        return avgTransactionAmount;
    }

    public void setAvgTransactionAmount(BigDecimal avgTransactionAmount) {
        this.avgTransactionAmount = avgTransactionAmount;
    }

    public Integer getAvgTransactionsPerMonth() {
        return avgTransactionsPerMonth;
    }

    public void setAvgTransactionsPerMonth(Integer avgTransactionsPerMonth) {
        this.avgTransactionsPerMonth = avgTransactionsPerMonth;
    }

    public Integer getAccountAgeDays() {
        return accountAgeDays;
    }

    public void setAccountAgeDays(Integer accountAgeDays) {
        this.accountAgeDays = accountAgeDays;
    }

    public Boolean getIsFlagged() {
        return isFlagged;
    }

    public void setIsFlagged(Boolean isFlagged) {
        this.isFlagged = isFlagged;
    }
}