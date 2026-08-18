package com.smartfinance.model;

import java.time.LocalDate;

/**
 * Investment model for tracking user investments.
 * Demonstrates: Encapsulation, OOP.
 */
public class Investment {
    private int investmentId;
    private int userId;
    private String type; // "SIP", "FIXED_DEPOSIT", "STOCKS", "MUTUAL_FUND", "PPF", "GOLD"
    private double amount;
    private double returnRate; // Annual return rate in percentage
    private LocalDate startDate;

    public Investment() {}

    public Investment(int userId, String type, double amount,
                      double returnRate, LocalDate startDate) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.returnRate = returnRate;
        this.startDate = startDate;
    }

    public Investment(int investmentId, int userId, String type, double amount,
                      double returnRate, LocalDate startDate) {
        this.investmentId = investmentId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.returnRate = returnRate;
        this.startDate = startDate;
    }

    // Getters and Setters
    public int getInvestmentId() { return investmentId; }
    public void setInvestmentId(int investmentId) { this.investmentId = investmentId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getReturnRate() { return returnRate; }
    public void setReturnRate(double returnRate) { this.returnRate = returnRate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    /** Returns readable type name */
    public String getDisplayType() {
        return switch (type) {
            case "SIP" -> "SIP";
            case "FIXED_DEPOSIT" -> "Fixed Deposit";
            case "STOCKS" -> "Stocks";
            case "MUTUAL_FUND" -> "Mutual Fund";
            case "PPF" -> "PPF";
            case "GOLD" -> "Gold";
            default -> type;
        };
    }

    @Override
    public String toString() {
        return "Investment{id=" + investmentId + ", type='" + type +
               "', amount=" + amount + ", returnRate=" + returnRate + "%}";
    }
}
