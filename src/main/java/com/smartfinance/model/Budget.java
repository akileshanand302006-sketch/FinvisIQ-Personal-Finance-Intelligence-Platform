package com.smartfinance.model;

import java.time.LocalDateTime;

/**
 * Budget Model — Category spending limits and thresholds.
 * Extended for Finora Budget System.
 */
public class Budget {
    private int budgetId;
    private int userId;
    private String category;
    private double budgetAmount;
    private String period = "MONTHLY";
    private double warningThreshold = 80.0; // Percentage
    private LocalDateTime createdAt = LocalDateTime.now();

    public Budget() {}

    public Budget(int userId, String category, double budgetAmount, String period, double warningThreshold) {
        this.userId = userId;
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.period = period != null ? period : "MONTHLY";
        this.warningThreshold = warningThreshold > 0 ? warningThreshold : 80.0;
        this.createdAt = LocalDateTime.now();
    }

    public Budget(int budgetId, int userId, String category, double budgetAmount, String period, double warningThreshold, LocalDateTime createdAt) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.period = period != null ? period : "MONTHLY";
        this.warningThreshold = warningThreshold > 0 ? warningThreshold : 80.0;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // Getters and Setters
    public int getBudgetId() { return budgetId; }
    public void setBudgetId(int budgetId) { this.budgetId = budgetId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(double budgetAmount) { this.budgetAmount = budgetAmount; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public double getWarningThreshold() { return warningThreshold; }
    public void setWarningThreshold(double warningThreshold) { this.warningThreshold = warningThreshold; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
