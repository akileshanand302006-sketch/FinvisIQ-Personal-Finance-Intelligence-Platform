package com.smartfinance.controller;

import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.model.Transaction;
import com.smartfinance.model.User;
import com.smartfinance.service.*;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.ValidationUtils;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Dashboard Controller — Main overview screen & command center for FinvisIQ.
 * Demonstrates: JavaFX Charts (PieChart, LineChart), Layout, Collections.
 */
@SuppressWarnings("unchecked")
public class DashboardController {
    private final User currentUser;
    private final MainController mainController;
    private final TransactionDAO transactionDAO;
    private final FinancialHealthService healthService;
    private final AIAdvisorService aiService;
    private final PredictionService predictionService;
    private final DemoDataService demoDataService;

    public DashboardController(User user, MainController mainController) {
        this.currentUser = user;
        this.mainController = mainController;
        this.transactionDAO = new TransactionDAO();
        this.healthService = new FinancialHealthService();
        this.aiService = new AIAdvisorService();
        this.predictionService = new PredictionService();
        this.demoDataService = new DemoDataService();

        // Ensure user has initial demo data if new
        demoDataService.seedDemoData(user.getUserId());
    }

    public VBox createView() {
        VBox root = new VBox(24);
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(30, 30, 80, 30));

        // Welcome header
        HBox header = createHeader();

        // Stats cards
        HBox statsCards = createStatsCards();

        // Charts row
        HBox chartsRow = createChartsRow();

        // AI Suggestions + Predictions
        HBox bottomRow = createBottomRow();

        // Recent transactions
        VBox recentTxns = createRecentTransactions();

        root.getChildren().addAll(header, statsCards, chartsRow, bottomRow, recentTxns);

