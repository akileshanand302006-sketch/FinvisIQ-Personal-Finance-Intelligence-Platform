package com.smartfinance.service;

import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.dao.GoalDAO;
import com.smartfinance.dao.InvestmentDAO;
import com.smartfinance.dao.UserDAO;
import com.smartfinance.model.Goal;
import com.smartfinance.model.Investment;
import com.smartfinance.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * AI Advisor Service - Rule-based intelligent financial suggestions.
 * Demonstrates: Rule-Based Logic, HashMap, ArrayList, String Handling.
 */
public class AIAdvisorService {
    private final TransactionDAO transactionDAO;
    private final GoalDAO goalDAO;
    private final InvestmentDAO investmentDAO;
    private final UserDAO userDAO;

    public AIAdvisorService() {
        this.transactionDAO = new TransactionDAO();
        this.goalDAO = new GoalDAO();
        this.investmentDAO = new InvestmentDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Generate personalized AI suggestions based on user's financial data.
     * Uses rule-based analysis on income, expenses, goals, and investments.
     */
    public ArrayList<String> generateSuggestions(int userId) {
        ArrayList<String> suggestions = new ArrayList<>();

        double totalIncome = transactionDAO.getTotalIncome(userId);
        double totalExpenses = transactionDAO.getTotalExpenses(userId);
        double balance = totalIncome - totalExpenses;
        HashMap<String, Double> categoryTotals = transactionDAO.getCategoryTotals(userId, "EXPENSE");
        ArrayList<Goal> goals = goalDAO.findByUserId(userId);
        ArrayList<Investment> investments = investmentDAO.findByUserId(userId);
        User user = userDAO.findById(userId);
        int age = user != null ? user.getAge() : 25; // default fallback

        // Rule 1: No data yet
        if (totalIncome == 0 && totalExpenses == 0) {
            suggestions.add("\uD83D\uDCCA Start by adding your income and expenses to get personalized financial advice!");
            suggestions.add("\uD83C\uDFAF Set a savings goal to stay motivated on your financial journey.");
            suggestions.add("\uD83D\uDCB0 Consider starting a SIP (Systematic Investment Plan) for long-term wealth creation.");
            return suggestions;
        }

        // Rule 2: Savings Ratio Analysis
        if (totalIncome > 0) {
            double savingsRatio = (balance / totalIncome) * 100;
            if (savingsRatio < 10) {
                suggestions.add("\u26A0\uFE0F Your savings rate is only " + String.format("%.1f%%", savingsRatio) +
                    ". Aim to save at least 20% of your income. Cut non-essential expenses!");
            } else if (savingsRatio < 20) {
                suggestions.add("\uD83D\uDCA1 Your savings rate is " + String.format("%.1f%%", savingsRatio) +
                    ". Try to increase it to 20%+ by reducing discretionary spending.");
            } else if (savingsRatio >= 30) {
                suggestions.add("\uD83C\uDF1F Excellent! You're saving " + String.format("%.1f%%", savingsRatio) +
                    " of your income. Consider investing the surplus for better returns!");
            } else {
                suggestions.add("\u2705 Good job! You're saving " + String.format("%.1f%%", savingsRatio) +
                    " of your income. Keep it up!");
            }
        }

        // Rule 3: Category-wise Analysis
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            double percentage = totalIncome > 0 ? (amount / totalIncome) * 100 : 0;

            if (category.equals("Food & Dining") && percentage > 30) {
                suggestions.add("\uD83C\uDF74 Food expenses are " + String.format("%.1f%%", percentage) +
                    " of income. Consider meal planning and cooking at home to save more.");
            }
            if (category.equals("Entertainment") && percentage > 15) {
                suggestions.add("\uD83C\uDFAC Entertainment spending is " + String.format("%.1f%%", percentage) +
                    " of income. Look for free or low-cost entertainment options.");
            }
            if (category.equals("Shopping") && percentage > 20) {
                suggestions.add("\uD83D\uDECD\uFE0F Shopping expenses are " + String.format("%.1f%%", percentage) +
                    " of income. Use the 48-hour rule before impulse purchases.");
            }
            if ((category.equals("Bills & Utilities")) && percentage > 15) {
                suggestions.add("\uD83D\uDCA1 Utility bills are " + String.format("%.1f%%", percentage) +
                    " of income. Consider energy-saving measures to reduce costs.");
            }
        }

        // Rule 4: Negative Balance Warning
        if (balance < 0) {
            suggestions.add("\uD83D\uDEA8 ALERT: Your expenses exceed your income by \u20B9" +
                String.format("%,.0f", Math.abs(balance)) + "! Immediately cut non-essential spending.");
        }

        // Rule 5: Investment Advice
        if (investments.isEmpty() && totalIncome > 0) {
            suggestions.add("\uD83D\uDCE6 You have no investments yet. Start a SIP with as little as \u20B9500/month for long-term wealth building.");
        } else if (investments.size() == 1) {
            suggestions.add("\uD83D\uDCC8 Diversify your portfolio! Having only one type of investment increases risk. Consider mutual funds, PPF, or gold.");
        }

        // Rule 6: Goal Progress Check
        for (Goal goal : goals) {
            if (goal.isActive()) {
                double progress = goal.getProgress();
                if (progress < 25) {
                    suggestions.add("\uD83C\uDFAF Goal '" + goal.getGoalName() + "' is at " +
                        String.format("%.0f%%", progress) + ". Increase your monthly savings to meet the deadline!");
                } else if (progress >= 75) {
                    suggestions.add("\uD83C\uDF89 Almost there! Goal '" + goal.getGoalName() + "' is at " +
                        String.format("%.0f%%", progress) + ". Keep going!");
                }
            }
        }

        // Rule 7: Emergency Fund
        if (balance < totalExpenses * 3 && totalExpenses > 0) {
            suggestions.add("\uD83D\uDEE1\uFE0F Build an emergency fund of at least 3-6 months' expenses (\u20B9" +
                String.format("%,.0f", totalExpenses * 3) + " - \u20B9" +
                String.format("%,.0f", totalExpenses * 6) + ").");
        }

        // Rule 8: 50-30-20 Budget Rule
        if (totalIncome > 0) {
            double needsLimit = totalIncome * 0.5;
            double needsActual = categoryTotals.getOrDefault("Housing/Rent", 0.0) +
                                 categoryTotals.getOrDefault("Bills & Utilities", 0.0) +
                                 categoryTotals.getOrDefault("Healthcare", 0.0) +
                                 categoryTotals.getOrDefault("Transportation", 0.0);
            if (needsActual > needsLimit) {
                suggestions.add("\uD83D\uDCCA Consider the 50-30-20 rule: 50% needs, 30% wants, 20% savings. Your needs spending exceeds 50% of income.");
            }
        }

        // Rule 9: Income Diversification
        HashMap<String, Double> incomeCategoryTotals = transactionDAO.getCategoryTotals(userId, "INCOME");
        if (incomeCategoryTotals.size() == 1 && totalIncome > 5000) {
            suggestions.add("\u26A0\uFE0F You currently rely on a single source of income. Consider building passive income streams or side hustles to diversify your financial risk.");
        }

        // Rule 10: Idle Cash / Cash Hoarding Warning
        double totalInvested = investmentDAO.getTotalInvestment(userId);
        if (balance > totalExpenses * 6 && totalInvested < balance * 0.2 && balance > 0) {
            suggestions.add("\uD83D\uDCA1 You have a large amount of idle cash! Since your 6-month emergency fund is secure, consider investing the excess amount to beat inflation.");
        }

        // Rule 11: Debt-to-Income Warning
        double debtPayments = categoryTotals.getOrDefault("Debt Repayment", 0.0) + 
                              categoryTotals.getOrDefault("Loan/EMI", 0.0);
        if (totalIncome > 0 && (debtPayments / totalIncome) > 0.3) {
            suggestions.add("\uD83D\uDEA8 High Debt Alert: Debt payments consume over 30% of your income. Focus on clearing high-interest loans first using the snowball or avalanche method.");
        }

        // Rule 12: Super Saver Recognition
        if (totalIncome > 0 && (balance / totalIncome) >= 0.4) {
            suggestions.add("\uD83C\uDFC6 Exceptional financial discipline! You're saving 40%+ of your income. You are on the fast track to achieving financial independence.");
        }

        // --- NEW COMPLEX AI STRATEGIES ---

        // Rule 13: Age-Based Investment Strategy
        if (age >= 18 && age <= 30) {
            suggestions.add("\uD83D\uDE80 Age Analysis (" + age + "): You have a long investment horizon. Suggestion: Allocate 70-80% of your portfolio to aggressive growth assets like Equity Mutual Funds and the Stock Market.");
        } else if (age >= 31 && age <= 40) {
            suggestions.add("\u2696\uFE0F Age Analysis (" + age + "): You are in your prime earning years. Suggestion: Balance your portfolio with 60% Stocks/Mutual Funds, and 40% stable assets like Bonds or starting a Home investment.");
        } else if (age >= 41) {
            suggestions.add("\uD83D\uDEE1\uFE0F Age Analysis (" + age + "): Capital preservation is becoming important. Suggestion: Shift towards lower-risk instruments like Fixed Deposits (FDs), Bonds, and Debt Funds. Limit direct stock market exposure.");
        }

        // Rule 14: Capital & High-Income Allocation (Real Estate / Cars / Forex)
        if (balance > 2000000) { // 20 Lakhs
            suggestions.add("\uD83C\uDFD8\uFE0F Portfolio Size Analysis: Your accumulated balance is over ₹20L. You have sufficient capital to explore Real Estate (Land/Home investments) for long-term appreciation.");
        } else if (balance > 500000) { // 5 Lakhs
            suggestions.add("\uD83D\uDE97 Portfolio Size Analysis: With ₹5L+ in liquidity, you are in a good position to safely finance a car purchase or explore moderate risk Forex/Trading markets with a small fractional amount.");
        }

        // Rule 15: Deep Gold/Silver Strategy (Physical vs Digital)
        double shoppingExpenses = categoryTotals.getOrDefault("Shopping", 0.0) + categoryTotals.getOrDefault("Personal Care", 0.0);
        if (shoppingExpenses > totalIncome * 0.15 && totalIncome > 0) {
            // User likes shopping, suggest physical jewelry
            suggestions.add("\uD83D\uDC8D Gold Strategy: Based on your shopping habits, investing in Physical Gold/Silver (like jewelry or coins) may satisfy your purchasing preferences while creating an appreciating asset.");
        } else {
            // Pure investor
            suggestions.add("\uD83D\uDCB0 Gold Strategy: For efficient returns, avoid physical gold. Invest in Digital Gold, Gold ETFs, or Sovereign Gold Bonds (SGBs) to eliminate making charges and storage costs.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("\u2705 Your finances look healthy! Keep maintaining good spending habits.");
        }

        return suggestions;
    }
}
