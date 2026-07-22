package com.wex.finance.application.service;

import com.wex.finance.domain.dto.ConvertedPurchaseResponse;
import com.wex.finance.domain.dto.CreatePurchaseRequest;
import com.wex.finance.domain.dto.PurchaseResponse;
import com.wex.finance.domain.entity.ExchangeRate;
import com.wex.finance.domain.entity.Purchase;
import com.wex.finance.domain.exception.InvalidPurchaseException;
import com.wex.finance.domain.exception.PurchaseNotFoundException;
import com.wex.finance.infrastructure.repository.PurchaseRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void createPurchaseShouldSaveAndReturnMappedResponse() {
        var request = new CreatePurchaseRequest("Fuel", "2026-05-06", new BigDecimal("10.129"));
        var savedPurchase = new Purchase(
                "purchase-id",
                "Fuel",
                LocalDateTime.of(2026, 5, 6, 0, 0),
                new BigDecimal("10.13"),
                LocalDateTime.of(2026, 5, 6, 12, 0)
        );

        when(purchaseRepository.save(any(Purchase.class))).thenReturn(savedPurchase);

        var response = purchaseService.createPurchase(request);

        assertEquals("purchase-id", response.id());
        assertEquals("Fuel", response.description());
        assertEquals(new BigDecimal("10.13"), response.amountUSD());
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void createPurchaseShouldRoundAmountBeforeSaving() {
        var request = new CreatePurchaseRequest("Fuel", "2026-05-06", new BigDecimal("15.456"));
        var captor = ArgumentCaptor.forClass(Purchase.class);

        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        purchaseService.createPurchase(request);

        verify(purchaseRepository).save(captor.capture());
        assertEquals(new BigDecimal("15.46"), captor.getValue().amountUSD());
    }

    @Test
    void createPurchaseShouldThrowInvalidPurchaseForInvalidDescription() {
        var request = new CreatePurchaseRequest("", "2026-05-06", new BigDecimal("10.00"));

        var ex = assertThrows(
                InvalidPurchaseException.class,
                () -> purchaseService.createPurchase(request)
        );

        assertEquals("Description is required", ex.getMessage());
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void createPurchaseShouldThrowInvalidPurchaseForInvalidAmount() {
        var request = new CreatePurchaseRequest("Fuel", "2026-05-06", BigDecimal.ZERO);

        var ex = assertThrows(
                InvalidPurchaseException.class,
                () -> purchaseService.createPurchase(request)
        );

        assertEquals("Amount must be positive and not exceed 999,999,999.99", ex.getMessage());
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void getPurchaseShouldReturnPurchaseWhenFound() {
        var stored = new Purchase(
                "purchase-id",
                "Fuel",
                LocalDateTime.of(2026, 5, 6, 10, 0),
                new BigDecimal("9.99"),
                LocalDateTime.of(2026, 5, 6, 10, 5)
        );
        when(purchaseRepository.findById("purchase-id")).thenReturn(Optional.of(stored));

        var response = purchaseService.getPurchase("purchase-id");

        assertEquals("purchase-id", response.id());
        assertEquals("Fuel", response.description());
        assertEquals(new BigDecimal("9.99"), response.amountUSD());
    }

    @Test
    void getPurchaseShouldThrowWhenPurchaseNotFound() {
        when(purchaseRepository.findById("missing-id")).thenReturn(Optional.empty());

        var ex = assertThrows(
                PurchaseNotFoundException.class,
                () -> purchaseService.getPurchase("missing-id")
        );

        assertTrue(ex.getMessage().contains("missing-id"));
    }

    @Test
    void getPurchaseWithConversionShouldReturnConvertedResponse() {
        var purchaseDate = LocalDateTime.of(2026, 5, 6, 10, 0);
        var purchase = new Purchase(
                "purchase-id",
                "Fuel",
                purchaseDate,
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 5, 6, 10, 1)
        );
        var exchangeRate = new ExchangeRate(
                1L,
                "BRL",
                2,
                2026,
                LocalDate.of(2026, 4, 30),
                new BigDecimal("5.123456"),
                LocalDateTime.now()
        );

        when(purchaseRepository.findById("purchase-id")).thenReturn(Optional.of(purchase));
        when(exchangeRateService.getExchangeRate(eq("BRL"), eq(purchaseDate))).thenReturn(exchangeRate);

        var response = purchaseService.getPurchaseWithConversion("purchase-id", "brl");

        assertEquals("purchase-id", response.id());
        assertEquals("BRL", response.targetCurrency());
        assertEquals(purchaseDate, response.transactionDate());
        assertEquals(new BigDecimal("5.123456"), response.exchangeRate());
        assertEquals(new BigDecimal("51.23"), response.convertedAmount());
    }

    @Test
    void getPurchaseWithConversionShouldReturnExpectedEurConvertedAmount() {
        var purchaseDate = LocalDateTime.of(2024, 8, 15, 10, 0);
        var purchase = new Purchase(
                "purchase-id",
                "Office Supplies",
                purchaseDate,
                new BigDecimal("100.00"),
                LocalDateTime.of(2024, 8, 15, 10, 1)
        );
        var exchangeRate = new ExchangeRate(
                1L,
                "EUR",
                3,
                2024,
                LocalDate.of(2024, 8, 1),
                new BigDecimal("0.893000"),
                LocalDateTime.now()
        );

        when(purchaseRepository.findById("purchase-id")).thenReturn(Optional.of(purchase));
        when(exchangeRateService.getExchangeRate(eq("EUR"), eq(purchaseDate))).thenReturn(exchangeRate);

        var response = purchaseService.getPurchaseWithConversion("purchase-id", "eur");

        assertEquals("purchase-id", response.id());
        assertEquals("EUR", response.targetCurrency());
        assertEquals(purchaseDate, response.transactionDate());
        assertEquals(new BigDecimal("0.893000"), response.exchangeRate());
        assertEquals(new BigDecimal("89.30"), response.convertedAmount());
    }

    @Test
    void getPurchaseWithConversionShouldThrowWhenPurchaseNotFound() {
        when(purchaseRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThrows(
                PurchaseNotFoundException.class,
                () -> purchaseService.getPurchaseWithConversion("missing-id", "BRL")
        );
        verify(exchangeRateService, never()).getExchangeRate(any(), any());
    }
}
