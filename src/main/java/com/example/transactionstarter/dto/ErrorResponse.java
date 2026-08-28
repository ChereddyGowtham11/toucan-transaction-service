package com.example.transactionstarter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Single error shape for every failed request. fieldErrors is only present
 * for validation failures.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, String> fieldErrors
) {

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(Instant.now(), status, message, null);
    }

    public static ErrorResponse of(int status, String message, Map<String, String> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, message, fieldErrors);
    }
}
