package com.smartfinance.model;

import java.time.LocalDate;

/**
 * Subscription Model — Recurring subscription tracking (Netflix, Spotify, AWS, etc.).
 */
public class Subscription {
    private int subscriptionId;
    private int userId;
    private String serviceName;
    private double amount;
    private String billingCycle = "MONTHLY"; // MONTHLY, YEARLY
    private LocalDate nextBillingDate;
    private String category = "Entertainment";
    private String status = "ACTIVE"; // ACTIVE, CANCELLED

    public Subscription() {}

    public Subscription(int userId, String serviceName, double amount, String billingCycle, LocalDate nextBillingDate, String category, String status) {
        this.userId = userId;
        this.serviceName = serviceName;
        this.amount = amount;
        this.billingCycle = billingCycle != null ? billingCycle : "MONTHLY";
        this.nextBillingDate = nextBillingDate;
        this.category = category != null ? category : "Entertainment";
        this.status = status != null ? status : "ACTIVE";
    }

    public Subscription(int subscriptionId, int userId, String serviceName, double amount, String billingCycle, LocalDate nextBillingDate, String category, String status) {
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.serviceName = serviceName;
        this.amount = amount;
        this.billingCycle = billingCycle != null ? billingCycle : "MONTHLY";
        this.nextBillingDate = nextBillingDate;
        this.category = category != null ? category : "Entertainment";
        this.status = status != null ? status : "ACTIVE";
    }

    // Getters and Setters
    public int getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(int subscriptionId) { this.subscriptionId = subscriptionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public LocalDate getNextBillingDate() { return nextBillingDate; }
    public void setNextBillingDate(LocalDate nextBillingDate) { this.nextBillingDate = nextBillingDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() { return "ACTIVE".equalsIgnoreCase(status); }

    public double getMonthlyCost() {
        if ("YEARLY".equalsIgnoreCase(billingCycle)) {
            return amount / 12.0;
        }
        return amount;
    }
}
