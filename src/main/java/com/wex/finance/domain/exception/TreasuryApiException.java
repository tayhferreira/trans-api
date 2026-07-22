package com.wex.finance.domain.exception;

public class TreasuryApiException extends DomainException {

    public TreasuryApiException(String message) {
        super("TREASURY_API_ERROR", message);
    }

    public TreasuryApiException(String message, Throwable cause) {
        super("TREASURY_API_ERROR", message, cause);
    }
}
