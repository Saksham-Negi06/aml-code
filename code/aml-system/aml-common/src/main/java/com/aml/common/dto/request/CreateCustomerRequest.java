package com.aml.common.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateCustomerRequest {

    @NotBlank(message = "Account holder type is required")
    private String accountHolderType;

    @NotBlank(message = "KYC verification status is required")
    private String kycVerificationStatus;

    private Boolean riskCountryFlag;

    public CreateCustomerRequest() {
    }

    public CreateCustomerRequest(
            String accountHolderType,
            String kycVerificationStatus,
            Boolean riskCountryFlag
    ) {
        this.accountHolderType = accountHolderType;
        this.kycVerificationStatus = kycVerificationStatus;
        this.riskCountryFlag = riskCountryFlag;
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
}