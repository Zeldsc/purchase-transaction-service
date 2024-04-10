package com.purchase.transaction.jobs;

import com.purchase.transaction.service.ExchangeRateService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@AllArgsConstructor
public class ExchangeRateScheduledTask {

    private ExchangeRateService exchangeRateService;
    @Scheduled(cron = "0 0 2 * * *")
    public void executeTask() {
        exchangeRateService.fetchDataAndSaveToDatabase(LocalDate.now());
    }
}