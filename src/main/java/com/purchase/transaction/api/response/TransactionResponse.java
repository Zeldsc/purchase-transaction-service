package com.purchase.transaction.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class TransactionResponse {
    private Long id;

    @JsonProperty("description")
    private String description;

    @JsonProperty("transaction_date")
    private LocalDate transactionDate;

    @JsonProperty("purchase_amount")
    private BigDecimal purchaseAmount;

    @JsonProperty("converted_purchase_amount")
    private BigDecimal convertedPurchaseAmount;

}
