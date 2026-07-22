package com.wex.finance.domain.exception;

public class ExchangeRateNotFoundException extends DomainException {

    public ExchangeRateNotFoundException(String currency, int quarter, int year) {
        super(
                "EXCHANGE_RATE_NOT_FOUND",
                "No exchange rate found for currency '" + currency + "' in Q" + quarter + " " + year
        );
    }

    public ExchangeRateNotFoundException(String currency) {
        super(
                "EXCHANGE_RATE_NOT_FOUND",
                "No valid exchange rate found for currency '" + currency + "' within 6-month window"
        );
    }
}
