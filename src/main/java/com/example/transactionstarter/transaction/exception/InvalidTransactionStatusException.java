package com.example.transactionstarter.transaction.exception;

public class InvalidTransactionStatusException extends RuntimeException {

    public InvalidTransactionStatusException(String message) {
        super(message);
    }
}