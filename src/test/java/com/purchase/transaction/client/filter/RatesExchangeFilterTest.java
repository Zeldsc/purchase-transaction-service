package com.purchase.transaction.client.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class RatesExchangeFilterTest {

    // Default test data
    private final LocalDate effectiveDateInit = LocalDate.of(2023, 1, 1);
    private final LocalDate effectiveDateEnd = LocalDate.of(2023, 12, 31);
    private final String currency = "USD";
    private final SortOrder sortOrder = SortOrder.DESC;
    private final Integer pageSize = 10;

    @Test
    public void testToStringWithAllFieldsSet() {
        // Create RatesExchangeFilter object
        ExchangeRateApiFilter filter = ExchangeRateApiFilter.builder()
                .effectiveDateInit(effectiveDateInit)
                .effectiveDateEnd(effectiveDateEnd)
                .currency(currency)
                .sortOrder(sortOrder)
                .pageSize(pageSize)
                .build();

        // Call toString() method
        String result = filter.toString();

        // Assertions
        assertEquals("&sort=-record_date&filter=effective_date:gte:2023-01-01,effective_date:lte:2023-12-31,currency:eq:USD&page[size]=10", result);
    }

    @Test
    public void testToStringWithNullFields() {
        // Create RatesExchangeFilter object with all fields null
        ExchangeRateApiFilter filter = ExchangeRateApiFilter.builder().build();

        // Call toString() method
        String result = filter.toString();

        // Assertions
        assertEquals("&sort=+record_date", result);
    }

    @Test
    public void testToStringWithEmptyCurrency() {
        // Create RatesExchangeFilter object with empty currency field
        ExchangeRateApiFilter filter = ExchangeRateApiFilter.builder()
                .effectiveDateInit(effectiveDateInit)
                .effectiveDateEnd(effectiveDateEnd)
                .currency("")
                .sortOrder(sortOrder)
                .pageSize(pageSize)
                .build();

        // Call toString() method
        String result = filter.toString();

        // Assertions
        assertEquals("&sort=-record_date&filter=effective_date:gte:2023-01-01,effective_date:lte:2023-12-31&page[size]=10", result);
    }

    @Test
    public void testToStringWithoutEffectiveDateInit() {
        // Create RatesExchangeFilter object
        ExchangeRateApiFilter filter = ExchangeRateApiFilter.builder()
                .effectiveDateEnd(effectiveDateEnd)
                .currency(currency)
                .sortOrder(sortOrder)
                .pageSize(pageSize)
                .build();

        // Call toString() method
        String result = filter.toString();

        // Assertions
        assertEquals("&sort=-record_date&filter=effective_date:lte:2023-12-31,currency:eq:USD&page[size]=10", result);
    }

    @Test
    public void testToStringWithoutEffectiveDateEnd() {
        // Create RatesExchangeFilter object
        ExchangeRateApiFilter filter = ExchangeRateApiFilter.builder()
                .effectiveDateInit(effectiveDateInit)
                .currency(currency)
                .sortOrder(sortOrder)
                .pageSize(pageSize)
                .build();

        // Call toString() method
        String result = filter.toString();

        // Assertions
        assertEquals("&sort=-record_date&filter=effective_date:gte:2023-01-01,currency:eq:USD&page[size]=10", result);
    }

    @Test
    public void testToStringWithoutAllEffectiveDate() {
        // Create RatesExchangeFilter object
        ExchangeRateApiFilter filter = ExchangeRateApiFilter.builder()
                .currency(currency)
                .sortOrder(sortOrder)
                .pageSize(pageSize)
                .build();

        // Call toString() method
        String result = filter.toString();

        // Assertions
        assertEquals("&sort=-record_date&filter=currency:eq:USD&page[size]=10", result);
    }

    @Test
    public void testToStringWithoutCurrency() {
        // Create RatesExchangeFilter object
        ExchangeRateApiFilter filter = ExchangeRateApiFilter.builder()
                .effectiveDateInit(effectiveDateInit)
                .effectiveDateEnd(effectiveDateEnd)
                .sortOrder(sortOrder)
                .pageSize(pageSize)
                .build();

        // Call toString() method
        String result = filter.toString();

        // Assertions
        assertEquals("&sort=-record_date&filter=effective_date:gte:2023-01-01,effective_date:lte:2023-12-31&page[size]=10", result);
    }

    @Test
    public void testToStringWithoutAllFilter() {
        // Create RatesExchangeFilter object
        ExchangeRateApiFilter filter = ExchangeRateApiFilter.builder()
                .sortOrder(sortOrder)
                .pageSize(pageSize)
                .build();

        // Call toString() method
        String result = filter.toString();

        // Assertions
        assertEquals("&sort=-record_date&page[size]=10", result);
    }

    @Test
    public void testToStringWithAscOrder() {
        // Create RatesExchangeFilter object
        ExchangeRateApiFilter filter = ExchangeRateApiFilter.builder()
                .sortOrder(SortOrder.ASC)
                .pageSize(pageSize)
                .build();

        // Call toString() method
        String result = filter.toString();

        // Assertions
        assertEquals("&sort=+record_date&page[size]=10", result);
    }

    @Test
    public void testToStringWithoutPageSize() {
        // Create RatesExchangeFilter object
        ExchangeRateApiFilter filter = ExchangeRateApiFilter.builder()
                .effectiveDateInit(effectiveDateInit)
                .effectiveDateEnd(effectiveDateEnd)
                .currency(currency)
                .sortOrder(sortOrder)
                .build();

        // Call toString() method
        String result = filter.toString();

        // Assertions
        assertEquals("&sort=-record_date&filter=effective_date:gte:2023-01-01,effective_date:lte:2023-12-31,currency:eq:USD", result);
    }
}
