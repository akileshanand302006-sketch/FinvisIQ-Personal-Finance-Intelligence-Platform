package com.smartfinance.model;

import java.time.LocalDate;

/**
 * Recurring Transaction Model — Daily, Weekly, Monthly, Yearly income/expense automation.
 */
public class RecurringTransaction {
    private int id;
    private int userId;
    private double amount;
    private String type; // INCOME, EXPENSE
    private String category;
    private String frequency = "MONTHLY"; // DAILY, WEEKLY, MONTHLY, YEARLY
    private LocalDate nextDate;
    private String description;
    private boolean active = true;

    public RecurringTransaction() {}

    public RecurringTransaction(int userId, double amount, String type, String category, String frequency, LocalDate nextDate, String description, boolean active) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.frequency = frequency != null ? frequency : "MONTHLY";
        this.nextDate = nextDate;
        this.description = description;
        this.active = active;
    }

    public RecurringTransaction(int id, int userId, double amount, String type, String category, String frequency, LocalDate nextDate, String description, boolean active) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.frequency = frequency != null ? frequency : "MONTHLY";
        this.nextDate = nextDate;
        this.description = description;
        this.active = active;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public LocalDate getNextDate() { return nextDate; }
    public void setNextDate(LocalDate nextDate) { this.nextDate = nextDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
