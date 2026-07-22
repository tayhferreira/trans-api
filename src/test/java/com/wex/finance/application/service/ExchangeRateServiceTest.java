package com.wex.finance.application.service;

import com.wex.finance.infrastructure.client.TreasuryApiClient;
import com.wex.finance.domain.entity.ExchangeRate;
import com.wex.finance.domain.exception.ExchangeRateNotFoundException;
import com.wex.finance.infrastructure.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository repository;

    @Mock
    private TreasuryApiClient treasuryApiClient;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void getExchangeRateShouldReturnCachedRateWhenPresent() {
        var transactionDate = LocalDateTime.of(2026, 5, 10, 12, 0);
        var cached = new ExchangeRate(1L, "BRL", 2, 2026, LocalDate.of(2026, 4, 30), new BigDecimal("5.123456"), LocalDateTime.now());
        when(repository.findMostRecentRateWithinWindow("BRL", LocalDate.of(2026, 5, 10), LocalDate.of(2025, 11, 10)))
                .thenReturn(Optional.of(cached));

        var result = exchangeRateService.getExchangeRate("brl", transactionDate);

        assertEquals(cached, result);
        verify(treasuryApiClient, never()).fetchExchangeRate(any(), any());
    }

    @Test
    void getExchangeRateShouldFetchAndPersistRateWhenCacheMisses() {
        var transactionDate = LocalDateTime.of(2026, 5, 10, 12, 0);
        when(repository.findMostRecentRateWithinWindow("BRL", LocalDate.of(2026, 5, 10), LocalDate.of(2025, 11, 10)))
                .thenReturn(Optional.empty());
        when(treasuryApiClient.fetchExchangeRate("brl", transactionDate))
                .thenReturn(new TreasuryApiClient.ExchangeRateQuote(new BigDecimal("5.1234567"), LocalDate.of(2026, 4, 30)));
        when(repository.save(any(ExchangeRate.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = exchangeRateService.getExchangeRate("brl", transactionDate);

        assertEquals("BRL", result.currency());
        assertEquals(Integer.valueOf(2), result.quarter());
        assertEquals(Integer.valueOf(2026), result.year());
        assertEquals(LocalDate.of(2026, 4, 30), result.rateDate());
        assertEquals(new BigDecimal("5.123457"), result.rate());

        var captor = ArgumentCaptor.forClass(ExchangeRate.class);
        verify(repository).save(captor.capture());
        assertEquals("BRL", captor.getValue().currency());
        assertEquals(LocalDate.of(2026, 4, 30), captor.getValue().rateDate());
        assertEquals(new BigDecimal("5.123457"), captor.getValue().rate());
    }

    @Test
    void getExchangeRateShouldThrowDomainExceptionWhenClientFails() {
        var transactionDate = LocalDateTime.of(2026, 5, 10, 12, 0);
        when(repository.findMostRecentRateWithinWindow("BRL", LocalDate.of(2026, 5, 10), LocalDate.of(2025, 11, 10)))
                .thenReturn(Optional.empty());
        when(treasuryApiClient.fetchExchangeRate(eq("brl"), eq(transactionDate)))
                .thenThrow(new RuntimeException("upstream error"));

        var ex = assertThrows(
                ExchangeRateNotFoundException.class,
                () -> exchangeRateService.getExchangeRate("brl", transactionDate)
        );

        assertEquals("EXCHANGE_RATE_NOT_FOUND", ex.getCode());
    }
}
