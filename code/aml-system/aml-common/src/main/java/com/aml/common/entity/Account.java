package com.aml.common.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_accounts_customer")
    )
    private Customer customer;

    @Column(name = "account_hash", nullable = false, unique = true, length = 64)
    private String accountHash;

    @Column(name = "account_type", length = 50)
    private String accountType;

    @Column(name = "bank_location", length = 100)
    private String bankLocation;

    @Column(name = "account_risk_score", precision = 10, scale = 4)
    private BigDecimal accountRiskScore;

    @Column(name = "avg_transaction_amount", precision = 19, scale = 4)
    private BigDecimal avgTransactionAmount;

    @Column(name = "avg_transactions_per_month")
    private Integer avgTransactionsPerMonth;

    @Column(name = "account_age_days")
    private Integer accountAgeDays;

    @Column(name = "is_flagged", nullable = false)
    private Boolean isFlagged;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Account() {
    }

    public Account(
            String id,
            Customer customer,
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
        this.customer = customer;
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

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (isFlagged == null) {
            isFlagged = false;
        }

        if (accountRiskScore == null) {
            accountRiskScore = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public void setIsFlagged(Boolean flagged) {
        isFlagged = flagged;
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
