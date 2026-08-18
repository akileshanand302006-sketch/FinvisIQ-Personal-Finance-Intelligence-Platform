package com.smartfinance.service;

import com.smartfinance.dao.TransactionDAO;

/**
 * Emergency Fund Service — Calculates required 3/6/9/12 month emergency buffer.
 */
public class EmergencyFundService {
    private final TransactionDAO transactionDAO;

    public EmergencyFundService() {
        this.transactionDAO = new TransactionDAO();
    }

    public double getAverageMonthlyExpense(int userId) {
        double totalExp = transactionDAO.getTotalExpenses(userId);
        int count = Math.max(1, transactionDAO.getMonthlyTotals(userId, "EXPENSE").size());
        return totalExp / count;
    }

    public double getTargetEmergencyFund(int userId, int months) {
        return getAverageMonthlyExpense(userId) * months;
    }
}
