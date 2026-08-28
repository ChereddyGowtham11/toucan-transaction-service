package com.example.transactionstarter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Transaction {

    /**
     * Client-supplied identifier, used directly as the primary key: it never
     * changes, every lookup is by this value, and the database then enforces
     * uniqueness as well as the service-level duplicate check.
     */
    @Id
    @Column(length = 64)
    private String transactionId;

    @Column(nullable = false, length = 64)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransactionStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected Transaction() {
        // required by JPA
    }

    public Transaction(String transactionId, String customerId, BigDecimal amount,
                       Currency currency, TransactionType type, TransactionStatus status,
                       Instant createdAt) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Status is the only mutable field; all other fields are fixed at creation.
     */
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
