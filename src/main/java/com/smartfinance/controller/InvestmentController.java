package com.smartfinance.controller;

import com.smartfinance.dao.InvestmentDAO;
import com.smartfinance.model.Investment;
import com.smartfinance.model.User;
import com.smartfinance.service.SIPCalculatorService;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.ValidationUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;


/**
 * Investment Controller - SIP Calculator and investment management.
 * Demonstrates: Java Math API, Charts, TableView, Event Handling.
 */
@SuppressWarnings("unchecked")
public class InvestmentController {
    private final User currentUser;
    private final InvestmentDAO investmentDAO;
    private final SIPCalculatorService sipService;
    private TableView<Investment> table;

    private static final String[] INVESTMENT_TYPES = {
        "SIP", "FIXED_DEPOSIT", "STOCKS", "MUTUAL_FUND", "PPF", "GOLD"
    };

    public InvestmentController(User user) {
        this.currentUser = user;
        this.investmentDAO = new InvestmentDAO();
        this.sipService = new SIPCalculatorService();
    }

    public VBox createView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30));

        Label title = new Label("Investments");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("SIP Calculator & Portfolio Management");
        subtitle.getStyleClass().add("page-subtitle");

        // SIP Calculator
        VBox sipCard = createSIPCalculator();

        // Add Investment + Portfolio
        HBox bottomRow = createBottomRow();

        root.getChildren().addAll(title, subtitle, sipCard, bottomRow);
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 70));
        return root;
    }

    private VBox createSIPCalculator() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));

        Label sipTitle = new Label("\uD83D\uDCB0 SIP Calculator");
        sipTitle.getStyleClass().add("section-title");

        // Inputs
        TextField monthlyField = new TextField();
        monthlyField.setPromptText("Monthly Investment (\u20B9)");
        monthlyField.getStyleClass().add("text-field-modern");
        monthlyField.setPrefWidth(220);
        monthlyField.setId("sipMonthly");

        TextField rateField = new TextField();
        rateField.setPromptText("Expected Return (% p.a.)");
        rateField.getStyleClass().add("text-field-modern");
        rateField.setPrefWidth(220);
        rateField.setId("sipRate");

        TextField yearsField = new TextField();
        yearsField.setPromptText("Period (Years)");
        yearsField.getStyleClass().add("text-field-modern");
        yearsField.setPrefWidth(220);
        yearsField.setId("sipYears");

        HBox inputRow = new HBox(16, monthlyField, rateField, yearsField);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        // Results area
        VBox resultsBox = new VBox(16);
        resultsBox.setVisible(false);

        // Calculate button
        Button calcBtn = new Button("Calculate Returns");
        calcBtn.getStyleClass().add("btn-primary");
        calcBtn.setId("calcSIPBtn");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");

        calcBtn.setOnAction(e -> {
            if (!ValidationUtils.isValidAmount(monthlyField.getText())) {
                errorLabel.setText("Enter a valid monthly investment amount.");
                AnimationUtils.shake(monthlyField);
                return;
            }
            if (!ValidationUtils.isValidRate(rateField.getText())) {
                errorLabel.setText("Enter a valid return rate (0-100).");
                AnimationUtils.shake(rateField);
                return;
            }
            if (!ValidationUtils.isValidYears(yearsField.getText())) {
                errorLabel.setText("Enter a valid period (1-50 years).");
                AnimationUtils.shake(yearsField);
                return;
            }

            errorLabel.setText("");
            double monthly = ValidationUtils.parseDouble(monthlyField.getText());
            double rate = ValidationUtils.parseDouble(rateField.getText());
            int years = ValidationUtils.parseInt(yearsField.getText());

            double maturity = sipService.calculateSIPMaturity(monthly, rate, years);
            double invested = sipService.calculateTotalInvested(monthly, years);
            double returns = sipService.calculateEstimatedReturns(monthly, rate, years);
            double[] yearlyGrowth = sipService.getYearWiseGrowth(monthly, rate, years);

            resultsBox.getChildren().clear();
            resultsBox.setVisible(true);

            // Results cards
            HBox resultsCards = new HBox(16);
            VBox investedCard = makeSipResultCard("Total Invested", ValidationUtils.formatCurrency(invested), "#6C63FF");
            VBox returnsCard = makeSipResultCard("Est. Returns", ValidationUtils.formatCurrency(returns), "#2ECC71");
            VBox maturityCard = makeSipResultCard("Maturity Value", ValidationUtils.formatCurrency(maturity), "#F39C12");
            HBox.setHgrow(investedCard, Priority.ALWAYS);
            HBox.setHgrow(returnsCard, Priority.ALWAYS);
            HBox.setHgrow(maturityCard, Priority.ALWAYS);
            resultsCards.getChildren().addAll(investedCard, returnsCard, maturityCard);

            // Growth chart
            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Year");
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Value (\u20B9)");

            AreaChart<String, Number> growthChart = new AreaChart<>(xAxis, yAxis);
            growthChart.setTitle("SIP Growth Over " + years + " Years");
            growthChart.setAnimated(true);
            growthChart.setPrefHeight(280);

            XYChart.Series<String, Number> growthSeries = new XYChart.Series<>();
            growthSeries.setName("Portfolio Value");
            for (int i = 0; i < yearlyGrowth.length; i++) {
                growthSeries.getData().add(new XYChart.Data<>("Year " + (i + 1), yearlyGrowth[i]));
            }

            XYChart.Series<String, Number> investedSeries = new XYChart.Series<>();
            investedSeries.setName("Amount Invested");
            for (int i = 0; i < years; i++) {
                investedSeries.getData().add(new XYChart.Data<>("Year " + (i + 1), monthly * 12 * (i + 1)));
            }

            var growthData = growthChart.getData();
            growthData.addAll(investedSeries, growthSeries);

            resultsBox.getChildren().addAll(resultsCards, growthChart);
            AnimationUtils.slideInFromBottom(resultsBox, 400);
        });

        card.getChildren().addAll(sipTitle, inputRow, calcBtn, errorLabel, resultsBox);
        return card;
    }

    private VBox makeSipResultCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: rgba(108, 99, 255, 0.05); -fx-background-radius: 12; -fx-padding: 16;");

        Label lbl = new Label(label);
        lbl.getStyleClass().add("label-muted");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(lbl, val);
        return card;
    }

    private HBox createBottomRow() {
        // Add investment form
        VBox addCard = new VBox(16);
        addCard.getStyleClass().add("card");
        addCard.setPadding(new Insets(20));

        Label addTitle = new Label("\u2795 Add Investment");
        addTitle.getStyleClass().add("section-title");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(INVESTMENT_TYPES);
        typeCombo.setPromptText("Investment Type");
        typeCombo.getStyleClass().add("combo-modern");
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setId("invType");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (\u20B9)");
        amountField.getStyleClass().add("text-field-modern");
        amountField.setId("invAmount");

        TextField rateField = new TextField();
        rateField.setPromptText("Return Rate (% p.a.)");
        rateField.getStyleClass().add("text-field-modern");
        rateField.setId("invRate");

        Label invMsg = new Label();

        Button addBtn = new Button("Add Investment");
        addBtn.getStyleClass().add("btn-success");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setId("addInvBtn");

        addBtn.setOnAction(e -> {
            if (typeCombo.getValue() == null) { invMsg.setText("Select type"); invMsg.getStyleClass().setAll("error-label"); return; }
            if (!ValidationUtils.isValidAmount(amountField.getText())) { invMsg.setText("Invalid amount"); invMsg.getStyleClass().setAll("error-label"); return; }
            if (!ValidationUtils.isValidRate(rateField.getText())) { invMsg.setText("Invalid rate"); invMsg.getStyleClass().setAll("error-label"); return; }

            Investment inv = new Investment(currentUser.getUserId(), typeCombo.getValue(),
                ValidationUtils.parseDouble(amountField.getText()),
                ValidationUtils.parseDouble(rateField.getText()), LocalDate.now());
            int id = investmentDAO.insert(inv);
            if (id > 0) {
                invMsg.setText("Investment added!"); invMsg.getStyleClass().setAll("success-label");
                typeCombo.setValue(null); amountField.clear(); rateField.clear();
                refreshTable();
            }
        });

        addCard.getChildren().addAll(addTitle, typeCombo, amountField, rateField, addBtn, invMsg);

        // Portfolio table
        VBox portfolioCard = new VBox(12);
        portfolioCard.getStyleClass().add("card");
        portfolioCard.setPadding(new Insets(20));

        Label portTitle = new Label("\uD83D\uDCBC My Portfolio");
        portTitle.getStyleClass().add("section-title");

        // Total invested
        double totalInv = investmentDAO.getTotalInvestment(currentUser.getUserId());
        Label totalLabel = new Label("Total Invested: " + ValidationUtils.formatCurrency(totalInv));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #6C63FF;");

        table = createPortfolioTable();
        refreshTable();

        portfolioCard.getChildren().addAll(portTitle, totalLabel, table);

        HBox row = new HBox(16, addCard, portfolioCard);
        HBox.setHgrow(addCard, Priority.SOMETIMES);
        HBox.setHgrow(portfolioCard, Priority.ALWAYS);
        addCard.setMinWidth(280);
        return row;
    }

    private TableView<Investment> createPortfolioTable() {
        TableView<Investment> tv = new TableView<>();
        tv.setPrefHeight(250);
        tv.setId("investmentTable");

        TableColumn<Investment, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDisplayType()));

        TableColumn<Investment, String> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(d -> new SimpleStringProperty(ValidationUtils.formatCurrency(d.getValue().getAmount())));

        TableColumn<Investment, String> rateCol = new TableColumn<>("Return %");
        rateCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.1f%%", d.getValue().getReturnRate())));

        TableColumn<Investment, String> dateCol = new TableColumn<>("Start Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStartDate().toString()));

        TableColumn<Investment, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button delBtn = new Button("\u2716");
            {
                delBtn.getStyleClass().add("btn-danger");
                delBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
                delBtn.setOnAction(e -> {
                    Investment inv = getTableView().getItems().get(getIndex());
                    investmentDAO.delete(inv.getInvestmentId());
                    refreshTable();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : delBtn);
            }
        });

        tv.getColumns().addAll(typeCol, amtCol, rateCol, dateCol, actionCol);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tv.setPlaceholder(new Label("No investments yet"));
        return tv;
    }

    private void refreshTable() {
        if (table != null) {
            ArrayList<Investment> investments = investmentDAO.findByUserId(currentUser.getUserId());
            table.setItems(FXCollections.observableArrayList(investments));
        }
    }
}
