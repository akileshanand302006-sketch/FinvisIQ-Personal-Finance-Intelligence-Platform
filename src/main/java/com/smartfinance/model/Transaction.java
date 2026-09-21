package com.smartfinance.model;

import java.time.LocalDate;

/**
 * Transaction model for income and expense tracking.
 * Extended for FinvisIQ with paymentMethod (Cash, UPI, Debit Card, Credit Card, Bank Transfer).
 * Demonstrates: Encapsulation, Java Date API.
 */
public class Transaction {
    private int transactionId;
    private int userId;
    private double amount;
    private String type; // "INCOME" or "EXPENSE"
    private String category;
    private LocalDate date;
    private String description;
    private String paymentMethod = "Cash";

    public Transaction() {}

    public Transaction(int userId, double amount, String type, String category,
                       LocalDate date, String description) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.description = description;
        this.paymentMethod = "Cash";
    }

    public Transaction(int userId, double amount, String type, String category,
                       LocalDate date, String description, String paymentMethod) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.description = description;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Cash";
    }

    public Transaction(int transactionId, int userId, double amount, String type,
                       String category, LocalDate date, String description) {
        this(transactionId, userId, amount, type, category, date, description, "Cash");
    }

    public Transaction(int transactionId, int userId, double amount, String type,
                       String category, LocalDate date, String description, String paymentMethod) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.description = description;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Cash";
    }

    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isIncome() { return "INCOME".equalsIgnoreCase(type); }
    public boolean isExpense() { return "EXPENSE".equalsIgnoreCase(type); }

    @Override
    public String toString() {
        return "Transaction{id=" + transactionId + ", amount=" + amount +
               ", type='" + type + "', category='" + category + "', date=" + date + "}";
    }
}
