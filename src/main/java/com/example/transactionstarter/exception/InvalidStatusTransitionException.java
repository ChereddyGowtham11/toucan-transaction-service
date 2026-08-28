package com.example.transactionstarter.exception;

import com.example.transactionstarter.model.TransactionStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(TransactionStatus current, TransactionStatus requested) {
        super("Cannot change status from " + current + " to " + requested);
    }
}
