package com.purchase.transaction.client.filter;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Data
@Builder
public class ExchangeRateApiFilter {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDateInit;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDateEnd;

    private String currency;

    private SortOrder sortOrder;

    private Integer pageSize;

    private String nextPage;

    @Override
    public String toString() {
        List<String> filters = new ArrayList<>();
        addTo(filters, "effective_date:gte", effectiveDateInit);
        addTo(filters, "effective_date:lte", effectiveDateEnd);
        addTo(filters, "currency:eq", currency);

        StringBuilder queryString = new StringBuilder();

        queryString.append("&sort=");
        queryString.append(sortOrder != null && sortOrder == SortOrder.DESC ? "-" : "+");
        queryString.append("record_date");

        if (!filters.isEmpty()) {
            queryString.append("&filter=");
            queryString.append(String.join(",", filters));
        }

        if (pageSize != null) {
            queryString.append("&page[size]=");
            queryString.append(pageSize);
        }

        if (nonNull(nextPage) && !nextPage.isBlank()) {
            if (!nextPage.startsWith("&")) queryString.append("&");
            queryString.append(pageSize);
        }
        return queryString.toString();
    }

    private void addTo(List<String> list, String key, Object value) {
        if (nonNull(value) && !value.toString().isBlank()) {
            list.add(key + ":" + value);
        }
    }

}
