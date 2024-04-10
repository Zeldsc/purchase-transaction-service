package com.purchase.transaction.client;

import com.purchase.transaction.client.response.ExchangeRateResponse;
import lombok.AllArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

/*
@FeignClient(
        name = "rates-exchange-api",
        url = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange"
)
public interface ExchangeRateApiClient {

    @GetMapping("?fields=record_date,currency,effective_date,exchange_rate{filter}")
    ExchangeRateResponse findAllByFilter(@RequestParam("filter") String filter);

}
*/

@Component
@AllArgsConstructor
public class ExchangeRateApiClient {

    private RestTemplate restTemplate;

    public ExchangeRateResponse findAllByFilter(String filter) {
        final String url = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange"
                + "?fields=record_date,currency,effective_date,exchange_rate" + filter;
        ResponseEntity<ExchangeRateResponse> response = restTemplate.getForEntity(url, ExchangeRateResponse.class);
        return response.getBody();
    }
}
