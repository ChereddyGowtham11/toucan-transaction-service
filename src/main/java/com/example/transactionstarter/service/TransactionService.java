package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.exception.DuplicateTransactionException;
import com.example.transactionstarter.exception.TransactionNotFoundException;
import com.example.transactionstarter.model.Transaction;
import com.example.transactionstarter.model.TransactionStatus;
import com.example.transactionstarter.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

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
}
