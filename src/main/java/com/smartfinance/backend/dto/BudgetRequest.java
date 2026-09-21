package com.smartfinance.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BudgetRequest {
    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "1.0", message = "Budget amount must be at least 1")
    private Double budgetAmount;

    private String period = "MONTHLY";
    private Double warningThreshold = 80.0;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(Double budgetAmount) { this.budgetAmount = budgetAmount; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Double getWarningThreshold() { return warningThreshold; }
    public void setWarningThreshold(Double warningThreshold) { this.warningThreshold = warningThreshold; }
}
