package com.smartfinance.controller;

import com.smartfinance.dao.BudgetDAO;
import com.smartfinance.model.Budget;
import com.smartfinance.model.User;
import com.smartfinance.service.BudgetService;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.ValidationUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

/**
 * Budget Controller — Category budgets, spend limits, visual progress bars & alerts.
 * Fully theme-aware with high-contrast text for both Light Mode and Dark Mode.
 */
public class BudgetController {
    private final User currentUser;
    private final BudgetDAO budgetDAO;
    private final BudgetService budgetService;
    private VBox budgetListContainer;

    private static final String[] CATEGORIES = {
        "Food & Dining", "Transportation", "Shopping", "Entertainment",
        "Bills & Utilities", "Healthcare", "Education", "Housing/Rent", "Travel", "Other"
    };

    public BudgetController(User user) {
        this.currentUser = user;
        this.budgetDAO = new BudgetDAO();
        this.budgetService = new BudgetService();
    }

    public VBox createView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30, 30, 80, 30));

        Label title = new Label("Budget Manager");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Set spending limits per category and track your progress");
        subtitle.getStyleClass().add("page-subtitle");

        // Add budget form card
        VBox formCard = createAddBudgetForm();

        // Budget list container
        budgetListContainer = new VBox(16);
        refreshBudgets();

        root.getChildren().addAll(title, subtitle, formCard, budgetListContainer);
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 70));
        return root;
    }

    private VBox createAddBudgetForm() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));

        Label formTitle = new Label("\uD83D\uDCCA Create / Update Category Budget");
        formTitle.getStyleClass().add("section-title");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(CATEGORIES);
        categoryCombo.setPromptText("Select Category");
        categoryCombo.getStyleClass().add("combo-modern");
        categoryCombo.setPrefWidth(220);

        TextField amountField = new TextField();
        amountField.setPromptText("Monthly Budget Limit (" + currentUser.getCurrencySymbol() + ")");
        amountField.getStyleClass().add("text-field-modern");
        amountField.setPrefWidth(220);

        TextField thresholdField = new TextField("80");
        thresholdField.setPromptText("Warning % (e.g. 80)");
        thresholdField.getStyleClass().add("text-field-modern");
        thresholdField.setPrefWidth(160);

        Label msgLabel = new Label();

        Button submitBtn = new Button("Set Budget");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setOnAction(e -> {
            if (categoryCombo.getValue() == null) {
                msgLabel.setText("Please select a category.");
                msgLabel.getStyleClass().setAll("error-label");
                return;
            }
            if (!ValidationUtils.isValidAmount(amountField.getText())) {
                msgLabel.setText("Enter a valid positive budget amount.");
                msgLabel.getStyleClass().setAll("error-label");
                return;
            }

            double amount = Double.parseDouble(amountField.getText().trim());
            double threshold = ValidationUtils.parseDouble(thresholdField.getText());
            if (threshold <= 0 || threshold > 100) threshold = 80.0;

            Budget budget = new Budget(currentUser.getUserId(), categoryCombo.getValue(), amount, "MONTHLY", threshold);
            int id = budgetDAO.insert(budget);
            if (id > 0) {
                msgLabel.setText("Budget set successfully!");
                msgLabel.getStyleClass().setAll("success-label");
                amountField.clear();
                categoryCombo.setValue(null);
                refreshBudgets();
                AnimationUtils.fadeIn(msgLabel, 200);
            } else {
                msgLabel.setText("Failed to set budget.");
                msgLabel.getStyleClass().setAll("error-label");
            }
        });

        HBox row = new HBox(16, categoryCombo, amountField, thresholdField, submitBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(formTitle, row, msgLabel);
        return card;
    }

    private void refreshBudgets() {
        budgetListContainer.getChildren().clear();
        ArrayList<Budget> budgets = budgetDAO.findByUserId(currentUser.getUserId());

        if (budgets.isEmpty()) {
            VBox emptyState = new VBox(12);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(40, 0, 40, 0));

            Label emptyIcon = new Label("\uD83D\uDCCA");
            emptyIcon.getStyleClass().add("empty-state-icon");

            Label emptyTitle = new Label("No Budgets Configured");
            emptyTitle.getStyleClass().add("empty-state-title");

            Label emptyDesc = new Label("Set up your first category budget above to control your expenses.");
            emptyDesc.getStyleClass().add("empty-state-desc");

            emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyDesc);
            budgetListContainer.getChildren().add(emptyState);
            return;
        }

        for (Budget budget : budgets) {
            budgetListContainer.getChildren().add(createBudgetCard(budget));
        }

        AnimationUtils.staggerChildren(budgetListContainer, 60);
    }

    private VBox createBudgetCard(Budget budget) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        double spent = budgetService.getCategorySpent(currentUser.getUserId(), budget.getCategory());
        double limit = budget.getBudgetAmount();
        double remaining = limit - spent;
        double pct = (spent / limit) * 100.0;
        String status = budgetService.getBudgetStatus(budget);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label catTitle = new Label(budget.getCategory());
        catTitle.getStyleClass().add("section-title");
        catTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        // Status badge
        Label badge = new Label(status);
        String badgeColor = switch (status) {
            case "EXCEEDED" -> "#EF4444";
            case "CRITICAL" -> "#F59E0B";
            case "WARNING" -> "#EAB308";
            default -> "#10B981";
        };
        badge.setStyle("-fx-background-color: " + badgeColor + "33; -fx-text-fill: " + badgeColor +
                "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 20; -fx-border-color: " + badgeColor + "55; -fx-border-radius: 20;");

        Button deleteBtn = new Button("\u2716");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
        deleteBtn.setOnAction(e -> {
            budgetDAO.delete(budget.getBudgetId());
            refreshBudgets();
        });

        header.getChildren().addAll(catTitle, sp, badge, deleteBtn);

        // Progress bar
        ProgressBar bar = new ProgressBar(Math.min(1.0, spent / limit));
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(12);
        bar.getStyleClass().add("glass-progress");

        // Stats row
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        Label spentLbl = new Label("Spent: " + ValidationUtils.formatCurrency(spent));
        spentLbl.getStyleClass().add("expense-text");

        Label limitLbl = new Label("Limit: " + ValidationUtils.formatCurrency(limit));
        limitLbl.getStyleClass().add("label-muted");

        Label remLbl = new Label("Remaining: " + ValidationUtils.formatCurrency(remaining));
        remLbl.getStyleClass().add(remaining >= 0 ? "income-text" : "expense-text");

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        Label pctLbl = new Label(String.format("%.1f%% used", pct));
        pctLbl.getStyleClass().add("label-bold");

        statsRow.getChildren().addAll(spentLbl, limitLbl, remLbl, sp2, pctLbl);

        card.getChildren().addAll(header, bar, statsRow);
        AnimationUtils.addHoverScale(card, 1.01);
        return card;
    }
}
