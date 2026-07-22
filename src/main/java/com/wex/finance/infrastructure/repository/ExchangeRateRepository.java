package com.wex.finance.infrastructure.repository;

import com.wex.finance.domain.entity.ExchangeRate;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends CrudRepository<ExchangeRate, Long> {

    @Query("""
        SELECT * FROM "exchange_rates"
        WHERE "currency" = :currency
        AND "rate_date" <= :purchaseDate
        AND "rate_date" >= :minDate
        ORDER BY "rate_date" DESC
        LIMIT 1
    """)
    Optional<ExchangeRate> findMostRecentRateWithinWindow(
            @Param("currency") String currency,
            @Param("purchaseDate") LocalDate purchaseDate,
            @Param("minDate") LocalDate minDate
    );
}
