package com.example.transactionstarter.repository;

import com.example.transactionstarter.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
