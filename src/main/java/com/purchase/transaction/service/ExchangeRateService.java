package com.purchase.transaction.service;

import com.purchase.transaction.client.ExchangeRateApiClient;
import com.purchase.transaction.client.filter.ExchangeRateApiFilter;
import com.purchase.transaction.client.filter.SortOrder;
import com.purchase.transaction.client.response.ExchangeRateResponse;
import com.purchase.transaction.entity.ExchangeRate;
import com.purchase.transaction.entity.ExchangeRateFilter;
import com.purchase.transaction.exception.DataProcessingException;
import com.purchase.transaction.exception.ExchangeRateNotFoundException;
import com.purchase.transaction.repository.ExchangeRateFilterRepository;
import com.purchase.transaction.repository.ExchangeRateRepository;
import javax.transaction.Transactional;

import com.purchase.transaction.repository.PartitionJdbc;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ExchangeRateService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    private final ExchangeRateApiClient exchangeRateApiClient;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateFilterRepository exchangeRateFilterRepository;
    private final PartitionJdbc partitionJdbc;

    public ExchangeRate findLatestExchangeRate(String currency, LocalDate effectiveDateEnd) throws ExchangeRateNotFoundException, DataProcessingException {
        logger.info("Finding the latest exchange rate for currency {}", currency);
        Optional<ExchangeRate> latestExchangeRate = exchangeRateRepository
                .findFirstByCurrencyAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(currency, effectiveDateEnd);

        if (latestExchangeRate.isPresent()) {
            logger.info("Latest exchange rate found");
            return latestExchangeRate.get();
        }

        try {
            logger.info("Fetching exchange rates by filter for currency {}", currency);
            processExchangeRatesByFilter(
                    ExchangeRateFilter.builder()
                            .effectiveDateInit(effectiveDateEnd.minusMonths(6))
                            .effectiveDateEnd(effectiveDateEnd)
                            .dateCreation(LocalDateTime.now())
                            .currency(currency)
                            .pageSize(1)
                            .status(1)
                            .build()
            );
            logger.info("Exchange rates fetched and saved successfully");
        } catch (Exception e) {
            logger.error("Error processing asynchronous data", e);
            throw new DataProcessingException("Error processing asynchronous data", e);
        }

        LocalDate sixMonthsAgo = effectiveDateEnd.minusMonths(6);
        latestExchangeRate = exchangeRateRepository
                .findFirstByCurrencyAndEffectiveDateGreaterThanEqualAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(currency, sixMonthsAgo, effectiveDateEnd);

        if (latestExchangeRate.isPresent()) {
            logger.info("Latest exchange rate found");
            return latestExchangeRate.get();
        }

        throw new ExchangeRateNotFoundException("Could not find the latest exchange rate for currency " + currency);
    }

    @Async
    public void fetchDataAndSaveToDatabase(LocalDate effectiveDateEnd) {
        if (effectiveDateEnd == null) {
            throw new IllegalArgumentException("Effective date end must not be null");
        }

        logger.info("Fetching exchange rates data and saving to database");
        LocalDate lastEffectiveDate = exchangeRateFilterRepository.findMaxLastEffectiveDateForAllWithoutError()
                .orElse(effectiveDateEnd.minusMonths(6));

        if (lastEffectiveDate.isBefore(effectiveDateEnd)) {
            processExchangeRatesByFilter(
                    ExchangeRateFilter.builder()
                            .effectiveDateInit(lastEffectiveDate)
                            .effectiveDateEnd(effectiveDateEnd)
                            .dateCreation(LocalDateTime.now())
                            .status(1)
                            .build()
            );
        }
        CompletableFuture.completedFuture(null);
        logger.info("Exchange rates data fetched and saved to database successfully");
    }

    @Transactional
    private void processExchangeRatesByFilter(ExchangeRateFilter filter) throws DataProcessingException {
        logger.info("Processing exchange rates by filter {}", filter);
        Optional<ExchangeRateFilter> existingFilter = exchangeRateFilterRepository
                .findByEffectiveDateInitAndEffectiveDateEndAndCurrencyAndNotStatus(
                        filter.getEffectiveDateInit(),
                        filter.getEffectiveDateEnd(),
                        filter.getCurrency(),
                        4
                );

        if (existingFilter.isPresent()) {
            logger.info("Filter already exists");
            return;
        }

        filter = exchangeRateFilterRepository.save(filter);
        logger.info("Filter saved successfully");

        List<ExchangeRate> exchangeRateList = new ArrayList<>();
        fetchExchangeRatesByFilter(exchangeRateList, toExchangeRateApiFilter(filter));

        exchangeRateList.removeIf(rate -> exchangeRateRepository
                .existsByCurrencyAndEffectiveDate(rate.getCurrency(), rate.getEffectiveDate())
        );

        LocalDate lastEffectiveDate = exchangeRateList.stream()
                .map(ExchangeRate::getEffectiveDate)
                .max(Comparator.naturalOrder())
                .orElse(null);

        filter.setTotalCount(exchangeRateList.size());
        filter.setLastEffectiveDate(lastEffectiveDate);

        exchangeRateFilterRepository.save(filter);

        if (!exchangeRateList.isEmpty()) {
            ExchangeRateFilter finalFilter = filter;
            exchangeRateList.forEach(exchangeRate -> {
                exchangeRate.setExchangeRateFilter(finalFilter);
                exchangeRate.setCreatedAt(LocalDate.now());
            });

            exchangeRateList.stream()
                    .map(ExchangeRate::getEffectiveDate)
                    .distinct()
                    .forEach(partitionJdbc::createPartitionForExchangeRateIfNotExists);

            exchangeRateRepository.saveAll(exchangeRateList);
            logger.info("Exchange rates saved successfully");
        }
    }

    private void fetchExchangeRatesByFilter(List<ExchangeRate> exchangeRateList, ExchangeRateApiFilter filter) throws DataProcessingException {
        logger.info("Fetching exchange rates by filter {}", filter);
        ExchangeRateResponse response = exchangeRateApiClient.findAllByFilter(filter.toString());

        if (response == null) {
            return ;
        }

        exchangeRateList.addAll(response.getData().stream()
                .map(this::toExchangeRate)
                .collect(Collectors.toList())
        );

        exchangeRateList = exchangeRateList.stream().distinct().collect(Collectors.toList());

        if(exchangeRateList.isEmpty()) {
            logger.info("No exchange rates found by filter {}.", filter);
            return;
        }
        String nextPage = (response.getLinks() != null)
                ? response.getLinks().getNext()
                : null;
        filter.setNextPage(nextPage);
        if (nextPage != null && !nextPage.isBlank()) {
            fetchExchangeRatesByFilter(exchangeRateList, filter);
        }
        logger.info("Exchange rates fetched by filter {}. Total: {}", filter, exchangeRateList.size());
    }

    private ExchangeRate toExchangeRate(ExchangeRateResponse.ExchangeRateData exchangeRateResponse) {
        return ExchangeRate.builder()
                .effectiveDate(exchangeRateResponse.getEffectiveDate())
                .exchangeRate(exchangeRateResponse.getExchangeRate())
                .currency(exchangeRateResponse.getCurrency())
                .build();
    }

    private ExchangeRateApiFilter toExchangeRateApiFilter(ExchangeRateFilter filter) {
        return ExchangeRateApiFilter.builder()
                .effectiveDateInit(filter.getEffectiveDateInit())
                .effectiveDateEnd(filter.getEffectiveDateEnd())
                .currency(filter.getCurrency())
                .pageSize(filter.getPageSize())
                .sortOrder(SortOrder.from(filter.getSortOrder()))
                .build();
    }
}
