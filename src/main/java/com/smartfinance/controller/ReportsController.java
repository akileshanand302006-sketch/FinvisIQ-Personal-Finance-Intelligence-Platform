package com.smartfinance.controller;

import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.model.User;
import com.smartfinance.service.PredictionService;
import com.smartfinance.service.ReportService;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.ValidationUtils;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;

/**
 * Reports Controller - Charts, financial statements & CSV export.
 * Demonstrates: PieChart, LineChart, BarChart, HashMap, File Export.
 */
@SuppressWarnings("unchecked")
public class ReportsController {
    private final User currentUser;
    private final TransactionDAO transactionDAO;
    private final PredictionService predictionService;
    private final ReportService reportService;

    public ReportsController(User user) {
        this.currentUser = user;
        this.transactionDAO = new TransactionDAO();
        this.predictionService = new PredictionService();
        this.reportService = new ReportService();
    }

    public VBox createView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30, 30, 80, 30));

        HBox headerBox = new HBox(16);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Financial Reports");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Visual analytics & downloadable transaction statements");
        subtitle.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button exportPdfBtn = new Button("\uD83D\uDCC4 Export PDF Report");
        exportPdfBtn.getStyleClass().add("btn-primary");

        Button exportWordBtn = new Button("\uD83D\uDCDD Export Word Document");
        exportWordBtn.getStyleClass().add("btn-secondary");

        Label msgLbl = new Label();

        exportPdfBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Export Financial Report (PDF)");
            chooser.setInitialFileName("FinvisIQ_Report_" + currentUser.getName().replaceAll("\\s+", "_") + ".pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document", "*.pdf"));
            File file = chooser.showSaveDialog(root.getScene().getWindow());
            if (file != null) {
                try {
                    reportService.exportReportToPDF(currentUser.getUserId(), currentUser.getName(), currentUser.getCurrencySymbol(), file);
                    msgLbl.setText("PDF Statement exported: " + file.getName());
                    msgLbl.getStyleClass().setAll("success-label");
                    AnimationUtils.fadeIn(msgLbl, 200);
                } catch (Exception ex) {
                    msgLbl.setText("Failed to export PDF report.");
                    msgLbl.getStyleClass().setAll("error-label");
                }
            }
        });

        exportWordBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Export Financial Report (Word)");
            chooser.setInitialFileName("FinvisIQ_Report_" + currentUser.getName().replaceAll("\\s+", "_") + ".docx");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Document", "*.docx", "*.doc"));
            File file = chooser.showSaveDialog(root.getScene().getWindow());
            if (file != null) {
                try {
                    reportService.exportReportToWord(currentUser.getUserId(), currentUser.getName(), currentUser.getCurrencySymbol(), file);
                    msgLbl.setText("Word Document exported: " + file.getName());
                    msgLbl.getStyleClass().setAll("success-label");
                    AnimationUtils.fadeIn(msgLbl, 200);
                } catch (Exception ex) {
                    msgLbl.setText("Failed to export Word document.");
                    msgLbl.getStyleClass().setAll("error-label");
                }
            }
        });

        HBox btnBox = new HBox(12, exportPdfBtn, exportWordBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        VBox rightHeaderBox = new VBox(6, btnBox, msgLbl);
        rightHeaderBox.setAlignment(Pos.CENTER_RIGHT);

        headerBox.getChildren().addAll(titleBox, sp, rightHeaderBox);

        // Summary cards
        HBox summaryCards = createSummaryCards();

        // Charts row 1: Pie + Bar
        HBox row1 = createChartsRow1();

        // Charts row 2: Line chart (full width)
        VBox row2 = createLineChartSection();

        // Prediction section
        VBox predictionSection = createPredictionSection();

        root.getChildren().addAll(headerBox, msgLbl, summaryCards, row1, row2, predictionSection);
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 70));
        return root;
    }

    private HBox createSummaryCards() {
        int userId = currentUser.getUserId();
        double income = transactionDAO.getTotalIncome(userId);
        double expenses = transactionDAO.getTotalExpenses(userId);
        double balance = income - expenses;
        double savingsRate = income > 0 ? ((income - expenses) / income) * 100 : 0;

        VBox incomeCard = makeMiniCard("Total Income", ValidationUtils.formatCurrency(income), "#2ECC71");
        VBox expenseCard = makeMiniCard("Total Expenses", ValidationUtils.formatCurrency(expenses), "#E74C3C");
        VBox balanceCard = makeMiniCard("Net Balance", ValidationUtils.formatCurrency(balance), "#6C63FF");
        VBox savingsCard = makeMiniCard("Savings Rate", String.format("%.1f%%", savingsRate), "#F39C12");

        HBox cards = new HBox(16, incomeCard, expenseCard, balanceCard, savingsCard);
        HBox.setHgrow(incomeCard, Priority.ALWAYS);
        HBox.setHgrow(expenseCard, Priority.ALWAYS);
        HBox.setHgrow(balanceCard, Priority.ALWAYS);
        HBox.setHgrow(savingsCard, Priority.ALWAYS);
        return cards;
    }

    private VBox makeMiniCard(String label, String value, String color) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("label-muted");

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(lbl, val);
        AnimationUtils.addHoverScale(card, 1.03);
        return card;
    }

    private HBox createChartsRow1() {
        VBox pieCard = new VBox(12);
        pieCard.getStyleClass().add("card");
        pieCard.setPadding(new Insets(20));

        Label pieTitle = new Label("\uD83D\uDD35 Expense Categories");
        pieTitle.getStyleClass().add("section-title");

        HashMap<String, Double> categories = transactionDAO.getCategoryTotals(currentUser.getUserId(), "EXPENSE");
        PieChart pieChart;
        if (categories.isEmpty()) {
            pieChart = new PieChart();
            pieChart.setTitle("No data available");
        } else {
            var data = FXCollections.observableArrayList(
                categories.entrySet().stream()
                    .map(e -> new PieChart.Data(e.getKey(), e.getValue()))
                    .toList()
            );
            pieChart = new PieChart(data);
        }
        pieChart.setAnimated(true);
        pieChart.setPrefSize(400, 320);
        pieChart.setLabelsVisible(true);

        pieCard.getChildren().addAll(pieTitle, pieChart);

        VBox barCard = new VBox(12);
        barCard.getStyleClass().add("card");
        barCard.setPadding(new Insets(20));

        Label barTitle = new Label("\uD83D\uDCCA Category Breakdown");
        barTitle.getStyleClass().add("section-title");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (\u20B9)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setAnimated(true);
        barChart.setPrefSize(400, 320);
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");
        categories.forEach((cat, val) ->
            series.getData().add(new XYChart.Data<>(cat.length() > 10 ? cat.substring(0, 10) + ".." : cat, val))
        );
        var barData = barChart.getData();
        barData.add(series);

        barCard.getChildren().addAll(barTitle, barChart);

        HBox row = new HBox(16, pieCard, barCard);
        HBox.setHgrow(pieCard, Priority.ALWAYS);
        HBox.setHgrow(barCard, Priority.ALWAYS);
        return row;
    }

    private VBox createLineChartSection() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        Label title = new Label("\uD83D\uDCC8 Monthly Income vs Expenses");
        title.getStyleClass().add("section-title");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (\u20B9)");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setAnimated(true);
        lineChart.setPrefHeight(350);

        LinkedHashMap<String, Double> incomeData = predictionService.getMonthlyIncomeHistory(currentUser.getUserId());
        LinkedHashMap<String, Double> expenseData = predictionService.getMonthlyExpenseHistory(currentUser.getUserId());

        Set<String> allMonths = new TreeSet<>();
        allMonths.addAll(incomeData.keySet());
        allMonths.addAll(expenseData.keySet());

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        for (String month : allMonths) {
            incomeSeries.getData().add(new XYChart.Data<>(month, incomeData.getOrDefault(month, 0.0)));
        }

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");
        for (String month : allMonths) {
            expenseSeries.getData().add(new XYChart.Data<>(month, expenseData.getOrDefault(month, 0.0)));
        }

        var lineData = lineChart.getData();
        lineData.addAll(incomeSeries, expenseSeries);

        if (allMonths.isEmpty()) {
            lineChart.setTitle("Add transactions to see monthly trends");
        }

        card.getChildren().addAll(title, lineChart);
        return card;
    }

    private VBox createPredictionSection() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        Label title = new Label("\uD83D\uDD2E Monthly Predictions");
        title.getStyleClass().add("section-title");

        double predictedExpense = predictionService.predictNextMonthExpenses(currentUser.getUserId());
        double predictedIncome = predictionService.predictNextMonthIncome(currentUser.getUserId());
        String trend = predictionService.getExpenseTrend(currentUser.getUserId());

        HBox predictions = new HBox(30);
        predictions.setAlignment(Pos.CENTER_LEFT);

        VBox expPred = new VBox(4);
        Label expLabel = new Label("Predicted Expenses");
        expLabel.getStyleClass().add("label-muted");
        Label expValue = new Label(ValidationUtils.formatCurrency(predictedExpense));
        expValue.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #E74C3C;");
        expPred.getChildren().addAll(expLabel, expValue);

        VBox incPred = new VBox(4);
        Label incLabel = new Label("Predicted Income");
        incLabel.getStyleClass().add("label-muted");
        Label incValue = new Label(ValidationUtils.formatCurrency(predictedIncome));
        incValue.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2ECC71;");
        incPred.getChildren().addAll(incLabel, incValue);

        VBox trendBox = new VBox(4);
        Label trendLabel = new Label("Expense Trend");
        trendLabel.getStyleClass().add("label-muted");
        String trendIcon = switch (trend) {
            case "INCREASING" -> "\u2191 Increasing";
            case "DECREASING" -> "\u2193 Decreasing";
            default -> "\u2194 Stable";
        };
        String trendColor = switch (trend) {
            case "INCREASING" -> "#E74C3C";
            case "DECREASING" -> "#2ECC71";
            default -> "#F39C12";
        };
        Label trendValue = new Label(trendIcon);
        trendValue.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + trendColor + ";");
        trendBox.getChildren().addAll(trendLabel, trendValue);

        predictions.getChildren().addAll(expPred, incPred, trendBox);
        card.getChildren().addAll(title, predictions);
        return card;
    }
}
