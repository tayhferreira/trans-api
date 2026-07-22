package com.wex.finance.infrastructure.client;

import com.wex.finance.domain.exception.TreasuryApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreasuryApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TreasuryApiProperties properties;

    @InjectMocks
    private TreasuryApiClient treasuryApiClient;

    @Test
    void fetchExchangeRateShouldReturnMostRecentRateWithinSixMonthsWindow() {
        var date = LocalDateTime.of(2026, 5, 10, 10, 0);
        var response = new TreasuryApiClient.TreasuryResponse(
                List.of(
                        new TreasuryApiClient.TreasuryRate(LocalDate.of(2026, 4, 20), new BigDecimal("5.321000")),
                        new TreasuryApiClient.TreasuryRate(LocalDate.of(2026, 5, 5), new BigDecimal("5.333000"))
                )
        );

        when(restTemplate.getForObject(org.mockito.ArgumentMatchers.any(URI.class), eq(TreasuryApiClient.TreasuryResponse.class)))
                .thenReturn(response);
        when(properties.getCurrencyNameMapping()).thenReturn(Map.of("BRL", "Real", "EUR", "Euro"));

        var quote = treasuryApiClient.fetchExchangeRate("brl", date);

        assertEquals(new BigDecimal("5.333000"), quote.rate());
        assertEquals(LocalDate.of(2026, 5, 5), quote.rateDate());

        var uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).getForObject(uriCaptor.capture(), eq(TreasuryApiClient.TreasuryResponse.class));
        assertTrue(uriCaptor.getValue().toString().contains("currency:eq:Real"));
        assertTrue(uriCaptor.getValue().toString().contains("record_date:gte:2025-11-10"));
        assertTrue(uriCaptor.getValue().toString().contains("record_date:lte:2026-05-10"));
    }

    @Test
    void fetchExchangeRateShouldMapEurCodeToEuroInTreasuryFilter() {
        var date = LocalDateTime.of(2024, 8, 15, 10, 0);
        var response = new TreasuryApiClient.TreasuryResponse(
                List.of(new TreasuryApiClient.TreasuryRate(LocalDate.of(2024, 8, 1), new BigDecimal("0.893000")))
        );

        when(restTemplate.getForObject(org.mockito.ArgumentMatchers.any(URI.class), eq(TreasuryApiClient.TreasuryResponse.class)))
                .thenReturn(response);
        when(properties.getCurrencyNameMapping()).thenReturn(Map.of("BRL", "Real", "EUR", "Euro"));

        var quote = treasuryApiClient.fetchExchangeRate("EUR", date);

        assertEquals(new BigDecimal("0.893000"), quote.rate());

        var uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).getForObject(uriCaptor.capture(), eq(TreasuryApiClient.TreasuryResponse.class));
        assertTrue(uriCaptor.getValue().toString().contains("currency:eq:Euro"));
    }

    @Test
    void fetchExchangeRateShouldThrowWhenCurrencyIsBlank() {
        var ex = assertThrows(
                IllegalArgumentException.class,
                () -> treasuryApiClient.fetchExchangeRate(" ", LocalDateTime.now())
        );

        assertEquals("currency must not be blank", ex.getMessage());
    }

    @Test
    void fetchExchangeRateShouldThrowWhenDateIsNull() {
        var ex = assertThrows(
                IllegalArgumentException.class,
                () -> treasuryApiClient.fetchExchangeRate("BRL", null)
        );

        assertEquals("date must not be null", ex.getMessage());
    }

    @Test
    void fetchExchangeRateShouldThrowTreasuryApiExceptionWhenResponseIsEmpty() {
        var date = LocalDateTime.of(2026, 5, 10, 10, 0);
        var response = new TreasuryApiClient.TreasuryResponse(List.of());
        when(restTemplate.getForObject(org.mockito.ArgumentMatchers.any(URI.class), eq(TreasuryApiClient.TreasuryResponse.class)))
                .thenReturn(response);
        when(properties.getCurrencyNameMapping()).thenReturn(Map.of("BRL", "Real"));

        var ex = assertThrows(
                TreasuryApiException.class,
                () -> treasuryApiClient.fetchExchangeRate("BRL", date)
        );

        assertTrue(ex.getMessage().contains("No exchange rate found"));
    }

    @Test
    void fetchExchangeRateShouldThrowTreasuryApiExceptionWhenNoRateFallsInSixMonthsWindow() {
        var date = LocalDateTime.of(2026, 5, 10, 10, 0);
        var response = new TreasuryApiClient.TreasuryResponse(
                List.of(new TreasuryApiClient.TreasuryRate(LocalDate.of(2025, 10, 10), new BigDecimal("5.111000")))
        );
        when(restTemplate.getForObject(org.mockito.ArgumentMatchers.any(URI.class), eq(TreasuryApiClient.TreasuryResponse.class)))
                .thenReturn(response);
        when(properties.getCurrencyNameMapping()).thenReturn(Map.of("BRL", "Real"));

        var ex = assertThrows(
                TreasuryApiException.class,
                () -> treasuryApiClient.fetchExchangeRate("BRL", date)
        );

        assertTrue(ex.getMessage().contains("within 6 months"));
    }
}
