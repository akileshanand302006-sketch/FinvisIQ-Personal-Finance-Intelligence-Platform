package com.smartfinance.service;

import com.smartfinance.dao.LiabilityDAO;
import com.smartfinance.model.Liability;

import java.util.ArrayList;

/**
 * Debt Service — EMI calculation & debt health scoring.
 */
public class DebtService {
    private final LiabilityDAO liabilityDAO;

    public DebtService() {
        this.liabilityDAO = new LiabilityDAO();
    }

    /** Calculate monthly EMI formula: P * r * (1+r)^n / ((1+r)^n - 1) */
    public double calculateEMI(double principal, double annualRate, int tenureMonths) {
        if (principal <= 0 || tenureMonths <= 0) return 0;
        if (annualRate <= 0) return principal / tenureMonths;

        double monthlyRate = annualRate / 12.0 / 100.0;
        double factor = Math.pow(1 + monthlyRate, tenureMonths);
        double emi = principal * monthlyRate * factor / (factor - 1);
        return Math.round(emi * 100.0) / 100.0;
    }

    public double getTotalMonthlyEMI(int userId) {
        ArrayList<Liability> liabilities = liabilityDAO.findByUserId(userId);
        double total = 0;
        for (Liability l : liabilities) {
            total += l.getEmi();
        }
        return total;
    }
}
