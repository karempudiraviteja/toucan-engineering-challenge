package com.example.transactionstarter.transaction;

import com.example.transactionstarter.transaction.entity.Transaction;

import com.example.transactionstarter.transaction.entity.TransactionStatus;
import com.example.transactionstarter.transaction.entity.TransactionType;
import com.example.transactionstarter.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
    }

    @Test
    void shouldCreateTransactionSuccessfully() throws Exception {

        String request = """
                {
                    "transactionId": "TXN001",
                    "customerId": "CUS001",
                    "amount": 500.00,
                    "currency": "INR",
                    "transactionType": "PAYMENT"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId", is("TXN001")))
                .andExpect(jsonPath("$.customerId", is("CUS001")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void shouldRejectInvalidTransaction() throws Exception {

        String request = """
                {
                    "transactionId": "",
                    "customerId": "CUS002",
                    "amount": -100.00,
                    "currency": "INR",
                    "transactionType": "PAYMENT"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void shouldRejectDuplicateTransactionId() throws Exception {

        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN003");
        transaction.setCustomerId("CUS003");
        transaction.setAmount(new java.math.BigDecimal("500.00"));
        transaction.setCurrency("INR");
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setStatus(TransactionStatus.PENDING);

        transactionRepository.save(transaction);

        String request = """
                {
                    "transactionId": "TXN003",
                    "customerId": "CUS004",
                    "amount": 700.00,
                    "currency": "INR",
                    "transactionType": "PAYMENT"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("Transaction already exists: TXN003")));
    }
    @Test
    void shouldReturnNotFoundForNonExistingTransaction() throws Exception {

        mockMvc.perform(get("/api/transactions/TXN999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Transaction not found: TXN999")));
    }
    
    @Test
    void shouldUpdateTransactionStatusSuccessfully() throws Exception {

        Transaction transaction = new Transaction();

        transaction.setTransactionId("TXN004");
        transaction.setCustomerId("CUS004");
        transaction.setAmount(new java.math.BigDecimal("1000.00"));
        transaction.setCurrency("INR");
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setStatus(TransactionStatus.PENDING);

        transactionRepository.save(transaction);

        String request = """
                {
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(patch("/api/transactions/TXN004/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId", is("TXN004")))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }
    
    @Test
    void shouldRejectInvalidStatusTransition() throws Exception {

        Transaction transaction = new Transaction();

        transaction.setTransactionId("TXN005");
        transaction.setCustomerId("CUS005");
        transaction.setAmount(new java.math.BigDecimal("1000.00"));
        transaction.setCurrency("INR");
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setStatus(TransactionStatus.COMPLETED);

        transactionRepository.save(transaction);

        String request = """
                {
                    "status": "FAILED"
                }
                """;

        mockMvc.perform(patch("/api/transactions/TXN005/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message",
                        is("Transaction status cannot be changed from COMPLETED")));
    }
    
    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingTransaction() throws Exception {

        String request = """
                {
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(patch("/api/transactions/TXN999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message",
                        is("Transaction not found: TXN999")));
    }
}