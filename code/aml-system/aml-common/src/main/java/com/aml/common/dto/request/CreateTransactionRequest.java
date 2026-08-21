package com.aml.common.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateTransactionRequest {


    @JsonProperty("transaction_id")
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @JsonProperty("sender_account")
    @NotBlank(message = "Sender account ID is required")
    private String senderAccountId;
    
    @JsonProperty("receiver_account")
    @NotBlank(message = "Receiver account ID is required")
    private String receiverAccountId;

    @JsonProperty("amount")
    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Amount must be greater than zero"
    )
    private BigDecimal amount;


    @JsonProperty("payment_currency")
    @NotBlank(message = "Payment currency is required")
    private String paymentCurrency;

    @JsonProperty("received_currency")
    @NotBlank(message = "Received currency is required")
    private String receivedCurrency;

    @JsonProperty("sender_bank_location")
    @NotBlank(message = "Sender bank location is required")
    private String senderBankLocation;

    @JsonProperty("receiver_bank_location")
    @NotBlank(message = "Receiver bank location is required")
    private String receiverBankLocation;

    @JsonProperty("payment_type")
    @NotBlank(message = "Payment type is required")
    private String paymentType;

    @JsonProperty("transaction_datetime")
    @NotNull(message = "Transaction datetime is required")
    private OffsetDateTime transactionDatetime;

    @JsonProperty("use_gemini")
    private Boolean useGemini;

    @JsonProperty("sender_history")
    private FastApiRequest.History senderHistory;

    @JsonProperty("receiver_history")
    private FastApiRequest.History receiverHistory;

    public CreateTransactionRequest() {
    }

    public CreateTransactionRequest(
            String transactionId,
            String senderAccountId,
            String receiverAccountId,
            BigDecimal amount,
            String paymentCurrency,
            String receivedCurrency,
            String senderBankLocation,
            String receiverBankLocation,
            String paymentType,
            OffsetDateTime transactionDatetime,
            Boolean useGemini,
            FastApiRequest.History senderHistory,
            FastApiRequest.History receiverHistory
    ) {
        this.transactionId = transactionId;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.paymentCurrency = paymentCurrency;
        this.receivedCurrency = receivedCurrency;
        this.senderBankLocation = senderBankLocation;
        this.receiverBankLocation = receiverBankLocation;
        this.paymentType = paymentType;
        this.transactionDatetime = transactionDatetime;
        this.useGemini = useGemini;
        this.senderHistory = senderHistory;
        this.receiverHistory = receiverHistory;
    }

    public CreateTransactionRequest(
            String transactionId,
            String senderAccountId,
            String receiverAccountId,
            BigDecimal amount,
            String paymentCurrency,
            String receivedCurrency,
            String senderBankLocation,
            String receiverBankLocation,
            String paymentType,
            OffsetDateTime transactionDatetime,
            Boolean useGemini
    ) {
        this(
                transactionId,
                senderAccountId,
                receiverAccountId,
                amount,
                paymentCurrency,
                receivedCurrency,
                senderBankLocation,
                receiverBankLocation,
                paymentType,
                transactionDatetime,
                useGemini,
                null,
                null
        );
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSenderAccountId() {
        return senderAccountId;
    }

    public void setSenderAccountId(String senderAccountId) {
        this.senderAccountId = senderAccountId;
    }

    public String getReceiverAccountId() {
        return receiverAccountId;
    }

    public void setReceiverAccountId(String receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(String paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public String getReceivedCurrency() {
        return receivedCurrency;
    }

    public void setReceivedCurrency(String receivedCurrency) {
        this.receivedCurrency = receivedCurrency;
    }

    public String getSenderBankLocation() {
        return senderBankLocation;
    }

    public void setSenderBankLocation(String senderBankLocation) {
        this.senderBankLocation = senderBankLocation;
    }

    public String getReceiverBankLocation() {
        return receiverBankLocation;
    }

    public void setReceiverBankLocation(String receiverBankLocation) {
        this.receiverBankLocation = receiverBankLocation;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public OffsetDateTime getTransactionDatetime() {
        return transactionDatetime;
    }

    public void setTransactionDatetime(
            OffsetDateTime transactionDatetime
    ) {
        this.transactionDatetime = transactionDatetime;
    }

    public Boolean getUseGemini() {
        return useGemini;
    }

    public void setUseGemini(Boolean useGemini) {
        this.useGemini = useGemini;
    }

    public FastApiRequest.History getSenderHistory() {
        return senderHistory;
    }

    public void setSenderHistory(FastApiRequest.History senderHistory) {
        this.senderHistory = senderHistory;
    }

    public FastApiRequest.History getReceiverHistory() {
        return receiverHistory;
    }

    public void setReceiverHistory(FastApiRequest.History receiverHistory) {
        this.receiverHistory = receiverHistory;
    }
}
