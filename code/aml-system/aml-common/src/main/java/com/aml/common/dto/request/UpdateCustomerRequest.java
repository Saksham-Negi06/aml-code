package com.aml.common.dto.request;

public class UpdateCustomerRequest {

    private String accountHolderType;
    private String kycVerificationStatus;
    private Boolean riskCountryFlag;

    public UpdateCustomerRequest() {
    }

    public UpdateCustomerRequest(
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