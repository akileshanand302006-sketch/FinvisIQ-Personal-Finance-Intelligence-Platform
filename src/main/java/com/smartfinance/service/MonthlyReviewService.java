package com.smartfinance.service;

import com.smartfinance.dao.TransactionDAO;
import java.util.HashMap;

/**
 * Monthly Review Service — Generates monthly financial scorecards and evaluations.
 */
public class MonthlyReviewService {
    private final TransactionDAO transactionDAO;

    public MonthlyReviewService() {
        this.transactionDAO = new TransactionDAO();
    }

    public String generateMonthlySummary(int userId) {
        double income = transactionDAO.getTotalIncome(userId);
        double expenses = transactionDAO.getTotalExpenses(userId);
        double balance = income - expenses;
        double savingsRate = income > 0 ? (balance / income) * 100.0 : 0;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Monthly Financial Summary:\n"));
        sb.append(String.format("• Total Income Tracked: \u20B9%,.2f\n", income));
        sb.append(String.format("• Total Expenses: \u20B9%,.2f\n", expenses));
        sb.append(String.format("• Net Savings: \u20B9%,.2f (%.1f%% Savings Rate)\n", balance, savingsRate));

        HashMap<String, Double> categoryTotals = transactionDAO.getCategoryTotals(userId, "EXPENSE");
        if (!categoryTotals.isEmpty()) {
            String topCategory = "";
            double topAmt = 0;
            for (var entry : categoryTotals.entrySet()) {
                if (entry.getValue() > topAmt) {
                    topAmt = entry.getValue();
                    topCategory = entry.getKey();
                }
            }
            sb.append(String.format("• Highest Expense Category: %s (\u20B9%,.2f)\n", topCategory, topAmt));
        }

        return sb.toString();
    }
}
