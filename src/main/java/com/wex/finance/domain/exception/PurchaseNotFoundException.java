package com.wex.finance.domain.exception;

public class PurchaseNotFoundException extends DomainException {

    public PurchaseNotFoundException(String purchaseId) {
        super("PURCHASE_NOT_FOUND", "Purchase with ID '" + purchaseId + "' not found");
    }
}
