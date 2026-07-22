package com.wex.finance.presentation.handler;

import com.wex.finance.domain.exception.DomainException;
import com.wex.finance.domain.exception.ExchangeRateNotFoundException;
import com.wex.finance.domain.exception.InvalidPurchaseException;
import com.wex.finance.domain.exception.PurchaseNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlePurchaseNotFoundShouldReturnNotFound() {
        var ex = new PurchaseNotFoundException("purchase-id");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/transactions/purchase-id");

        var response = handler.handlePurchaseNotFound(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("PURCHASE_NOT_FOUND", response.getBody().code());
        assertEquals("/api/transactions/purchase-id", response.getBody().path());
    }

    @Test
    void handleExchangeRateNotFoundShouldReturnNotFound() {
        var ex = new ExchangeRateNotFoundException("BRL");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/transactions/1/convert");

        var response = handler.handleExchangeRateNotFound(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("EXCHANGE_RATE_NOT_FOUND", response.getBody().code());
    }

    @Test
    void handleInvalidPurchaseShouldReturnBadRequest() {
        var ex = new InvalidPurchaseException("Description is required");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/transactions");

        var response = handler.handleInvalidPurchase(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("INVALID_PURCHASE", response.getBody().code());
        assertEquals("Description is required", response.getBody().message());
    }

    @Test
    void handleValidationExceptionShouldAggregateFieldMessages() {
        var fieldErrors = List.of(
                new FieldError("createPurchaseRequest", "description", "Description is required"),
                new FieldError("createPurchaseRequest", "amount", "Amount must be positive")
        );
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/transactions");

        var response = handler.handleValidationException(validationException, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
        assertEquals(
                "description: Description is required, amount: Amount must be positive",
                response.getBody().message()
        );
    }

    @Test
    void handleDomainExceptionShouldReturnInternalServerErrorWithDomainCode() {
        var ex = new DomainException("SOME_DOMAIN_CODE", "Domain failure");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/transactions");

        var response = handler.handleDomainException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("SOME_DOMAIN_CODE", response.getBody().code());
        assertEquals("Domain failure", response.getBody().message());
    }

    @Test
    void handleGenericExceptionShouldReturnInternalErrorResponse() {
        var ex = new RuntimeException("unexpected");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/transactions");

        var response = handler.handleGenericException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }
}
