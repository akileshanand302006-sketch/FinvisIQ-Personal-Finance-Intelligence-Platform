package com.smartfinance.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SIPCalculationRequest {
    @NotNull(message = "Monthly investment is required")
    @DecimalMin(value = "100.0", message = "Monthly investment must be at least 100")
    private Double monthlyInvestment;

    @NotNull(message = "Expected annual return rate is required")
    @DecimalMin(value = "0.1", message = "Annual return rate must be positive")
    private Double expectedAnnualReturn;

    @NotNull(message = "Tenure in years is required")
    @Min(value = 1, message = "Tenure must be at least 1 year")
    private Integer tenureYears;

    public Double getMonthlyInvestment() { return monthlyInvestment; }
    public void setMonthlyInvestment(Double monthlyInvestment) { this.monthlyInvestment = monthlyInvestment; }

    public Double getExpectedAnnualReturn() { return expectedAnnualReturn; }
    public void setExpectedAnnualReturn(Double expectedAnnualReturn) { this.expectedAnnualReturn = expectedAnnualReturn; }

    public Integer getTenureYears() { return tenureYears; }
    public void setTenureYears(Integer tenureYears) { this.tenureYears = tenureYears; }
}
