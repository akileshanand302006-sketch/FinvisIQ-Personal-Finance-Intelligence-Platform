package com.smartfinance.backend.controller;

import com.smartfinance.backend.dto.ApiResponse;
import com.smartfinance.backend.security.CurrentUser;
import com.smartfinance.backend.security.UserPrincipal;
import com.smartfinance.dao.*;
import com.smartfinance.model.Budget;
import com.smartfinance.model.Goal;
import com.smartfinance.model.Transaction;
import com.smartfinance.service.FinancialHealthService;
import com.smartfinance.service.NetWorthService;
import com.smartfinance.service.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final GoalDAO goalDAO = new GoalDAO();
    private final InvestmentDAO investmentDAO = new InvestmentDAO();
    private final FinancialHealthService financialHealthService = new FinancialHealthService();
    private final PredictionService predictionService = new PredictionService();
    private final NetWorthService netWorthService = new NetWorthService();

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary(@CurrentUser UserPrincipal principal) {
        int userId = principal.getUserId();

        double totalIncome = transactionDAO.getTotalIncome(userId);
        double totalExpenses = transactionDAO.getTotalExpenses(userId);
        double netBalance = totalIncome - totalExpenses;
        double savingsRate = totalIncome > 0 ? Math.max(0, ((totalIncome - totalExpenses) / totalIncome) * 100) : 0.0;

        int healthScore = financialHealthService.calculateHealthScore(userId);
        HashMap<String, Double> scoreBreakdown = financialHealthService.getScoreBreakdown(userId);
        double predictedExpenses = predictionService.predictNextMonthExpenses(userId);
        double netWorth = netWorthService.getNetWorth(userId);

        // Recent 5 transactions
        List<Transaction> allTxns = transactionDAO.findByUserId(userId);
        List<Transaction> recentTxns = allTxns.stream().limit(5).collect(Collectors.toList());

        // Category breakdown for expenses
        Map<String, Double> categoryExpenses = new HashMap<>();
        for (Transaction t : allTxns) {
            if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                categoryExpenses.put(t.getCategory(), categoryExpenses.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
            }
        }

        // Active budgets and goals
        List<Budget> budgets = budgetDAO.findByUserId(userId);
        List<Goal> goals = goalDAO.findByUserId(userId);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("userName", principal.getName());
        dashboard.put("userId", userId);
        dashboard.put("totalIncome", totalIncome);
        dashboard.put("totalExpenses", totalExpenses);
        dashboard.put("netBalance", netBalance);
        dashboard.put("savingsRate", Math.round(savingsRate * 10.0) / 10.0);
        dashboard.put("healthScore", healthScore);
        dashboard.put("scoreBreakdown", scoreBreakdown);
        dashboard.put("predictedNextMonthExpenses", Math.round(predictedExpenses * 100.0) / 100.0);
        dashboard.put("netWorth", netWorth);
        dashboard.put("totalInvestments", investmentDAO.getTotalInvestment(userId));
        dashboard.put("recentTransactions", recentTxns);
        dashboard.put("categoryExpenses", categoryExpenses);
        dashboard.put("totalBudgets", budgets.size());
        dashboard.put("totalGoals", goals.size());

        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
