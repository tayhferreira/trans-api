package com.wex.finance.domain.exception;

public class InvalidPurchaseException extends DomainException {

    public InvalidPurchaseException(String message) {
        super("INVALID_PURCHASE", message);
    }
}
