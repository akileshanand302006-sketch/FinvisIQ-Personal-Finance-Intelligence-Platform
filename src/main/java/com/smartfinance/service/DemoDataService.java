package com.smartfinance.service;

import com.smartfinance.dao.*;
import com.smartfinance.model.*;

import java.time.LocalDate;

/**
 * Demo Data Service — Generates realistic sample data for demo users.
 */
public class DemoDataService {
    private final TransactionDAO transactionDAO;
    private final GoalDAO goalDAO;
    private final InvestmentDAO investmentDAO;
    private final BudgetDAO budgetDAO;
    private final SubscriptionDAO subscriptionDAO;

    public DemoDataService() {
        this.transactionDAO = new TransactionDAO();
        this.goalDAO = new GoalDAO();
        this.investmentDAO = new InvestmentDAO();
        this.budgetDAO = new BudgetDAO();
        this.subscriptionDAO = new SubscriptionDAO();
    }

    public void seedDemoData(int userId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            // In cloud client mode, user data is securely persisted in Aiven MySQL.
            // Avoid making 5 redundant, synchronous network roundtrips on every dashboard open.
            return;
        }
        // Sample transactions
        if (transactionDAO.findByUserId(userId).isEmpty()) {
            transactionDAO.insert(new Transaction(userId, 85000, "INCOME", "Salary", LocalDate.now().minusDays(15), "Monthly Salary", "Bank Transfer"));
            transactionDAO.insert(new Transaction(userId, 12000, "INCOME", "Freelance", LocalDate.now().minusDays(5), "Web Development Project", "UPI"));
            transactionDAO.insert(new Transaction(userId, 24000, "EXPENSE", "Housing/Rent", LocalDate.now().minusDays(14), "Apartment Rent", "Bank Transfer"));
            transactionDAO.insert(new Transaction(userId, 6500, "EXPENSE", "Food & Dining", LocalDate.now().minusDays(10), "Groceries & Dining out", "Debit Card"));
            transactionDAO.insert(new Transaction(userId, 3200, "EXPENSE", "Transportation", LocalDate.now().minusDays(8), "Fuel & Metro Pass", "UPI"));
            transactionDAO.insert(new Transaction(userId, 4500, "EXPENSE", "Bills & Utilities", LocalDate.now().minusDays(12), "Electricity & Broadband", "UPI"));
        }

        // Sample goals
        if (goalDAO.findByUserId(userId).isEmpty()) {
            goalDAO.insert(new Goal(0, userId, "Emergency Reserve", 150000, 45000, LocalDate.now().plusMonths(8), "ACTIVE", "HIGH", "Emergency Fund", 12000, 0));
            goalDAO.insert(new Goal(0, userId, "Europe Vacation", 200000, 80000, LocalDate.now().plusMonths(14), "ACTIVE", "MEDIUM", "Vacation", 10000, 0));
        }

        // Sample investments
        if (investmentDAO.findByUserId(userId).isEmpty()) {
            investmentDAO.insert(new Investment(userId, "SIP", 10000, 12.0, LocalDate.now().minusMonths(6)));
            investmentDAO.insert(new Investment(userId, "FIXED_DEPOSIT", 50000, 7.2, LocalDate.now().minusMonths(12)));
        }

        // Sample budgets
        if (budgetDAO.findByUserId(userId).isEmpty()) {
            budgetDAO.insert(new Budget(userId, "Food & Dining", 10000, "MONTHLY", 80.0));
            budgetDAO.insert(new Budget(userId, "Transportation", 5000, "MONTHLY", 80.0));
            budgetDAO.insert(new Budget(userId, "Entertainment", 4000, "MONTHLY", 80.0));
        }

        // Sample subscriptions
        if (subscriptionDAO.findByUserId(userId).isEmpty()) {
            subscriptionDAO.insert(new Subscription(userId, "Netflix Premium", 649, "MONTHLY", LocalDate.now().plusDays(12), "Entertainment", "ACTIVE"));
            subscriptionDAO.insert(new Subscription(userId, "Spotify Duo", 149, "MONTHLY", LocalDate.now().plusDays(5), "Entertainment", "ACTIVE"));
        }
    }
}
