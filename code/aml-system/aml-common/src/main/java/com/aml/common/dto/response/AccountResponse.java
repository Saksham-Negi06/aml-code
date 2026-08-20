package com.aml.common.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {

    private String id;

    private String customerId;

    private String accountHash;

    private String accountType;

    private String bankLocation;

    private BigDecimal accountRiskScore;

    private BigDecimal avgTransactionAmount;

    private Integer avgTransactionsPerMonth;

    private Integer accountAgeDays;

    private Boolean isFlagged;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public AccountResponse() {
    }

    public AccountResponse(
            String id,
            String customerId,
            String accountHash,
            String accountType,
            String bankLocation,
            BigDecimal accountRiskScore,
            BigDecimal avgTransactionAmount,
            Integer avgTransactionsPerMonth,
            Integer accountAgeDays,
            Boolean isFlagged,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.accountHash = accountHash;
        this.accountType = accountType;
        this.bankLocation = bankLocation;
        this.accountRiskScore = accountRiskScore;
        this.avgTransactionAmount = avgTransactionAmount;
        this.avgTransactionsPerMonth = avgTransactionsPerMonth;
        this.accountAgeDays = accountAgeDays;
        this.isFlagged = isFlagged;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAccountHash() {
        return accountHash;
    }

    public void setAccountHash(String accountHash) {
        this.accountHash = accountHash;
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

    public void setAvgTransactionsPerMonth(
            Integer avgTransactionsPerMonth
    ) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}