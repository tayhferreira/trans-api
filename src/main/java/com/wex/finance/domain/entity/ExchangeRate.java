package com.wex.finance.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table("exchange_rates")
public record ExchangeRate(
        @Id
        @Column("id")
        Long id,

        @Column("currency")
        String currency,

        @Column("quarter")
        Integer quarter,

        @Column("rate_year")
        Integer year,

        @Column("rate_date")
        LocalDate rateDate,

        @Column("rate")
        BigDecimal rate,

        @Column("created_at")
        LocalDateTime createdAt
) {
    public ExchangeRate(String currency, Integer quarter, Integer year, LocalDate rateDate, BigDecimal rate) {
        this(null, currency, quarter, year, rateDate, rate, LocalDateTime.now());
    }
}
