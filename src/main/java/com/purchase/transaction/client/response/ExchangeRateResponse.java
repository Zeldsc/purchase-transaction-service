package com.purchase.transaction.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ExchangeRateResponse {

    private List<ExchangeRateData> data;
    private Link links;

    @Data
    @Builder
    public static class ExchangeRateData {
        @JsonProperty("record_date")
        private LocalDate recordDate;

        private String currency;

        @JsonProperty("effective_date")
        private LocalDate effectiveDate;

        @JsonProperty("exchange_rate")
        private BigDecimal exchangeRate;
    }

    @Data
    @Builder
    public static class Link {
        private String prev;
        private String next;
    }
}