        // Stagger animation
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 70));

        return root;
    }

    private HBox createHeader() {
        VBox left = new VBox(4);
        Label welcome = new Label("Welcome back, " + currentUser.getName() + "!");
        welcome.getStyleClass().add("page-title");

        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.getStyleClass().add("page-subtitle");

        left.getChildren().addAll(welcome, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Prediction badge
        double predictedExpense = predictionService.predictNextMonthExpenses(currentUser.getUserId());
        VBox predBadge = new VBox(2);
        predBadge.setAlignment(Pos.CENTER_RIGHT);
        Label predLabel = new Label("Predicted Next Month");
        predLabel.getStyleClass().add("label-muted");
        Label predValue = new Label(ValidationUtils.formatCurrency(predictedExpense));
        predValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #7C3AED;");
        predBadge.getChildren().addAll(predLabel, predValue);

        HBox header = new HBox(left, spacer, predBadge);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox createStatsCards() {
        int userId = currentUser.getUserId();
        double totalIncome = transactionDAO.getTotalIncome(userId);
        double totalExpenses = transactionDAO.getTotalExpenses(userId);
        double balance = totalIncome - totalExpenses;
        int healthScore = healthService.calculateHealthScore(userId);

        VBox balanceCard = createStatCard("Total Balance", balance, "\uD83D\uDCB3", "stat-card-balance");
        VBox incomeCard = createStatCard("Total Income", totalIncome, "\uD83D\uDCC8", "stat-card-income");
        VBox expenseCard = createStatCard("Total Expenses", totalExpenses, "\uD83D\uDCC9", "stat-card-expense");
        VBox scoreCard = createScoreCard(healthScore);

        HBox cards = new HBox(16, balanceCard, incomeCard, expenseCard, scoreCard);
        HBox.setHgrow(balanceCard, Priority.ALWAYS);
        HBox.setHgrow(incomeCard, Priority.ALWAYS);
        HBox.setHgrow(expenseCard, Priority.ALWAYS);
        HBox.setHgrow(scoreCard, Priority.ALWAYS);

        return cards;
    }

    private VBox createStatCard(String title, double value, String icon, String styleClass) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.getStyleClass().add(styleClass);
        card.setPadding(new Insets(20));
        card.setMinWidth(200);

        HBox topRow = new HBox();
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("stat-icon");
        topRow.getChildren().addAll(titleLabel, spacer, iconLabel);

        Label valueLabel = new Label("\u20B90");
        valueLabel.getStyleClass().add("stat-value");

        card.getChildren().addAll(topRow, valueLabel);

        // Animate counter
        javafx.application.Platform.runLater(() ->
            AnimationUtils.animateCounter(valueLabel, value, currentUser.getCurrencySymbol(), null)
        );

        AnimationUtils.addHoverScale(card, 1.03);
        return card;
    }

    private VBox createScoreCard(int score) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("stat-card", "stat-card-score");
        card.setPadding(new Insets(20));
        card.setMinWidth(200);

        HBox topRow = new HBox();
        Label titleLabel = new Label("Financial Health");
        titleLabel.getStyleClass().add("stat-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label iconLabel = new Label("\uD83C\uDFC6");
        iconLabel.getStyleClass().add("stat-icon");
        topRow.getChildren().addAll(titleLabel, spacer, iconLabel);

        Label valueLabel = new Label("0");
        valueLabel.getStyleClass().add("stat-value");

        Label ratingLabel = new Label(healthService.getHealthRating(score));
        ratingLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(topRow, valueLabel, ratingLabel);

        javafx.application.Platform.runLater(() ->
            AnimationUtils.animateCounter(valueLabel, score, "", "/100")
        );

        AnimationUtils.addHoverScale(card, 1.03);
        return card;
    }

    private HBox createChartsRow() {
        // Pie Chart - Category-wise expenses
        VBox pieSection = new VBox(12);
        pieSection.getStyleClass().add("card");
        pieSection.setPadding(new Insets(20));

        Label pieTitle = new Label("Expense Categories");
        pieTitle.getStyleClass().add("section-title");

        HashMap<String, Double> categoryTotals = transactionDAO.getCategoryTotals(
            currentUser.getUserId(), "EXPENSE");

        PieChart pieChart;
        if (categoryTotals.isEmpty()) {
            pieChart = new PieChart();
            pieChart.setTitle("No expense data yet");
        } else {
            var pieData = FXCollections.observableArrayList(
                categoryTotals.entrySet().stream()
                    .map(e -> new PieChart.Data(e.getKey() + " (" + currentUser.getCurrencySymbol() +
                        String.format("%,.0f", e.getValue()) + ")", e.getValue()))
                    .toList()
            );
            pieChart = new PieChart(pieData);
        }
        pieChart.setAnimated(true);
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(false);
        pieChart.setPrefSize(400, 280);

        pieSection.getChildren().addAll(pieTitle, pieChart);

        // Line Chart - Monthly trend
        VBox lineSection = new VBox(12);
        lineSection.getStyleClass().add("card");
        lineSection.setPadding(new Insets(20));

        Label lineTitle = new Label("Monthly Trends");
        lineTitle.getStyleClass().add("section-title");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (" + currentUser.getCurrencySymbol() + ")");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setAnimated(true);
        lineChart.setPrefSize(400, 280);

        LinkedHashMap<String, Double> incomeHistory = predictionService.getMonthlyIncomeHistory(currentUser.getUserId());
        LinkedHashMap<String, Double> expenseHistory = predictionService.getMonthlyExpenseHistory(currentUser.getUserId());

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        incomeHistory.forEach((month, val) -> incomeSeries.getData().add(new XYChart.Data<>(month, val)));

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");
        expenseHistory.forEach((month, val) -> expenseSeries.getData().add(new XYChart.Data<>(month, val)));

        var chartData = lineChart.getData();
        chartData.addAll(incomeSeries, expenseSeries);
        lineChart.setLegendVisible(true);

        if (incomeHistory.isEmpty() && expenseHistory.isEmpty()) {
            lineChart.setTitle("Add transactions to see trends");
        }

        lineSection.getChildren().addAll(lineTitle, lineChart);

        HBox row = new HBox(16, pieSection, lineSection);
        HBox.setHgrow(pieSection, Priority.ALWAYS);
        HBox.setHgrow(lineSection, Priority.ALWAYS);
        return row;
    }

    private HBox createBottomRow() {
        // AI Suggestions
        VBox suggestionsCard = new VBox(12);
        suggestionsCard.getStyleClass().add("card");
        suggestionsCard.setPadding(new Insets(20));

        Label aiTitle = new Label("\uD83E\uDD16 FinvisIQ Intelligence Suggestions");
        aiTitle.getStyleClass().add("section-title");

        ArrayList<String> suggestions = aiService.generateSuggestions(currentUser.getUserId());

        VBox suggestionsList = new VBox(10);
        for (String suggestion : suggestions) {
            Label sugLabel = new Label(suggestion);
            sugLabel.setWrapText(true);
            sugLabel.setMaxWidth(400);
            sugLabel.getStyleClass().add("suggestion-card");
            sugLabel.setPadding(new Insets(12, 16, 12, 16));
            suggestionsList.getChildren().add(sugLabel);
        }

        suggestionsCard.getChildren().addAll(aiTitle, suggestionsList);

        // Health Score Breakdown
        VBox healthCard = new VBox(12);
        healthCard.getStyleClass().add("card");
        healthCard.setPadding(new Insets(20));

        Label healthTitle = new Label("\uD83D\uDCCA Score Breakdown");
        healthTitle.getStyleClass().add("section-title");

        HashMap<String, Double> breakdown = healthService.getScoreBreakdown(currentUser.getUserId());
        VBox breakdownList = new VBox(10);
        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            Label name = new Label(entry.getKey());
            name.getStyleClass().add("label-muted");
            name.setMinWidth(140);

            ProgressBar bar = new ProgressBar(entry.getValue() / getMaxForCategory(entry.getKey()));
            bar.setPrefWidth(150);
            bar.setPrefHeight(10);

            Label score = new Label(String.format("%.0f", entry.getValue()));
            score.setStyle("-fx-font-weight: bold; -fx-text-fill: #7C3AED;");

            row.getChildren().addAll(name, bar, score);
            breakdownList.getChildren().add(row);
        }

        healthCard.getChildren().addAll(healthTitle, breakdownList);

        HBox row = new HBox(16, suggestionsCard, healthCard);
        HBox.setHgrow(suggestionsCard, Priority.ALWAYS);
        HBox.setHgrow(healthCard, Priority.ALWAYS);
        return row;
    }

    private double getMaxForCategory(String category) {
        return switch (category) {
            case "Savings Ratio" -> 30;
            case "Expense Stability" -> 20;
            case "Goal Progress" -> 20;
            case "Investment Score" -> 15;
            case "Balance Health" -> 15;
            default -> 20;
        };
    }

    private VBox createRecentTransactions() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        HBox titleRow = new HBox();
        Label title = new Label("Recent Transactions");
        title.getStyleClass().add("section-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Transaction");
        addBtn.getStyleClass().add("btn-secondary");
        addBtn.setOnAction(e -> mainController.navigateTo("Transactions"));

        Label viewAll = new Label("View All \u2197");
        viewAll.setStyle("-fx-text-fill: #7C3AED; -fx-cursor: hand; -fx-font-weight: bold;");
        viewAll.setOnMouseClicked(e -> mainController.navigateTo("Transactions"));

        titleRow.getChildren().addAll(title, sp, addBtn, viewAll);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        ArrayList<Transaction> transactions = transactionDAO.findByUserId(currentUser.getUserId());
        VBox txnList = new VBox(8);

        int limit = Math.min(5, transactions.size());
        if (limit == 0) {
            Label empty = new Label("No transactions yet. Add your first transaction!");
            empty.getStyleClass().add("label-muted");
            txnList.getChildren().add(empty);
        }

        for (int i = 0; i < limit; i++) {
            Transaction txn = transactions.get(i);
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 16, 10, 16));

            // Icon
            Label catIcon = new Label(getCategoryIcon(txn.getCategory()));
            catIcon.setStyle("-fx-font-size: 20px; -fx-min-width: 36; -fx-alignment: center;");

            // Info
            VBox info = new VBox(2);
            Label catLabel = new Label(txn.getCategory());
            catLabel.getStyleClass().add("label-bold");
            Label descLabel = new Label((txn.getDescription() != null ? txn.getDescription() : "") + " • " + txn.getPaymentMethod());
            descLabel.getStyleClass().add("label-muted");
            info.getChildren().addAll(catLabel, descLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Amount
            VBox amountBox = new VBox(2);
            amountBox.setAlignment(Pos.CENTER_RIGHT);
            Label amtLabel = new Label((txn.isIncome() ? "+" : "-") + ValidationUtils.formatCurrency(txn.getAmount()));
            amtLabel.getStyleClass().add(txn.isIncome() ? "income-text" : "expense-text");
            Label dateLabel = new Label(txn.getDate().format(DateTimeFormatter.ofPattern("MMM d")));
            dateLabel.getStyleClass().add("label-muted");
            amountBox.getChildren().addAll(amtLabel, dateLabel);

            row.getChildren().addAll(catIcon, info, spacer, amountBox);
            txnList.getChildren().add(row);
        }

        card.getChildren().addAll(titleRow, txnList);
        return card;
    }

    private String getCategoryIcon(String category) {
        return switch (category) {
            case "Food & Dining" -> "\uD83C\uDF74";
            case "Transportation" -> "\uD83D\uDE97";
            case "Shopping" -> "\uD83D\uDECD\uFE0F";
            case "Entertainment" -> "\uD83C\uDFAC";
            case "Bills & Utilities" -> "\uD83D\uDCA1";
            case "Healthcare" -> "\uD83C\uDFE5";
            case "Education" -> "\uD83D\uDCDA";
            case "Housing/Rent" -> "\uD83C\uDFE0";
            case "Travel" -> "\u2708\uFE0F";
            case "Salary" -> "\uD83D\uDCBC";
            case "Freelance" -> "\uD83D\uDCBB";
            case "Investment Returns" -> "\uD83D\uDCC8";
            case "Gift" -> "\uD83C\uDF81";
            case "Business" -> "\uD83C\uDFE2";
            default -> "\uD83D\uDCB0";
        };
    }
}
