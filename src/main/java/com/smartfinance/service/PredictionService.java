package com.smartfinance.service;

import com.smartfinance.dao.TransactionDAO;
import java.util.*;

/**
 * Prediction Service - Monthly expense prediction using moving averages.
 * Demonstrates: ArrayList, HashMap, Java Math API.
 */
public class PredictionService {
    private final TransactionDAO transactionDAO;

    public PredictionService() {
        this.transactionDAO = new TransactionDAO();
    }

    /**
     * Predict next month's total expenses using 3-month moving average.
     */
    public double predictNextMonthExpenses(int userId) {
        HashMap<String, Double> monthlyExpenses = transactionDAO.getMonthlyTotals(userId, "EXPENSE");
        if (monthlyExpenses.isEmpty()) return 0;

        // Sort months and get last 3
        List<String> sortedMonths = new ArrayList<>(monthlyExpenses.keySet());
        Collections.sort(sortedMonths);

        int count = Math.min(3, sortedMonths.size());
        double sum = 0;
        for (int i = sortedMonths.size() - count; i < sortedMonths.size(); i++) {
            sum += monthlyExpenses.get(sortedMonths.get(i));
        }
        return Math.round((sum / count) * 100.0) / 100.0;
    }

    /**
     * Predict next month's income using moving average.
     */
    public double predictNextMonthIncome(int userId) {
        HashMap<String, Double> monthlyIncome = transactionDAO.getMonthlyTotals(userId, "INCOME");
        if (monthlyIncome.isEmpty()) return 0;

        List<String> sortedMonths = new ArrayList<>(monthlyIncome.keySet());
        Collections.sort(sortedMonths);

        int count = Math.min(3, sortedMonths.size());
        double sum = 0;
        for (int i = sortedMonths.size() - count; i < sortedMonths.size(); i++) {
            sum += monthlyIncome.get(sortedMonths.get(i));
        }
        return Math.round((sum / count) * 100.0) / 100.0;
    }

    /**
     * Get category-wise predicted expenses using HashMap.
     */
    public HashMap<String, Double> predictCategoryExpenses(int userId) {
        HashMap<String, Double> categoryTotals = transactionDAO.getCategoryTotals(userId, "EXPENSE");
        HashMap<String, Double> predictions = new HashMap<>();

        // Category totals are used directly below, no aggregate needed here

        // Get monthly count for averaging
        HashMap<String, Double> monthlyTotals = transactionDAO.getMonthlyTotals(userId, "EXPENSE");
        int monthCount = Math.max(1, monthlyTotals.size());

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            double monthlyAvg = entry.getValue() / monthCount;
            predictions.put(entry.getKey(), Math.round(monthlyAvg * 100.0) / 100.0);
        }

        return predictions;
    }

    /**
     * Get expense trend direction.
     * @return "INCREASING", "DECREASING", or "STABLE"
     */
    public String getExpenseTrend(int userId) {
        HashMap<String, Double> monthlyExpenses = transactionDAO.getMonthlyTotals(userId, "EXPENSE");
        if (monthlyExpenses.size() < 2) return "STABLE";

        List<String> sortedMonths = new ArrayList<>(monthlyExpenses.keySet());
        Collections.sort(sortedMonths);

        int size = sortedMonths.size();
        double lastMonth = monthlyExpenses.get(sortedMonths.get(size - 1));
        double prevMonth = monthlyExpenses.get(sortedMonths.get(size - 2));

        double change = ((lastMonth - prevMonth) / prevMonth) * 100;
        if (change > 10) return "INCREASING";
        if (change < -10) return "DECREASING";
        return "STABLE";
    }

    /**
     * Get monthly expense data for charting.
     */
    public LinkedHashMap<String, Double> getMonthlyExpenseHistory(int userId) {
        HashMap<String, Double> raw = transactionDAO.getMonthlyTotals(userId, "EXPENSE");
        List<String> sortedMonths = new ArrayList<>(raw.keySet());
        Collections.sort(sortedMonths);

        LinkedHashMap<String, Double> sorted = new LinkedHashMap<>();
        for (String month : sortedMonths) {
            sorted.put(month, raw.get(month));
        }
        return sorted;
    }

    /**
     * Get monthly income data for charting.
     */
    public LinkedHashMap<String, Double> getMonthlyIncomeHistory(int userId) {
        HashMap<String, Double> raw = transactionDAO.getMonthlyTotals(userId, "INCOME");
        List<String> sortedMonths = new ArrayList<>(raw.keySet());
        Collections.sort(sortedMonths);

        LinkedHashMap<String, Double> sorted = new LinkedHashMap<>();
        for (String month : sortedMonths) {
            sorted.put(month, raw.get(month));
        }
        return sorted;
    }
}
