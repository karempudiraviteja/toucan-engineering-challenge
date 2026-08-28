package com.example.transactionstarter.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.transactionstarter.transaction.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

}
