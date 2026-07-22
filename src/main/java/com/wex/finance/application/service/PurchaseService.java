package com.wex.finance.application.service;

import com.wex.finance.domain.dto.ConvertedPurchaseResponse;
import com.wex.finance.domain.dto.CreatePurchaseRequest;
import com.wex.finance.domain.dto.PurchaseResponse;
import com.wex.finance.domain.entity.Purchase;
import com.wex.finance.domain.exception.InvalidPurchaseException;
import com.wex.finance.domain.exception.PurchaseNotFoundException;
import com.wex.finance.domain.util.DateUtils;
import com.wex.finance.domain.util.DecimalUtils;
import com.wex.finance.infrastructure.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import java.util.function.Predicate;

@Service
public class PurchaseService {

    private static final int MAX_DESCRIPTION_LENGTH = 50;

    private static final Predicate<CreatePurchaseRequest> VALID_REQUEST = req ->
            req != null &&
                    req.getDescription() != null &&
                    !req.getDescription().isBlank() &&
                    req.getDescription().length() <= MAX_DESCRIPTION_LENGTH &&
                    DecimalUtils.isValidAmount(req.getAmount()) &&
                    DateUtils.isValidDateFormat(req.getTransactionDate());

    private final PurchaseRepository purchaseRepository;
    private final ExchangeRateService exchangeRateService;

    public PurchaseService(PurchaseRepository purchaseRepository, ExchangeRateService exchangeRateService) {
        this.purchaseRepository = purchaseRepository;
        this.exchangeRateService = exchangeRateService;
    }

    public PurchaseResponse createPurchase(CreatePurchaseRequest request) {
        if (!VALID_REQUEST.test(request)) {
            throw new InvalidPurchaseException(buildValidationError(request));
        }

        var transactionDate = DateUtils.parseDate(request.getTransactionDate());
        var amountUSD = DecimalUtils.roundToUSD(request.getAmount());

        var purchase = new Purchase(
                request.getDescription(),
                transactionDate,
                amountUSD
        );

        return toPurchaseResponse(purchaseRepository.save(purchase));
    }

    public ConvertedPurchaseResponse getPurchaseWithConversion(String purchaseId, String targetCurrency) {
        var purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));

        var exchangeRate = exchangeRateService.getExchangeRate(
                targetCurrency.toUpperCase(),
                purchase.transactionDate()
        );

        var convertedAmount = DecimalUtils.convertCurrency(
                purchase.amountUSD(),
                exchangeRate.rate()
        );

        return new ConvertedPurchaseResponse(
                purchase.id(),
                purchase.description(),
                purchase.transactionDate(),
                purchase.amountUSD(),
                targetCurrency.toUpperCase(),
                exchangeRate.rate(),
                convertedAmount
        );
    }

    public PurchaseResponse getPurchase(String purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .map(this::toPurchaseResponse)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));
    }

    private String buildValidationError(CreatePurchaseRequest request) {
        if (request == null) {
            return "Request body is required";
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            return "Description is required";
        }
        if (request.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            return "Description must not exceed %d characters".formatted(MAX_DESCRIPTION_LENGTH);
        }
        if (!DecimalUtils.isValidAmount(request.getAmount())) {
            return "Amount must be positive and not exceed 999,999,999.99";
        }
        if (!DateUtils.isValidDateFormat(request.getTransactionDate())) {
            return "Transaction date must be in YYYY-MM-DD or ISO 8601 format";
        }
        return "Invalid purchase request";
    }

    private PurchaseResponse toPurchaseResponse(Purchase purchase) {
        return new PurchaseResponse(
                purchase.id(),
                purchase.description(),
                purchase.transactionDate(),
                purchase.amountUSD(),
                purchase.createdAt()
        );
    }
}
