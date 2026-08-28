package com.example.transactionstarter.model;

/**
 * Lifecycle of a transaction. Every transaction starts as PENDING;
 * COMPLETED and FAILED are terminal because a settled or failed
 * transaction is an immutable financial record. A reversal is modelled
 * as a new REFUND transaction, not by mutating the original.
 */
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED;

    public boolean canTransitionTo(TransactionStatus next) {
        return this == PENDING && (next == COMPLETED || next == FAILED);
    }
}
