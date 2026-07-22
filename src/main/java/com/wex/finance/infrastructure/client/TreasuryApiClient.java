package com.wex.finance.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wex.finance.domain.exception.TreasuryApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class TreasuryApiClient {

    private static final Logger log = LoggerFactory.getLogger(TreasuryApiClient.class);

    private static final String TREASURY_API_URL =
            "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange";

    private static final Predicate<TreasuryResponse> HAS_DATA =
            response -> response != null && response.data() != null && !response.data().isEmpty();

    private final RestTemplate restTemplate;
    private final TreasuryApiProperties properties;

    public TreasuryApiClient(RestTemplate restTemplate, TreasuryApiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public ExchangeRateQuote fetchExchangeRate(String currency, LocalDateTime date) {
        validateInput(currency, date);

        var uri = buildUri(currency, date);
        log.debug("Calling Treasury API with uri={}", uri);
        var response = restTemplate.getForObject(uri, TreasuryResponse.class);

        return extractExchangeRate(response, currency, date);
    }

    private void validateInput(String currency, LocalDateTime date) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }
        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }
    }

    private URI buildUri(String currency, LocalDateTime transactionDate) {
        var treasuryCurrencyName = toTreasuryCurrencyName(currency);
        var purchaseDate = transactionDate.toLocalDate();
        var minDate = purchaseDate.minusMonths(6);

        var filter = "currency:eq:%s,record_date:gte:%s,record_date:lte:%s"
                .formatted(
                        treasuryCurrencyName,
                        minDate,
                        purchaseDate
                );

        return UriComponentsBuilder.fromUriString(TREASURY_API_URL)
                .queryParam("filter", filter)
                .build(true)
                .toUri();
    }

    private String toTreasuryCurrencyName(String currency) {
        var upperCurrency = currency.toUpperCase(Locale.ROOT);
        var mapped = configuredMappings().get(upperCurrency);
        if (mapped != null) {
            return mapped;
        }

        if (upperCurrency.length() == 3) {
            try {
                return java.util.Currency.getInstance(upperCurrency).getDisplayName(Locale.ENGLISH);
            } catch (IllegalArgumentException ignored) {
                return upperCurrency;
            }
        }

        return currency;
    }

    private Map<String, String> configuredMappings() {
        var rawMappings = properties.getCurrencyNameMapping();
        if (rawMappings == null || rawMappings.isEmpty()) {
            return Map.of();
        }

        return rawMappings.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toUpperCase(Locale.ROOT),
                        Map.Entry::getValue,
                        (left, right) -> right
                ));
    }


    private ExchangeRateQuote extractExchangeRate(
            TreasuryResponse response,
            String currency,
            LocalDateTime transactionDate
    ) {
        if (!HAS_DATA.test(response)) {
            log.warn("Treasury API returned no data for currency={} purchaseDate={}", currency, transactionDate.toLocalDate());
            throw new TreasuryApiException(
                    "No exchange rate found for %s within 6 months before %s".formatted(currency, transactionDate.toLocalDate())
            );
        }

        var purchaseDate = transactionDate.toLocalDate();
        var minDate = purchaseDate.minusMonths(6);

        return response.data().stream()
                .filter(rate -> rate.recordDate() != null)
                .filter(rate -> !rate.recordDate().isAfter(purchaseDate))
                .filter(rate -> !rate.recordDate().isBefore(minDate))
                .max(java.util.Comparator.comparing(TreasuryRate::recordDate))
                .map(rate -> new ExchangeRateQuote(rate.exchangeRate(), rate.recordDate()))
                .orElseThrow(() -> new TreasuryApiException(
                        "No exchange rate found for %s within 6 months before %s. Available dates: %s"
                                .formatted(
                                        currency,
                                        purchaseDate,
                                        response.data().stream()
                                                .map(TreasuryRate::recordDate)
                                                .map(String::valueOf)
                                                .collect(Collectors.joining(", "))
                                )
                ));
    }

    public record TreasuryResponse(List<TreasuryRate> data) {}

    public record TreasuryRate(
            @JsonProperty("record_date") LocalDate recordDate,
            @JsonProperty("exchange_rate") BigDecimal exchangeRate
    ) {}

    public record ExchangeRateQuote(BigDecimal rate, LocalDate rateDate) {}
}
