package com.example.transactionstarter.dto;

import com.example.transactionstarter.model.Currency;
import com.example.transactionstarter.model.TransactionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Deliberately has no status field: every new transaction starts as PENDING,
 * set by the service. Clients cannot create a transaction in another state.
 */
public record CreateTransactionRequest(

        @NotBlank(message = "transactionId must not be blank")
        String transactionId,

        @NotBlank(message = "customerId must not be blank")
        String customerId,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than 0")
        @DecimalMax(value = "100000", message = "amount must not exceed 100000")
        BigDecimal amount,

        @NotNull(message = "currency is required")
        Currency currency,

        @NotNull(message = "type is required")
        TransactionType type
) {
}
