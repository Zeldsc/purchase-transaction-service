package com.purchase.transaction.service;

import com.purchase.transaction.api.payload.TransactionPayload;
import com.purchase.transaction.api.response.TransactionResponse;
import com.purchase.transaction.entity.Transaction;
import com.purchase.transaction.exception.TransactionNotFoundException;
import com.purchase.transaction.repository.PartitionJdbc;
import com.purchase.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

import com.purchase.transaction.entity.ExchangeRate;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private PartitionJdbc partitionJdbc;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void saveTransaction_ValidPayload_Success() {
        // Given
        TransactionPayload payload = TransactionPayload.builder()
                .description("Test transaction")
                .transactionDate(LocalDate.now())
                .purchaseAmount(BigDecimal.valueOf(100))
                .build();

        // When
        transactionService.saveTransaction(payload);

        // Then
        verify(partitionJdbc, times(1)).createPartitionForTransactionIfNotExists(any(LocalDate.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void getTransactionById_InvalidId_ThrowsException() {
        // Given
        Long invalidId = 1L;

        // When
        when(transactionRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Then
        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.getTransactionById(invalidId, "USD");
        });
    }

    @Test
    public void getTransactionById_ValidIdAndCurrency_Success() throws Exception {
        // Given
        Long validId = 1L;
        String currency = "USD";
        Transaction transaction = Transaction.builder()
                .id(validId)
                .description("Test transaction")
                .transactionDate(LocalDate.now())
                .purchaseAmount(BigDecimal.valueOf(100))
                .build();
        ExchangeRate exchangeRate = ExchangeRate.builder()
                .exchangeRate(BigDecimal.valueOf(1.2)) // Exchange rate for 1 USD to the given currency
                .build();

        // When
        when(transactionRepository.findById(validId)).thenReturn(Optional.of(transaction));
        when(exchangeRateService.findLatestExchangeRate(currency, transaction.getTransactionDate())).thenReturn(exchangeRate);
        TransactionResponse response = transactionService.getTransactionById(validId, currency);

        // Then
        assertNotNull(response);
        assertEquals(validId, response.getId());
        assertEquals(transaction.getDescription(), response.getDescription());
        assertEquals(transaction.getTransactionDate(), response.getTransactionDate());
        assertEquals(transaction.getPurchaseAmount(), response.getPurchaseAmount());
        assertEquals(BigDecimal.valueOf(120).setScale(2, RoundingMode.HALF_UP), response.getConvertedPurchaseAmount());
    }
}