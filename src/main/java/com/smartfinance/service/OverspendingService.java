package com.smartfinance.service;

import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.dao.NotificationDAO;
import com.smartfinance.model.Notification;

import java.time.LocalDate;
import java.util.*;

/**
 * Overspending Detection Service - Detects unusual spending patterns.
 * Generates alerts when spending exceeds thresholds.
 * Demonstrates: HashMap, ArrayList, Rule-Based Logic.
 */
public class OverspendingService {
    private final TransactionDAO transactionDAO;
    private final NotificationDAO notificationDAO;

    private static final double OVERSPEND_THRESHOLD = 1.2; // 120% of average

    public OverspendingService() {
        this.transactionDAO = new TransactionDAO();
        this.notificationDAO = new NotificationDAO();
    }

    /**
     * Detect overspending and generate notifications.
     * Compares current month's spending vs average of previous months.
     */
    public ArrayList<String> detectOverspending(int userId) {
        ArrayList<String> alerts = new ArrayList<>();

        HashMap<String, Double> monthlyExpenses = transactionDAO.getMonthlyTotals(userId, "EXPENSE");
        if (monthlyExpenses.size() < 2) return alerts;

        // Get sorted months
        List<String> sortedMonths = new ArrayList<>(monthlyExpenses.keySet());
        Collections.sort(sortedMonths);

        String currentMonth = sortedMonths.get(sortedMonths.size() - 1);
        double currentTotal = monthlyExpenses.get(currentMonth);

        // Calculate average of previous months
        double prevSum = 0;
        int prevCount = 0;
        for (int i = 0; i < sortedMonths.size() - 1; i++) {
            prevSum += monthlyExpenses.get(sortedMonths.get(i));
            prevCount++;
        }

        if (prevCount > 0) {
            double avgPrev = prevSum / prevCount;
            double ratio = currentTotal / avgPrev;

            if (ratio >= OVERSPEND_THRESHOLD) {
                double overspendPct = (ratio - 1) * 100;
                String alert = String.format(
                    "\uD83D\uDEA8 Overspending Alert: This month's expenses (\u20B9%,.0f) are %.0f%% higher than your average (\u20B9%,.0f)!",
                    currentTotal, overspendPct, avgPrev
                );
                alerts.add(alert);

                // Generate notification
                notificationDAO.insert(new Notification(userId, alert, LocalDate.now(), false));
            }
        }

        // Category-wise overspending detection
        HashMap<String, Double> currentCategoryTotals = transactionDAO.getCategoryTotals(userId, "EXPENSE");
        for (Map.Entry<String, Double> entry : currentCategoryTotals.entrySet()) {
            String category = entry.getKey();
            double categoryTotal = entry.getValue();

            // Simple threshold check
            HashMap<String, Double> monthlyTotals = transactionDAO.getMonthlyTotals(userId, "EXPENSE");
            int monthCount = Math.max(1, monthlyTotals.size());
            double monthlyAvg = categoryTotal / monthCount;

            // Check if any category takes more than 40% of total monthly expenses
            double totalExpenses = transactionDAO.getTotalExpenses(userId);
            double monthlyTotalAvg = totalExpenses / monthCount;
            if (monthlyTotalAvg > 0 && (monthlyAvg / monthlyTotalAvg) > 0.4) {
                alerts.add(String.format(
                    "\u26A0\uFE0F High spending in '%s': \u20B9%,.0f/month (%.0f%% of total expenses). Consider budgeting.",
                    category, monthlyAvg, (monthlyAvg / monthlyTotalAvg) * 100
                ));
            }
        }

        return alerts;
    }

    /**
     * Check if user is on track with their budget.
     */
    public boolean isOnBudget(int userId) {
        double income = transactionDAO.getTotalIncome(userId);
        double expenses = transactionDAO.getTotalExpenses(userId);
        return expenses <= income;
    }

    /**
     * Get spending velocity (daily average for current period).
     */
    public double getDailySpendingRate(int userId) {
        HashMap<String, Double> monthlyExpenses = transactionDAO.getMonthlyTotals(userId, "EXPENSE");
        if (monthlyExpenses.isEmpty()) return 0;

        List<String> sortedMonths = new ArrayList<>(monthlyExpenses.keySet());
        Collections.sort(sortedMonths);

        double lastMonthExpense = monthlyExpenses.get(sortedMonths.get(sortedMonths.size() - 1));
        return Math.round((lastMonthExpense / 30.0) * 100.0) / 100.0;
    }
}
