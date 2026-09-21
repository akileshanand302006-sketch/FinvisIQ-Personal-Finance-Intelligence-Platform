package com.smartfinance.model;

import java.time.LocalDate;

/**
 * Goal model for savings goal tracking.
 * Extended for FinvisIQ with priority, category, monthly contribution & expected returns.
 * Demonstrates: Encapsulation, OOP.
 */
public class Goal {
    private int goalId;
    private int userId;
    private String goalName;
    private double targetAmount;
    private double savedAmount;
    private LocalDate deadline;
    private String status; // "ACTIVE", "COMPLETED", "FAILED"
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, URGENT
    private String category = "General"; // Emergency Fund, Retirement, Car, House, Education, Vacation, General
    private double monthlyContribution;
    private double expectedReturn;

    public Goal() {}

    public Goal(int userId, String goalName, double targetAmount,
                double savedAmount, LocalDate deadline, String status) {
        this.userId = userId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.deadline = deadline;
        this.status = status;
    }

    public Goal(int goalId, int userId, String goalName, double targetAmount,
                double savedAmount, LocalDate deadline, String status) {
        this.goalId = goalId;
        this.userId = userId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.deadline = deadline;
        this.status = status;
    }

    public Goal(int goalId, int userId, String goalName, double targetAmount, double savedAmount,
                LocalDate deadline, String status, String priority, String category, double monthlyContribution, double expectedReturn) {
        this.goalId = goalId;
        this.userId = userId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.deadline = deadline;
        this.status = status;
        this.priority = priority != null ? priority : "MEDIUM";
        this.category = category != null ? category : "General";
        this.monthlyContribution = monthlyContribution;
        this.expectedReturn = expectedReturn;
    }

    public Goal(int userId, String goalName, double targetAmount, double savedAmount,
                LocalDate deadline, String status, String priority, String category, double monthlyContribution, double expectedReturn) {
        this.userId = userId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.deadline = deadline;
        this.status = status;
        this.priority = priority != null ? priority : "MEDIUM";
        this.category = category != null ? category : "General";
        this.monthlyContribution = monthlyContribution;
        this.expectedReturn = expectedReturn;
    }

    // Getters and Setters
    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getGoalName() { return goalName; }
    public void setGoalName(String goalName) { this.goalName = goalName; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getSavedAmount() { return savedAmount; }
    public void setSavedAmount(double savedAmount) { this.savedAmount = savedAmount; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getMonthlyContribution() { return monthlyContribution; }
    public void setMonthlyContribution(double monthlyContribution) { this.monthlyContribution = monthlyContribution; }

    public double getExpectedReturn() { return expectedReturn; }
    public void setExpectedReturn(double expectedReturn) { this.expectedReturn = expectedReturn; }

    /** Returns progress percentage (0-100) */
    public double getProgress() {
        if (targetAmount <= 0) return 0;
        return Math.min(100.0, (savedAmount / targetAmount) * 100.0);
    }

    public boolean isCompleted() { return "COMPLETED".equalsIgnoreCase(status); }
    public boolean isActive() { return "ACTIVE".equalsIgnoreCase(status); }

    @Override
    public String toString() {
        return "Goal{id=" + goalId + ", name='" + goalName + "', progress=" +
               String.format("%.1f%%", getProgress()) + ", status='" + status + "'}";
    }
}
