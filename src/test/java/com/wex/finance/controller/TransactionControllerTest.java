package com.wex.finance.presentation.controller;

import com.wex.finance.application.service.PurchaseService;
import com.wex.finance.domain.dto.ConvertedPurchaseResponse;
import com.wex.finance.domain.dto.CreatePurchaseRequest;
import com.wex.finance.domain.dto.PurchaseResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private PurchaseService purchaseService;

    @InjectMocks
    private TransactionController controller;

    @Test
    void createPurchaseShouldReturnCreatedStatus() {
        var request = new CreatePurchaseRequest("Fuel", "2026-05-10", new BigDecimal("10.00"));
        var response = new PurchaseResponse(
                "purchase-id",
                "Fuel",
                LocalDateTime.of(2026, 5, 10, 0, 0),
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 5, 10, 12, 0)
        );
        when(purchaseService.createPurchase(request)).thenReturn(response);

        var result = controller.createPurchase(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(purchaseService).createPurchase(request);
    }

    @Test
    void getPurchaseShouldReturnOkStatus() {
        var response = new PurchaseResponse(
                "purchase-id",
                "Fuel",
                LocalDateTime.of(2026, 5, 10, 0, 0),
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 5, 10, 12, 0)
        );
        when(purchaseService.getPurchase("purchase-id")).thenReturn(response);

        var result = controller.getPurchase("purchase-id");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(purchaseService).getPurchase("purchase-id");
    }

    @Test
    void getPurchaseWithConversionShouldReturnOkStatus() {
        var response = new ConvertedPurchaseResponse(
                "purchase-id",
                "Fuel",
                LocalDateTime.of(2026, 5, 10, 0, 0),
                new BigDecimal("10.00"),
                "BRL",
                new BigDecimal("5.123456"),
                new BigDecimal("51.23")
        );
        when(purchaseService.getPurchaseWithConversion("purchase-id", "BRL")).thenReturn(response);

        var result = controller.getPurchaseWithConversion("purchase-id", "BRL");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(purchaseService).getPurchaseWithConversion("purchase-id", "BRL");
    }
}
