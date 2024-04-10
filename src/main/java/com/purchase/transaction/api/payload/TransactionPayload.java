package com.purchase.transaction.api.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPayload {
    @Size(max = 50)
    @NotNull
    @JsonProperty("description")
    private String description;

    @NotNull
    @JsonProperty("transaction_date")
    private LocalDate transactionDate;

    @NotNull
    @Positive
    @JsonProperty("purchase_amount")
    private BigDecimal purchaseAmount;
}
