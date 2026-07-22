package com.wex.finance.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("purchases")
public record Purchase(
        @Id
        @Column("id")
        String id,

        @Column("description")
        String description,

        @Column("transaction_date")
        LocalDateTime transactionDate,

        @Column("amount_usd")
        BigDecimal amountUSD,

        @Column("created_at")
        LocalDateTime createdAt
) {
    public Purchase(String description, LocalDateTime transactionDate, BigDecimal amountUSD) {
        this(
                null,
                description,
                transactionDate,
                amountUSD,
                LocalDateTime.now()
        );
    }
}
