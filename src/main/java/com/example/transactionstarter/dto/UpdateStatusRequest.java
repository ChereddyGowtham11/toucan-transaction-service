package com.example.transactionstarter.dto;

import com.example.transactionstarter.model.TransactionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(

        @NotNull(message = "status is required")
        TransactionStatus status
) {
}
