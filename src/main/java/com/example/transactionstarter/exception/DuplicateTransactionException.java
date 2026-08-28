package com.example.transactionstarter.exception;

public class DuplicateTransactionException extends RuntimeException {

    public DuplicateTransactionException(String transactionId) {
        super("Transaction '" + transactionId + "' already exists");
    }
}
