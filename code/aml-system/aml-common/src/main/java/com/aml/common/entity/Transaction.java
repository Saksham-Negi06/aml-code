package com.aml.common.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(
                        name = "idx_transaction_id",
                        columnList = "transaction_id"
                ),
                @Index(
                        name = "idx_transaction_datetime",
                        columnList = "transaction_datetime"
                ),
                @Index(
                        name = "idx_sender_account",
                        columnList = "sender_account_id"
                ),
                @Index(
                        name = "idx_receiver_account",
                        columnList = "receiver_account_id"
                )
        }
)
public class Transaction {

    @Id
    @Column(length = 36)
    private String id;

    @Column(
            name = "transaction_id",
            nullable = false,
            unique = true,
            length = 100
    )
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "sender_account_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_transactions_sender")
    )
    private Account senderAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "receiver_account_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_transactions_receiver")
    )
    private Account receiverAccount;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal amount;

    @Column(
            name = "payment_currency",
            nullable = false,
            length = 10
    )
    private String paymentCurrency;

    @Column(
            name = "received_currency",
            nullable = false,
            length = 10
    )
    private String receivedCurrency;

    @Column(
            name = "payment_type",
            nullable = false,
            length = 50
    )
    private String paymentType;

    @Column(name = "transaction_datetime", nullable = false)
    private OffsetDateTime transactionDatetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transaction() {
    }

    public Transaction(
            String id,
            String transactionId,
            Account senderAccount,
            Account receiverAccount,
            BigDecimal amount,
            String paymentCurrency,
            String receivedCurrency,
            String paymentType,
            OffsetDateTime transactionDatetime,
            TransactionStatus status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.transactionId = transactionId;
        this.senderAccount = senderAccount;
        this.receiverAccount = receiverAccount;
        this.amount = amount;
        this.paymentCurrency = paymentCurrency;
        this.receivedCurrency = receivedCurrency;
        this.paymentType = paymentType;
        this.transactionDatetime = transactionDatetime;
        this.status = status;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Account getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(Account senderAccount) {
        this.senderAccount = senderAccount;
    }

    public Account getReceiverAccount() {
        return receiverAccount;
    }

    public void setReceiverAccount(Account receiverAccount) {
        this.receiverAccount = receiverAccount;
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

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public OffsetDateTime getTransactionDatetime() {
        return transactionDatetime;
    }

    public void setTransactionDatetime(OffsetDateTime transactionDatetime) {
        this.transactionDatetime = transactionDatetime;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
