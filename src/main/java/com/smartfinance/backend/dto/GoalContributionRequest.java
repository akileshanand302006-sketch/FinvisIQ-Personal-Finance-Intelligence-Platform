package com.smartfinance.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class GoalContributionRequest {
    @NotNull(message = "Contribution amount is required")
    @DecimalMin(value = "0.01", message = "Contribution amount must be greater than 0")
    private Double amount;

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
