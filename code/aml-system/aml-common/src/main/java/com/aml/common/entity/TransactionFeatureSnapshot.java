package com.aml.common.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transaction_feature_snapshots",
        indexes = {
                @Index(
                        name = "idx_feature_snapshot_transaction",
                        columnList = "transaction_id"
                )
        }
)
public class TransactionFeatureSnapshot {

    @Id
    @Column(length = 36)
    private String id;

    /*
     * A transaction can have multiple feature snapshots if the AML
     * model is run multiple times.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    // ------------------------------------------------------------
    // Sender history
    // ------------------------------------------------------------

    @Column(name = "sender_transaction_count")
    private Integer senderTransactionCount;

    @Column(name = "sender_average_amount", precision = 19, scale = 4)
    private BigDecimal senderAverageAmount;

    @Column(name = "sender_amount_stddev", precision = 19, scale = 4)
    private BigDecimal senderAmountStddev;

    @Column(name = "sender_minimum_amount", precision = 19, scale = 4)
    private BigDecimal senderMinimumAmount;

    @Column(name = "sender_maximum_amount", precision = 19, scale = 4)
    private BigDecimal senderMaximumAmount;

    @Column(name = "sender_unique_counterparties")
    private Integer senderUniqueCounterparties;

    // ------------------------------------------------------------
    // Receiver history
    // ------------------------------------------------------------

    @Column(name = "receiver_transaction_count")
    private Integer receiverTransactionCount;

    @Column(name = "receiver_average_amount", precision = 19, scale = 4)
    private BigDecimal receiverAverageAmount;

    @Column(name = "receiver_amount_stddev", precision = 19, scale = 4)
    private BigDecimal receiverAmountStddev;

    @Column(name = "receiver_minimum_amount", precision = 19, scale = 4)
    private BigDecimal receiverMinimumAmount;

    @Column(name = "receiver_maximum_amount", precision = 19, scale = 4)
    private BigDecimal receiverMaximumAmount;

    @Column(name = "receiver_unique_counterparties")
    private Integer receiverUniqueCounterparties;

    // ------------------------------------------------------------
    // FastAPI/model options
    // ------------------------------------------------------------

    @Column(name = "use_gemini", nullable = false)
    private Boolean useGemini;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TransactionFeatureSnapshot() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (useGemini == null) {
            useGemini = false;
        }
    }

    // ------------------------------------------------------------
    // Getters / Setters
    // ------------------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Integer getSenderTransactionCount() {
        return senderTransactionCount;
    }

    public void setSenderTransactionCount(Integer senderTransactionCount) {
        this.senderTransactionCount = senderTransactionCount;
    }

    public BigDecimal getSenderAverageAmount() {
        return senderAverageAmount;
    }

    public void setSenderAverageAmount(BigDecimal senderAverageAmount) {
        this.senderAverageAmount = senderAverageAmount;
    }

    public BigDecimal getSenderAmountStddev() {
        return senderAmountStddev;
    }

    public void setSenderAmountStddev(BigDecimal senderAmountStddev) {
        this.senderAmountStddev = senderAmountStddev;
    }

    public BigDecimal getSenderMinimumAmount() {
        return senderMinimumAmount;
    }

    public void setSenderMinimumAmount(BigDecimal senderMinimumAmount) {
        this.senderMinimumAmount = senderMinimumAmount;
    }

    public BigDecimal getSenderMaximumAmount() {
        return senderMaximumAmount;
    }

    public void setSenderMaximumAmount(BigDecimal senderMaximumAmount) {
        this.senderMaximumAmount = senderMaximumAmount;
    }

    public Integer getSenderUniqueCounterparties() {
        return senderUniqueCounterparties;
    }

    public void setSenderUniqueCounterparties(
            Integer senderUniqueCounterparties
    ) {
        this.senderUniqueCounterparties = senderUniqueCounterparties;
    }

    public Integer getReceiverTransactionCount() {
        return receiverTransactionCount;
    }

    public void setReceiverTransactionCount(Integer receiverTransactionCount) {
        this.receiverTransactionCount = receiverTransactionCount;
    }

    public BigDecimal getReceiverAverageAmount() {
        return receiverAverageAmount;
    }

    public void setReceiverAverageAmount(BigDecimal receiverAverageAmount) {
        this.receiverAverageAmount = receiverAverageAmount;
    }

    public BigDecimal getReceiverAmountStddev() {
        return receiverAmountStddev;
    }

    public void setReceiverAmountStddev(BigDecimal receiverAmountStddev) {
        this.receiverAmountStddev = receiverAmountStddev;
    }

    public BigDecimal getReceiverMinimumAmount() {
        return receiverMinimumAmount;
    }

    public void setReceiverMinimumAmount(BigDecimal receiverMinimumAmount) {
        this.receiverMinimumAmount = receiverMinimumAmount;
    }

    public BigDecimal getReceiverMaximumAmount() {
        return receiverMaximumAmount;
    }

    public void setReceiverMaximumAmount(BigDecimal receiverMaximumAmount) {
        this.receiverMaximumAmount = receiverMaximumAmount;
    }

    public Integer getReceiverUniqueCounterparties() {
        return receiverUniqueCounterparties;
    }

    public void setReceiverUniqueCounterparties(
            Integer receiverUniqueCounterparties
    ) {
        this.receiverUniqueCounterparties = receiverUniqueCounterparties;
    }

    public Boolean getUseGemini() {
        return useGemini;
    }

    public void setUseGemini(Boolean useGemini) {
        this.useGemini = useGemini;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}