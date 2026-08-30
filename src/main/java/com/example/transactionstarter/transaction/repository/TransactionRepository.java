package com.example.transactionstarter.transaction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.transactionstarter.transaction.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
	
	List<Transaction> findByCustomerId(String customerId);

}
