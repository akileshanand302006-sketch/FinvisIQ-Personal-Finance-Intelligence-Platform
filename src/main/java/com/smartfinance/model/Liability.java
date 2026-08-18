package com.smartfinance.model;

/**
 * Liability Model — Loans, Credit Cards, Mortgages & EMI obligations.
 */
public class Liability {
    private int liabilityId;
    private int userId;
    private String name;
    private double principal;
    private double interestRate; // Annual %
    private int tenureMonths;
    private double emi;
    private double remainingBalance;
    private int dueDate; // Day of month (1-31)

    public Liability() {}

    public Liability(int userId, String name, double principal, double interestRate, int tenureMonths, double emi, double remainingBalance, int dueDate) {
        this.userId = userId;
        this.name = name;
        this.principal = principal;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.emi = emi;
        this.remainingBalance = remainingBalance;
        this.dueDate = dueDate;
    }

    public Liability(int liabilityId, int userId, String name, double principal, double interestRate, int tenureMonths, double emi, double remainingBalance, int dueDate) {
        this.liabilityId = liabilityId;
        this.userId = userId;
        this.name = name;
        this.principal = principal;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.emi = emi;
        this.remainingBalance = remainingBalance;
        this.dueDate = dueDate;
    }

    // Getters and Setters
    public int getLiabilityId() { return liabilityId; }
    public void setLiabilityId(int liabilityId) { this.liabilityId = liabilityId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrincipal() { return principal; }
    public void setPrincipal(double principal) { this.principal = principal; }

    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    public int getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(int tenureMonths) { this.tenureMonths = tenureMonths; }

    public double getEmi() { return emi; }
    public void setEmi(double emi) { this.emi = emi; }

    public double getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(double remainingBalance) { this.remainingBalance = remainingBalance; }

    public int getDueDate() { return dueDate; }
    public void setDueDate(int dueDate) { this.dueDate = dueDate; }
}
