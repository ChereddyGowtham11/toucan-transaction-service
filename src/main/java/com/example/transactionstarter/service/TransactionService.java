package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.exception.DuplicateTransactionException;
import com.example.transactionstarter.exception.InvalidStatusTransitionException;
import com.example.transactionstarter.exception.TransactionNotFoundException;
import com.example.transactionstarter.model.Transaction;
import com.example.transactionstarter.model.TransactionStatus;
import com.example.transactionstarter.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction createTransaction(CreateTransactionRequest request) {
        if (repository.existsById(request.transactionId())) {
            throw new DuplicateTransactionException(request.transactionId());
        }
        Transaction transaction = new Transaction(
                request.transactionId(),
                request.customerId(),
                request.amount(),
                request.currency(),
                request.type(),
                TransactionStatus.PENDING,
                Instant.now());
        return repository.save(transaction);
    }

    public Transaction getTransaction(String transactionId) {
        return repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    /**
     * The allowed transitions are defined on TransactionStatus: only
     * PENDING -> COMPLETED and PENDING -> FAILED are legal. Inside the
     * transaction the loaded entity is managed, so changing the status is
     * flushed to the database on commit without an explicit save call.
     */
    @Transactional
    public Transaction updateStatus(String transactionId, TransactionStatus newStatus) {
        Transaction transaction = getTransaction(transactionId);
        if (!transaction.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(transaction.getStatus(), newStatus);
        }
        transaction.setStatus(newStatus);
        return transaction;
    }

    /**
     * A customer with no transactions gets an empty list, not an error:
     * the collection legitimately exists and is empty, and this service
     * does not own customer records so it cannot tell an unknown customer
     * apart from one who has not transacted yet.
     */
    public List<Transaction> getCustomerTransactions(String customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
}
