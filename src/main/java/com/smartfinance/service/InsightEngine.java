package com.smartfinance.service;

import com.smartfinance.dao.*;
import com.smartfinance.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Insight Engine — Comprehensive rule-based financial intelligence platform.
 * Analyzes cash flow, debt ratio, savings velocity, category overspending, and investment allocations.
 */
public class InsightEngine {
    private final TransactionDAO transactionDAO;
    private final BudgetDAO budgetDAO;
    private final GoalDAO goalDAO;

    public InsightEngine() {
        this.transactionDAO = new TransactionDAO();
        this.budgetDAO = new BudgetDAO();
        this.goalDAO = new GoalDAO();
    }

    public ArrayList<FinancialInsight> generateInsights(int userId) {
        ArrayList<FinancialInsight> insights = new ArrayList<>();

        double income = transactionDAO.getTotalIncome(userId);
        double expenses = transactionDAO.getTotalExpenses(userId);
        double balance = income - expenses;
        double savingsRate = income > 0 ? (balance / income) * 100.0 : 0;

        // 1. Savings Rate Analysis
        if (savingsRate >= 30.0) {
            insights.add(new FinancialInsight(
                FinancialInsight.Priority.SUCCESS, "Savings",
                "Excellent Savings Rate",
                String.format("You are saving %.1f%% of your total income. Keep this momentum to achieve financial freedom faster!", savingsRate)
            ));
        } else if (savingsRate > 0 && savingsRate < 20.0) {
            insights.add(new FinancialInsight(
                FinancialInsight.Priority.WARNING, "Savings",
                "Low Savings Rate Alert",
                String.format("Your savings rate is currently %.1f%%. Experts recommend aiming for at least 20%% monthly savings.", savingsRate)
            ));
        } else if (expenses > income && income > 0) {
            insights.add(new FinancialInsight(
                FinancialInsight.Priority.CRITICAL, "Cash Flow",
                "Negative Cash Flow Warning",
                String.format("Your expenses (\u20B9%,.0f) exceed your income (\u20B9%,.0f) by \u20B9%,.0f this period!", expenses, income, Math.abs(balance))
            ));
        }

        // 2. Category Concentration Analysis
        HashMap<String, Double> categoryExpenses = transactionDAO.getCategoryTotals(userId, "EXPENSE");
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            if (expenses > 0) {
                double pct = (entry.getValue() / expenses) * 100.0;
                if (pct > 35.0) {
                    insights.add(new FinancialInsight(
                        FinancialInsight.Priority.WARNING, "Spending",
                        "High Spending Concentration in " + entry.getKey(),
                        String.format("The category '%s' accounts for %.1f%% of your total expenses (\u20B9%,.0f). Consider setting a budget constraint.", entry.getKey(), pct, entry.getValue())
                    ));
                }
            }
        }

        // 3. Active Budget Warnings
        ArrayList<Budget> budgets = budgetDAO.findByUserId(userId);
        for (Budget b : budgets) {
            double spent = categoryExpenses.getOrDefault(b.getCategory(), 0.0);
            if (b.getBudgetAmount() > 0) {
                double usedPct = (spent / b.getBudgetAmount()) * 100.0;
                if (usedPct >= 100.0) {
                    insights.add(new FinancialInsight(
                        FinancialInsight.Priority.CRITICAL, "Budget",
                        "Budget Exceeded: " + b.getCategory(),
                        String.format("You have spent \u20B9%,.0f against your \u20B9%,.0f limit (%.0f%% used).", spent, b.getBudgetAmount(), usedPct)
                    ));
                } else if (usedPct >= b.getWarningThreshold()) {
                    insights.add(new FinancialInsight(
                        FinancialInsight.Priority.WARNING, "Budget",
                        "Budget Warning: " + b.getCategory(),
                        String.format("You have reached %.0f%% of your \u20B9%,.0f limit for %s.", usedPct, b.getBudgetAmount(), b.getCategory())
                    ));
                }
            }
        }

        // 4. Goals Progress Check
        ArrayList<Goal> goals = goalDAO.findByUserId(userId);
        int activeGoals = 0;
        for (Goal g : goals) {
            if (g.isActive()) {
                activeGoals++;
                if (g.getProgress() >= 100.0) {
                    insights.add(new FinancialInsight(
                        FinancialInsight.Priority.SUCCESS, "Goals",
                        "Goal Reached: " + g.getGoalName(),
                        String.format("Congratulations! You have reached 100%% of your target for '%s'.", g.getGoalName())
                    ));
                }
            }
        }

        if (activeGoals == 0) {
            insights.add(new FinancialInsight(
                FinancialInsight.Priority.INFO, "Goals",
                "Set Your First Savings Goal",
                "Setting clear financial targets improves savings discipline by over 40%. Head to Goals to set one up!"
            ));
        }

        return insights;
    }
}
