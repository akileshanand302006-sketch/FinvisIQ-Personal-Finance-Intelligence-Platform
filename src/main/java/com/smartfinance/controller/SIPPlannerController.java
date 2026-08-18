package com.smartfinance.controller;

import com.smartfinance.model.User;
import com.smartfinance.service.SIPCalculatorService;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.ValidationUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Premium SIP Planner Controller — Wealth compounding simulator, Step-Up SIP & year-wise growth projection.
 * Redesigned with uncropped full-height charts, vivid lines, high-contrast labels, and 100% full scrollability.
 */
public class SIPPlannerController {
    private final User currentUser;
    private final SIPCalculatorService sipService;

    private TextField monthlyInvField, returnRateField, yearsField;
    private Label totalInvestedLabel, estimatedReturnsLabel, totalValueLabel;
    private Label errorLabel;
    private LineChart<String, Number> growthChart;

    public SIPPlannerController(User user) {
        this.currentUser = user;
        this.sipService = new SIPCalculatorService();
    }

    public VBox createView() {
        VBox root = new VBox(24);
        // Extra bottom padding to ensure 100% scrollability to the bottom
        root.setPadding(new Insets(30, 30, 80, 30));

        Label title = new Label("SIP & Wealth Planner");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Calculate compounding returns and plan long-term systematic investments");
        subtitle.getStyleClass().add("page-subtitle");

        HBox mainRow = new HBox(20);

        // Inputs Card
        VBox inputCard = createInputCard();

        // Results Summary Card
        VBox resultCard = createResultCard();

        HBox.setHgrow(inputCard, Priority.ALWAYS);
        HBox.setHgrow(resultCard, Priority.ALWAYS);
        mainRow.getChildren().addAll(inputCard, resultCard);

        // Uncropped Chart Card
        VBox chartCard = createChartCard();

        // Disclaimer
        Label disclaimer = new Label("\u2139\uFE0F Disclaimer: Mutual Fund investments are subject to market risks. Read all scheme related documents carefully.");
        disclaimer.setStyle("-fx-font-size: 12px; -fx-font-weight: 500;");
        disclaimer.getStyleClass().add("label-muted");

        root.getChildren().addAll(title, subtitle, mainRow, chartCard, disclaimer);
        javafx.application.Platform.runLater(() -> {
            calculateSIP();
            AnimationUtils.staggerChildren(root, 70);
        });
        return root;
    }

