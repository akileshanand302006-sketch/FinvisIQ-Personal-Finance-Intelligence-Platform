package com.smartfinance.service;

import com.smartfinance.dao.BudgetDAO;
import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.model.Budget;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Budget Service — Calculates category spending limits, percentages, and alerts.
 */
public class BudgetService {
    private final BudgetDAO budgetDAO;
    private final TransactionDAO transactionDAO;

    public BudgetService() {
        this.budgetDAO = new BudgetDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public ArrayList<Budget> getUserBudgets(int userId) {
        return budgetDAO.findByUserId(userId);
    }

    public double getCategorySpent(int userId, String category) {
        HashMap<String, Double> categoryTotals = transactionDAO.getCategoryTotals(userId, "EXPENSE");
        return categoryTotals.getOrDefault(category, 0.0);
    }

    public double getSpentPercentage(Budget budget) {
        if (budget.getBudgetAmount() <= 0) return 0;
        double spent = getCategorySpent(budget.getUserId(), budget.getCategory());
        return Math.min(100.0, (spent / budget.getBudgetAmount()) * 100.0);
    }

    /** Status: NORMAL, WARNING, CRITICAL, EXCEEDED */
    public String getBudgetStatus(Budget budget) {
        double spent = getCategorySpent(budget.getUserId(), budget.getCategory());
        double limit = budget.getBudgetAmount();

        if (limit <= 0) return "NORMAL";
        double pct = (spent / limit) * 100.0;

        if (pct >= 100.0) return "EXCEEDED";
        if (pct >= 90.0) return "CRITICAL";
        if (pct >= budget.getWarningThreshold()) return "WARNING";
        return "NORMAL";
    }
}
