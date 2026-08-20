package com.aml.common.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class CustomerResponse {

    private String id;
    private List<String> accountIds;
    private String accountHolderType;
    private String kycVerificationStatus;
    private Boolean riskCountryFlag;
    private LocalDateTime createdAt;

    public CustomerResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<String> accountIds) {
        this.accountIds = accountIds;
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