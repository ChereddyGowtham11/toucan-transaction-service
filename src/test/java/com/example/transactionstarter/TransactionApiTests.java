package com.example.transactionstarter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack API tests: each request runs through the real controller,
 * service, repository and H2 database. @Transactional rolls the database
 * back after every test so tests cannot affect each other.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransactionApiTests {

    @Autowired
    private MockMvc mockMvc;

    private String transactionJson(String transactionId, String customerId,
                                   String amount, String currency, String type) {
        return """
                {
                  "transactionId": %s,
                  "customerId": %s,
                  "amount": %s,
                  "currency": %s,
                  "type": %s
                }
                """.formatted(
                jsonString(transactionId), jsonString(customerId),
                amount, jsonString(currency), jsonString(type));
    }

    private String jsonString(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    private ResultActions postTransaction(String body) throws Exception {
        return mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    // --- Create transaction ---

    @Test
    void createTransaction_returnsCreatedTransactionWithPendingStatus() throws Exception {
        postTransaction(transactionJson("TXN-1", "CUST-1", "250.50", "INR", "PAYMENT"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN-1"))
                .andExpect(jsonPath("$.customerId").value("CUST-1"))
                .andExpect(jsonPath("$.amount").value(250.50))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.type").value("PAYMENT"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createTransaction_blankCustomerId_isRejected() throws Exception {
        postTransaction(transactionJson("TXN-2", "  ", "100", "USD", "PAYMENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.customerId").value("customerId must not be blank"));
    }

    @Test
    void createTransaction_zeroOrNegativeAmount_isRejected() throws Exception {
        postTransaction(transactionJson("TXN-3", "CUST-1", "0", "USD", "PAYMENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").value("amount must be greater than 0"));

        postTransaction(transactionJson("TXN-3", "CUST-1", "-10", "USD", "PAYMENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").value("amount must be greater than 0"));
    }

    @Test
    void createTransaction_amountAboveMaximum_isRejected() throws Exception {
        postTransaction(transactionJson("TXN-4", "CUST-1", "100000.01", "EUR", "TRANSFER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").value("amount must not exceed 100000"));
    }

    @Test
    void createTransaction_unsupportedCurrency_isRejected() throws Exception {
        postTransaction(transactionJson("TXN-5", "CUST-1", "100", "GBP", "PAYMENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Invalid value 'GBP' for 'currency'. Allowed values: [INR, USD, EUR]"));
    }

    @Test
    void createTransaction_duplicateTransactionId_isRejected() throws Exception {
        postTransaction(transactionJson("TXN-6", "CUST-1", "100", "INR", "PAYMENT"))
                .andExpect(status().isCreated());

        postTransaction(transactionJson("TXN-6", "CUST-2", "999", "USD", "REFUND"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Transaction 'TXN-6' already exists"));
    }

    // --- Get transaction ---

    @Test
    void getTransaction_existingId_returnsTransaction() throws Exception {
        postTransaction(transactionJson("TXN-10", "CUST-1", "42.00", "EUR", "REFUND"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/transactions/TXN-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-10"))
                .andExpect(jsonPath("$.type").value("REFUND"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getTransaction_unknownId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/NO-SUCH-TXN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction 'NO-SUCH-TXN' not found"));
    }

    // --- Update transaction status ---

    private ResultActions patchStatus(String transactionId, String statusJson) throws Exception {
        return mockMvc.perform(patch("/api/transactions/" + transactionId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": " + statusJson + "}"));
    }

    @Test
    void updateStatus_pendingToCompleted_succeeds() throws Exception {
        postTransaction(transactionJson("TXN-20", "CUST-1", "10", "INR", "PAYMENT"))
                .andExpect(status().isCreated());

        patchStatus("TXN-20", "\"COMPLETED\"")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // the change is persisted, not just echoed back
        mockMvc.perform(get("/api/transactions/TXN-20"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void updateStatus_fromTerminalStatus_isRejected() throws Exception {
        postTransaction(transactionJson("TXN-21", "CUST-1", "10", "INR", "PAYMENT"))
                .andExpect(status().isCreated());
        patchStatus("TXN-21", "\"COMPLETED\"").andExpect(status().isOk());

        patchStatus("TXN-21", "\"PENDING\"")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot change status from COMPLETED to PENDING"));
    }

    @Test
    void updateStatus_unknownTransaction_returnsNotFound() throws Exception {
        patchStatus("NO-SUCH-TXN", "\"COMPLETED\"")
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_unknownStatusValue_isRejected() throws Exception {
        postTransaction(transactionJson("TXN-22", "CUST-1", "10", "INR", "PAYMENT"))
                .andExpect(status().isCreated());

        patchStatus("TXN-22", "\"CANCELLED\"")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Invalid value 'CANCELLED' for 'status'. Allowed values: [PENDING, COMPLETED, FAILED]"));
    }
}
