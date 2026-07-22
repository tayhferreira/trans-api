package com.wex.finance.application.service;


import com.wex.finance.domain.entity.ExchangeRate;
import com.wex.finance.domain.exception.ExchangeRateNotFoundException;
import com.wex.finance.domain.util.DateUtils;
import com.wex.finance.domain.util.DecimalUtils;
import com.wex.finance.infrastructure.client.TreasuryApiClient;

import com.wex.finance.infrastructure.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.Supplier;

@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private final ExchangeRateRepository repository;
    private final TreasuryApiClient treasuryApiClient;

    public ExchangeRateService(ExchangeRateRepository repository, TreasuryApiClient treasuryApiClient) {
        this.repository = repository;
        this.treasuryApiClient = treasuryApiClient;
    }

    public ExchangeRate getExchangeRate(String currency, LocalDateTime transactionDate) {
        var purchaseDate = transactionDate.toLocalDate();
        var minDate = purchaseDate.minusMonths(6);
        log.debug(
                "Looking up exchange rate for currency={} from {} to {}",
                currency,
                minDate,
                purchaseDate
        );

        return repository.findMostRecentRateWithinWindow(
                currency.toUpperCase(),
                purchaseDate,
                minDate
        ).orElseGet(fetchAndCacheSupplier(currency, transactionDate));
    }

    private Supplier<ExchangeRate> fetchAndCacheSupplier(String currency, LocalDateTime transactionDate) {
        return () -> {
            try {
                var quote = treasuryApiClient.fetchExchangeRate(currency, transactionDate);
                var roundedRate = DecimalUtils.roundToRate(quote.rate());
                var rateDateTime = quote.rateDate().atStartOfDay();

                var exchangeRate = new ExchangeRate(
                        currency.toUpperCase(),
                        DateUtils.getQuarter(rateDateTime),
                        quote.rateDate().getYear(),
                        quote.rateDate(),
                        roundedRate
                );
                return repository.save(exchangeRate);
            } catch (Exception e) {
                log.error(
                        "Failed to fetch/cache exchange rate for currency={} purchaseDate={}",
                        currency.toUpperCase(),
                        transactionDate.toLocalDate(),
                        e
                );
                throw new ExchangeRateNotFoundException(currency);
            }
        };
    }
}
