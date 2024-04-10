package com.purchase.transaction.service;

import com.purchase.transaction.client.ExchangeRateApiClient;
import com.purchase.transaction.client.response.ExchangeRateResponse;
import com.purchase.transaction.entity.ExchangeRate;
import com.purchase.transaction.entity.ExchangeRateFilter;
import com.purchase.transaction.exception.ExchangeRateNotFoundException;
import com.purchase.transaction.repository.ExchangeRateFilterRepository;
import com.purchase.transaction.repository.ExchangeRateRepository;
import com.purchase.transaction.repository.PartitionJdbc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateApiClient exchangeRateApiClient;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private ExchangeRateFilterRepository exchangeRateFilterRepository;

    private ExchangeRateService exchangeRateService;

    @Mock
    private PartitionJdbc partitionJdbc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        exchangeRateService = new ExchangeRateService(exchangeRateApiClient, exchangeRateRepository, exchangeRateFilterRepository, partitionJdbc);
    }

    @Test
    void testFindLatestExchangeRate_FoundInRepository() throws Exception {
        // Mock data
        String currency = "USD";
        LocalDate effectiveDate = LocalDate.now();
        ExchangeRate exchangeRate = ExchangeRate.builder()
                .currency(currency)
                .effectiveDate(effectiveDate)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .build();

        // Mock repository response
        when(exchangeRateRepository.findFirstByCurrencyAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(currency, effectiveDate))
                .thenReturn(Optional.of(exchangeRate));

        // Call service method
        ExchangeRate result = exchangeRateService.findLatestExchangeRate(currency, effectiveDate);

        // Verify
        assertEquals(exchangeRate, result);
        verify(exchangeRateApiClient, never()).findAllByFilter(any());
        verify(exchangeRateFilterRepository, never()).findMaxLastEffectiveDateForAllWithoutError();
        verify(exchangeRateFilterRepository, never()).save(any());
        verify(exchangeRateRepository, never()).saveAll(any());
    }

    @Test
    void testFindLatestExchangeRate_NotFoundInRepository_FoundInAPI() throws Exception {
        // Mock data
        String currency = "USD";
        LocalDate effectiveDateEnd = LocalDate.now();
        ExchangeRate exchangeRate = ExchangeRate.builder()
                .currency(currency)
                .effectiveDate(effectiveDateEnd)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .build();

        // Mock repository response
        when(exchangeRateRepository.findFirstByCurrencyAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(currency, effectiveDateEnd))
                .thenReturn(Optional.empty());
        when(exchangeRateFilterRepository.save(any()))
                .thenAnswer(new Answer<ExchangeRateFilter>() {
                    public ExchangeRateFilter answer(InvocationOnMock invocation) {
                        ExchangeRateFilter exchangeRateFilter = invocation.getArgument(0);
                        exchangeRateFilter.setId(1L);
                        return exchangeRateFilter;
                    }
                });

        // Mock API response
        ExchangeRateResponse.ExchangeRateData responseData = ExchangeRateResponse.ExchangeRateData.builder()
                .recordDate(LocalDate.now())
                .effectiveDate(effectiveDateEnd)
                .currency(currency)
                .exchangeRate(BigDecimal.valueOf(1.0))
                .build();
        List<ExchangeRateResponse.ExchangeRateData> responseDataList = Collections.singletonList(responseData);
        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .data(responseDataList)
                .build();

        when(exchangeRateApiClient.findAllByFilter(any())).thenReturn(response);

        when(exchangeRateRepository
                .findFirstByCurrencyAndEffectiveDateGreaterThanEqualAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc
                        (any(), any(), any())
        ).thenReturn(Optional.of(exchangeRate));

        // Call service method
        ExchangeRate result = exchangeRateService.findLatestExchangeRate(currency, effectiveDateEnd);

        // Verify
        assertEquals(exchangeRate, result);
        verify(exchangeRateApiClient, times(1)).findAllByFilter(any());
        verify(exchangeRateFilterRepository, times(2)).save(any());
        verify(exchangeRateRepository, times(1)).saveAll(any());
    }

    @Test
    void testFindLatestExchangeRate_NotFoundInRepository_NotFoundInAPI() throws Exception {
        // Mock data
        String currency = "USD";
        LocalDate effectiveDateEnd = LocalDate.now();

        // Mock repository response
        when(exchangeRateRepository.findFirstByCurrencyAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(currency, effectiveDateEnd))
                .thenReturn(Optional.empty());

        when(exchangeRateFilterRepository.save(any()))
                .thenAnswer(new Answer<ExchangeRateFilter>() {
                    public ExchangeRateFilter answer(InvocationOnMock invocation) {
                        ExchangeRateFilter exchangeRateFilter = invocation.getArgument(0);
                        exchangeRateFilter.setId(1L);
                        return exchangeRateFilter;
                    }
                });

        // Mock API response
        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .data(Collections.emptyList())
                .build();

        when(exchangeRateApiClient.findAllByFilter(any())).thenReturn(response);

        // Call service method and verify exception
        Exception exception = assertThrows(ExchangeRateNotFoundException.class, () -> {
            exchangeRateService.findLatestExchangeRate(currency, effectiveDateEnd);
        });

        // Verify
        assertEquals("Could not find the latest exchange rate for currency USD", exception.getMessage());
        verify(exchangeRateApiClient, times(1)).findAllByFilter(any());
        verify(exchangeRateRepository, never()).saveAll(any());
    }

}