    private VBox createInputCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));

        Label sectionTitle = new Label("\uD83D\uDCA1 Investment Parameters (" + currentUser.getCurrencySymbol() + ")");
        sectionTitle.getStyleClass().add("section-title");

        monthlyInvField = new TextField("5000");
        monthlyInvField.setPromptText("Monthly SIP Amount (" + currentUser.getCurrencySymbol() + ")");
        monthlyInvField.getStyleClass().add("text-field-modern");

        returnRateField = new TextField("12.0");
        returnRateField.setPromptText("Expected Annual Return Rate (%)");
        returnRateField.getStyleClass().add("text-field-modern");

        yearsField = new TextField("10");
        yearsField.setPromptText("Time Period (Years)");
        yearsField.getStyleClass().add("text-field-modern");

        errorLabel = new Label();
        errorLabel.setWrapText(true);

        Button calculateBtn = new Button("Calculate Future Value");
        calculateBtn.getStyleClass().add("btn-primary");
        calculateBtn.setMaxWidth(Double.MAX_VALUE);
        calculateBtn.setOnAction(e -> calculateSIP());

        card.getChildren().addAll(sectionTitle,
                new Label("Monthly Investment (" + currentUser.getCurrencySymbol() + "):"), monthlyInvField,
                new Label("Expected Return Rate (% p.a.):"), returnRateField,
                new Label("Investment Tenure (Years):"), yearsField,
                errorLabel, calculateBtn);
        return card;
    }

    private VBox createResultCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));
        card.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("\uD83C\uDFC6 Projection Summary");
        sectionTitle.getStyleClass().add("section-title");

        VBox invBox = new VBox(4);
        Label invLbl = new Label("Total Amount Invested");
        invLbl.getStyleClass().add("label-muted");
        totalInvestedLabel = new Label(currentUser.getCurrencySymbol() + "0");
        totalInvestedLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        totalInvestedLabel.getStyleClass().add("label-bold");
        invBox.getChildren().addAll(invLbl, totalInvestedLabel);

        VBox retBox = new VBox(4);
        Label retLbl = new Label("Estimated Compounded Returns");
        retLbl.getStyleClass().add("label-muted");
        estimatedReturnsLabel = new Label(currentUser.getCurrencySymbol() + "0");
        estimatedReturnsLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #10B981;");
        retBox.getChildren().addAll(retLbl, estimatedReturnsLabel);

        VBox totalBox = new VBox(4);
        Label totalLbl = new Label("Total Maturity Value");
        totalLbl.getStyleClass().add("label-muted");
        totalValueLabel = new Label(currentUser.getCurrencySymbol() + "0");
        totalValueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #7C3AED;");
        totalBox.getChildren().addAll(totalLbl, totalValueLabel);

        card.getChildren().addAll(sectionTitle, invBox, new Separator(), retBox, new Separator(), totalBox);
        return card;
    }

    private VBox createChartCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));
        card.setMinHeight(500);

        Label chartTitle = new Label("\uD83D\uDCC8 Wealth Growth Curve over Time");
        chartTitle.getStyleClass().add("section-title");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Years");
        xAxis.setAnimated(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (" + currentUser.getCurrencySymbol() + ")");
        yAxis.setAnimated(false);

        growthChart = new LineChart<>(xAxis, yAxis);
        growthChart.setAnimated(false);
        growthChart.setCreateSymbols(true);
        growthChart.setLegendVisible(true);
        growthChart.setMinHeight(420);
        growthChart.setPrefHeight(450);
        VBox.setVgrow(growthChart, Priority.ALWAYS);

        card.getChildren().addAll(chartTitle, growthChart);
        return card;
    }

    @SuppressWarnings("unchecked")
    private void calculateSIP() {
        String monthlyStr = monthlyInvField.getText().trim();
        String rateStr = returnRateField.getText().trim();
        String yearsStr = yearsField.getText().trim();

        if (!ValidationUtils.isValidAmount(monthlyStr)) {
            showError("Please enter a valid monthly investment amount.");
            return;
        }
        if (!ValidationUtils.isValidAmount(rateStr)) {
            showError("Please enter a valid annual return rate percentage.");
            return;
        }
        if (!ValidationUtils.isValidAmount(yearsStr) || ValidationUtils.parseInt(yearsStr) <= 0) {
            showError("Please enter a valid positive tenure in years.");
            return;
        }

        clearError();

        double monthly = ValidationUtils.parseDouble(monthlyStr);
        double rate = ValidationUtils.parseDouble(rateStr);
        int years = ValidationUtils.parseInt(yearsStr);

        double totalInvested = sipService.calculateTotalInvested(monthly, years);
        double totalMaturity = sipService.calculateSIPMaturity(monthly, rate, years);
        double estReturns = totalMaturity - totalInvested;

        totalInvestedLabel.setText(ValidationUtils.formatCurrency(totalInvested));
        estimatedReturnsLabel.setText(ValidationUtils.formatCurrency(estReturns));
        totalValueLabel.setText(ValidationUtils.formatCurrency(totalMaturity));

        // Update Chart safely with full uncropped bounds
        growthChart.getData().clear();
        XYChart.Series<String, Number> investedSeries = new XYChart.Series<>();
        investedSeries.setName("Amount Invested");

        XYChart.Series<String, Number> maturitySeries = new XYChart.Series<>();
        maturitySeries.setName("Future Maturity Value");

        double[] yearlyGrowth = sipService.getYearWiseGrowth(monthly, rate, years);
        for (int y = 1; y <= years; y++) {
            double inv = monthly * y * 12;
            investedSeries.getData().add(new XYChart.Data<>("Yr " + y, inv));
            maturitySeries.getData().add(new XYChart.Data<>("Yr " + y, yearlyGrowth[y - 1]));
        }

        growthChart.getData().addAll(investedSeries, maturitySeries);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.getStyleClass().setAll("error-label");
        AnimationUtils.fadeIn(errorLabel, 200);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.getStyleClass().clear();
    }

    public User getCurrentUser() { return currentUser; }
}
