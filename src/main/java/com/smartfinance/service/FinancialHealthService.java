package com.smartfinance.service;

import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.dao.GoalDAO;
import com.smartfinance.dao.InvestmentDAO;
import com.smartfinance.model.Goal;
import com.smartfinance.model.Investment;

import java.util.*;

/**
 * Financial Health Score Service - Composite scoring system.
 * Score: 0-100, based on multiple financial parameters.
 * Demonstrates: Java Math API, HashMap, ArrayList.
 */
public class FinancialHealthService {
    private final TransactionDAO transactionDAO;
    private final GoalDAO goalDAO;
    private final InvestmentDAO investmentDAO;

    public FinancialHealthService() {
        this.transactionDAO = new TransactionDAO();
        this.goalDAO = new GoalDAO();
        this.investmentDAO = new InvestmentDAO();
    }

    /**
     * Calculate overall financial health score (0-100).
     * Breakdown:
     * - Savings Ratio: 30 points
     * - Expense Stability: 20 points
     * - Goal Progress: 20 points
     * - Investment Score: 15 points
     * - Balance Health: 15 points
     */
    public int calculateHealthScore(int userId) {
        return calculateHealthScore(getScoreBreakdown(userId));
    }

    public int calculateHealthScore(Map<String, Double> breakdown) {
        if (breakdown == null || breakdown.isEmpty()) return 50;
        double sum = breakdown.values().stream().mapToDouble(Double::doubleValue).sum();
        return Math.min(100, Math.max(0, (int) Math.round(sum)));
    }

    /**
     * Get detailed score breakdown.
     */
    public HashMap<String, Double> getScoreBreakdown(int userId) {
        HashMap<String, Double> breakdown = new HashMap<>();
        breakdown.put("Savings Ratio", calculateSavingsScore(userId));
        breakdown.put("Expense Stability", calculateStabilityScore(userId));
        breakdown.put("Goal Progress", calculateGoalScore(userId));
        breakdown.put("Investment Score", calculateInvestmentScore(userId));
        breakdown.put("Balance Health", calculateBalanceScore(userId));
        return breakdown;
    }

    /**
     * Get health rating label.
     */
    public String getHealthRating(int score) {
        if (score >= 80) return "Excellent";
        if (score >= 60) return "Good";
        if (score >= 40) return "Fair";
        if (score >= 20) return "Poor";
        return "Critical";
    }

    /**
     * Get health color hex code.
     */
    public String getHealthColor(int score) {
        if (score >= 80) return "#2ECC71";      // Green
        if (score >= 60) return "#27AE60";      // Dark Green
        if (score >= 40) return "#F39C12";      // Orange
        if (score >= 20) return "#E67E22";      // Dark Orange
        return "#E74C3C";                        // Red
    }

    // === Private Score Components ===

    /** Savings score: max 30 points. Based on (income - expenses) / income. */
    private double calculateSavingsScore(int userId) {
        double income = transactionDAO.getTotalIncome(userId);
        double expenses = transactionDAO.getTotalExpenses(userId);
        if (income <= 0) return 15; // Neutral if no income data

        double savingsRatio = (income - expenses) / income;
        if (savingsRatio >= 0.3) return 30;
        if (savingsRatio >= 0.2) return 25;
        if (savingsRatio >= 0.1) return 18;
        if (savingsRatio >= 0) return 10;
        return 0; // Negative savings
    }

    /** Expense stability: max 20 points. Low variance = high score. */
    private double calculateStabilityScore(int userId) {
        HashMap<String, Double> monthlyExpenses = transactionDAO.getMonthlyTotals(userId, "EXPENSE");
        if (monthlyExpenses.size() < 2) return 15; // Neutral

        List<Double> values = new ArrayList<>(monthlyExpenses.values());
        double mean = values.stream().mapToDouble(v -> v).average().orElse(0);
        if (mean == 0) return 15;

        double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0);
        double cv = Math.sqrt(variance) / mean; // Coefficient of variation

        if (cv < 0.1) return 20;   // Very stable
        if (cv < 0.2) return 16;
        if (cv < 0.3) return 12;
        if (cv < 0.5) return 8;
        return 4; // Highly volatile
    }

    /** Goal progress: max 20 points. Average progress across active goals. */
    private double calculateGoalScore(int userId) {
        ArrayList<Goal> goals = goalDAO.findByUserId(userId);
        if (goals.isEmpty()) return 10; // Neutral

        double totalProgress = 0;
        int activeCount = 0;
        for (Goal goal : goals) {
            if (goal.isActive()) {
                totalProgress += goal.getProgress();
                activeCount++;
            } else if (goal.isCompleted()) {
                totalProgress += 100;
                activeCount++;
            }
        }
        if (activeCount == 0) return 10;

        double avgProgress = totalProgress / activeCount;
        return (avgProgress / 100.0) * 20.0;
    }

    /** Investment score: max 15 points. Based on diversity and presence. */
    private double calculateInvestmentScore(int userId) {
        ArrayList<Investment> investments = investmentDAO.findByUserId(userId);
        if (investments.isEmpty()) return 0;

        Set<String> types = new HashSet<>();
        for (Investment inv : investments) {
            types.add(inv.getType());
        }

        int diversity = types.size();
        if (diversity >= 4) return 15;
        if (diversity >= 3) return 12;
        if (diversity >= 2) return 9;
        return 6; // At least one investment
    }

    /** Balance health: max 15 points. Positive balance relative to expenses. */
    private double calculateBalanceScore(int userId) {
        double income = transactionDAO.getTotalIncome(userId);
        double expenses = transactionDAO.getTotalExpenses(userId);
        double balance = income - expenses;

        if (expenses <= 0) return 10; // Neutral
        double ratio = balance / expenses;

        if (ratio >= 3) return 15;
        if (ratio >= 1.5) return 12;
        if (ratio >= 0.5) return 8;
        if (ratio >= 0) return 4;
        return 0; // Negative balance
    }
}
