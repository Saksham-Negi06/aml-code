package com.aml.common.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @Column(length = 36)
    private String id;

    @OneToMany(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            orphanRemoval = false
    )
    private List<Account> accounts = new ArrayList<>();

    @Column(name = "account_holder_type", length = 50)
    private String accountHolderType;

    @Column(name = "kyc_verification_status", length = 50)
    private String kycVerificationStatus;

    @Column(name = "risk_country_flag", nullable = false)
    private Boolean riskCountryFlag;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Customer() {
    }

    public Customer(
            String id,
            List<Account> accounts,
            String accountHolderType,
            String kycVerificationStatus,
            Boolean riskCountryFlag,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.accounts = accounts;
        this.accountHolderType = accountHolderType;
        this.kycVerificationStatus = kycVerificationStatus;
        this.riskCountryFlag = riskCountryFlag;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (riskCountryFlag == null) {
            riskCountryFlag = false;
        }
    }

    public void addAccount(Account account) {
        accounts.add(account);
        account.setCustomer(this);
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
        account.setCustomer(null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public String getAccountHolderType() {
        return accountHolderType;
    }

    public void setAccountHolderType(String accountHolderType) {
        this.accountHolderType = accountHolderType;
    }

    public String getKycVerificationStatus() {
        return kycVerificationStatus;
    }

    public void setKycVerificationStatus(String kycVerificationStatus) {
        this.kycVerificationStatus = kycVerificationStatus;
    }

    public Boolean getRiskCountryFlag() {
        return riskCountryFlag;
    }

    public void setRiskCountryFlag(Boolean riskCountryFlag) {
        this.riskCountryFlag = riskCountryFlag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}