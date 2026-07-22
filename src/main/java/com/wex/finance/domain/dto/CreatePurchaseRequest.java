package com.wex.finance.domain.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreatePurchaseRequest {

    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description must not exceed 50 characters")
    private String description;

    @NotBlank(message = "Transaction date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2})?$",
            message = "Date must be in YYYY-MM-DD or ISO 8601 format")
    private String transactionDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @DecimalMax(value = "999999999.99", message = "Amount must not exceed 999,999,999.99")
    private BigDecimal amount;

    public CreatePurchaseRequest() {}

    public CreatePurchaseRequest(String description, String transactionDate, BigDecimal amount) {
        this.description = description;
        this.transactionDate = transactionDate;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
