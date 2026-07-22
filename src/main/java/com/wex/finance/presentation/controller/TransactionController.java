package com.wex.finance.presentation.controller;

import com.wex.finance.application.service.PurchaseService;
import com.wex.finance.domain.dto.ConvertedPurchaseResponse;
import com.wex.finance.domain.dto.CreatePurchaseRequest;
import com.wex.finance.domain.dto.PurchaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions" )
@Tag(name = "Transactions", description = "Purchase transaction management endpoints")
public class TransactionController {

    private final PurchaseService purchaseService;

    public TransactionController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new purchase transaction",
            description = "Creates a new purchase transaction with USD amount and transaction date"
    )
    @ApiResponse(responseCode = "201", description = "Purchase created successfully",
            content = @Content(schema = @Schema(implementation = PurchaseResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    public ResponseEntity<PurchaseResponse> createPurchase(@Valid @RequestBody CreatePurchaseRequest request) {
        var response = purchaseService.createPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Retrieve a purchase transaction",
            description = "Retrieves a purchase transaction by ID without currency conversion"
    )
    @ApiResponse(responseCode = "200", description = "Purchase found",
            content = @Content(schema = @Schema(implementation = PurchaseResponse.class)))
    @ApiResponse(responseCode = "404", description = "Purchase not found")
    public ResponseEntity<PurchaseResponse> getPurchase(@PathVariable String id) {
        var response = purchaseService.getPurchase(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/convert")
    @Operation(
            summary = "Retrieve purchase with currency conversion",
            description = "Retrieves a purchase and converts the amount to the specified currency using Treasury API rates"
    )
    @ApiResponse(responseCode = "200", description = "Purchase retrieved and converted",
            content = @Content(schema = @Schema(implementation = ConvertedPurchaseResponse.class)))
    @ApiResponse(responseCode = "404", description = "Purchase or exchange rate not found")
    @ApiResponse(responseCode = "400", description = "Invalid currency code")
    public ResponseEntity<ConvertedPurchaseResponse> getPurchaseWithConversion(
            @PathVariable String id,
            @RequestParam String currency
    ) {
        var response = purchaseService.getPurchaseWithConversion(id, currency);
        return ResponseEntity.ok(response);
    }
}
