package com.purchase.transaction.service;

import com.purchase.transaction.api.payload.TransactionPayload;
import com.purchase.transaction.api.response.TransactionResponse;
import com.purchase.transaction.entity.ExchangeRate;
import com.purchase.transaction.entity.Transaction;
import com.purchase.transaction.exception.ExchangeRateNotFoundException;
import com.purchase.transaction.exception.TransactionNotFoundException;
import com.purchase.transaction.repository.PartitionJdbc;
import com.purchase.transaction.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@AllArgsConstructor
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final PartitionJdbc partitionJdbc;

    public Transaction saveTransaction(TransactionPayload payload) {
        logger.info("Saving transaction...");
        Transaction transaction = Transaction.builder()
                .description(payload.getDescription())
                .transactionDate(payload.getTransactionDate())
                .purchaseAmount(roundWithTwoDigits(payload.getPurchaseAmount()))
                .build();

        partitionJdbc.createPartitionForTransactionIfNotExists(transaction.getTransactionDate());
        logger.info("Transaction saved successfully.");
        return transactionRepository.save(transaction);
    }

    public TransactionResponse getTransactionById(Long id, String currency) {
        logger.info("Fetching transaction by ID: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found for ID: " + id));

        ExchangeRate exchangeRate;
        try {
            logger.info("Fetching exchange rate for currency: {} and transaction date: {}", currency, transaction.getTransactionDate());
            exchangeRate = exchangeRateService.findLatestExchangeRate(currency, transaction.getTransactionDate());
        } catch (Exception e) {
            logger.error("Error fetching exchange rate: {}", e.getMessage());
            throw new ExchangeRateNotFoundException("Error fetching exchange rate: " + e.getMessage());
        }

        if (exchangeRate == null) {
            logger.error("Exchange rate not found for ID: {}", id);
            throw new ExchangeRateNotFoundException("Exchange rate not found for ID: " + id);
        }

        BigDecimal convertedAmount = convertAmount(transaction.getPurchaseAmount(), exchangeRate.getExchangeRate());

        logger.info("Transaction fetched successfully. ID: {}", id);
        return TransactionResponse.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .purchaseAmount(transaction.getPurchaseAmount())
                .convertedPurchaseAmount(roundWithTwoDigits(convertedAmount))
                .build();
    }

    private BigDecimal roundWithTwoDigits(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal convertAmount(BigDecimal amount, BigDecimal exchangeRate) {
        return roundWithTwoDigits(amount.multiply(exchangeRate));
    }
}
