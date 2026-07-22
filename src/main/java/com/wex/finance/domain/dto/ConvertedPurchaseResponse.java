package com.wex.finance.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConvertedPurchaseResponse(
        @JsonProperty("id")
        String id,

        @JsonProperty("description")
        String description,

        @JsonProperty("transaction_date")
        LocalDateTime transactionDate,

        @JsonProperty("original_amount_usd")
        BigDecimal originalAmountUSD,

        @JsonProperty("target_currency")
        String targetCurrency,

        @JsonProperty("exchange_rate")
        BigDecimal exchangeRate,

        @JsonProperty("converted_amount")
        BigDecimal convertedAmount
) {}
