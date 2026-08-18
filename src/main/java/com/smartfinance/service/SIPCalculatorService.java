package com.smartfinance.service;

/**
 * SIP (Systematic Investment Plan) Calculator Service.
 * Demonstrates: Java Math API, Abstraction.
 */
public class SIPCalculatorService {

    /**
     * Calculate SIP maturity amount.
     * Formula: M × ((1 + r)^n - 1) / r × (1 + r)
     *
     * @param monthlyInvestment Monthly SIP amount (₹)
     * @param annualReturnRate  Expected annual return rate (%)
     * @param years             Investment period in years
     * @return Total maturity amount
     */
    public double calculateSIPMaturity(double monthlyInvestment, double annualReturnRate, int years) {
        double monthlyRate = annualReturnRate / 12.0 / 100.0;
        int totalMonths = years * 12;

        if (monthlyRate == 0) {
            return monthlyInvestment * totalMonths;
        }

        double maturity = monthlyInvestment *
            ((Math.pow(1 + monthlyRate, totalMonths) - 1) / monthlyRate) *
            (1 + monthlyRate);

        return Math.round(maturity * 100.0) / 100.0;
    }

    /**
     * Calculate total amount invested.
     */
    public double calculateTotalInvested(double monthlyInvestment, int years) {
        return monthlyInvestment * years * 12;
    }

    /**
     * Calculate estimated returns (profit).
     */
    public double calculateEstimatedReturns(double monthlyInvestment, double annualReturnRate, int years) {
        double maturity = calculateSIPMaturity(monthlyInvestment, annualReturnRate, years);
        double totalInvested = calculateTotalInvested(monthlyInvestment, years);
        return maturity - totalInvested;
    }

    /**
     * Calculate lump sum investment maturity.
     * Formula: P × (1 + r)^n
     */
    public double calculateLumpSumMaturity(double principal, double annualReturnRate, int years) {
        double rate = annualReturnRate / 100.0;
        return Math.round(principal * Math.pow(1 + rate, years) * 100.0) / 100.0;
    }

    /**
     * Calculate year-wise SIP growth for chart data.
     * @return Array of cumulative values for each year
     */
    public double[] getYearWiseGrowth(double monthlyInvestment, double annualReturnRate, int years) {
        double[] yearlyValues = new double[years];
        double monthlyRate = annualReturnRate / 12.0 / 100.0;

        for (int year = 1; year <= years; year++) {
            int months = year * 12;
            if (monthlyRate == 0) {
                yearlyValues[year - 1] = monthlyInvestment * months;
            } else {
                yearlyValues[year - 1] = monthlyInvestment *
                    ((Math.pow(1 + monthlyRate, months) - 1) / monthlyRate) *
                    (1 + monthlyRate);
            }
        }
        return yearlyValues;
    }

    /**
     * Calculate required monthly SIP for a target amount.
     */
    public double calculateRequiredSIP(double targetAmount, double annualReturnRate, int years) {
        double monthlyRate = annualReturnRate / 12.0 / 100.0;
        int totalMonths = years * 12;

        if (monthlyRate == 0) {
            return targetAmount / totalMonths;
        }

        double factor = ((Math.pow(1 + monthlyRate, totalMonths) - 1) / monthlyRate) * (1 + monthlyRate);
        return Math.round((targetAmount / factor) * 100.0) / 100.0;
    }
}
