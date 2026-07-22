package com.wex.finance.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseResponse(
        @JsonProperty("id")
        String id,

        @JsonProperty("description")
        String description,

        @JsonProperty("transaction_date")
        LocalDateTime transactionDate,

        @JsonProperty("amount_usd")
        BigDecimal amountUSD,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {}
