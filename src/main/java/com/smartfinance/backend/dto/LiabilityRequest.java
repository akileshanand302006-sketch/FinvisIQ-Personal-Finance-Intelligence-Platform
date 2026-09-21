package com.smartfinance.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LiabilityRequest {
    @NotBlank(message = "Liability name is required")
    private String name;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "0.0", message = "Principal cannot be negative")
    private Double principal;

    private Double interestRate = 0.0;
    private Integer tenureMonths = 12;
    private Double emi = 0.0;
    private Double remainingBalance = 0.0;
    private Integer dueDate = 5;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrincipal() { return principal; }
    public void setPrincipal(Double principal) { this.principal = principal; }

    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }

    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }

    public Double getEmi() { return emi; }
    public void setEmi(Double emi) { this.emi = emi; }

    public Double getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(Double remainingBalance) { this.remainingBalance = remainingBalance; }

    public Integer getDueDate() { return dueDate; }
    public void setDueDate(Integer dueDate) { this.dueDate = dueDate; }
}
