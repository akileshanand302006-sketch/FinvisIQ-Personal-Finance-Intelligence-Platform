package com.smartfinance.controller;

import com.smartfinance.dao.GoalDAO;
import com.smartfinance.model.Goal;
import com.smartfinance.model.User;
import com.smartfinance.service.SIPCalculatorService;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.ValidationUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Goal Controller - Set and track savings goals.
 * Demonstrates: ProgressBar, Event Handling, ArrayList.
 */
public class GoalController {
    private final User currentUser;
    private final GoalDAO goalDAO;
    @SuppressWarnings("unused")
    private final SIPCalculatorService sipService;
    private VBox goalsContainer;

    public GoalController(User user) {
        this.currentUser = user;
        this.goalDAO = new GoalDAO();
        this.sipService = new SIPCalculatorService();
    }

    public VBox createView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30));

        Label title = new Label("Savings Goals");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Set targets and track your progress");
        subtitle.getStyleClass().add("page-subtitle");

        // Add goal form
        VBox addCard = createAddGoalForm();

        // Goals list
        goalsContainer = new VBox(16);
        refreshGoals();

        root.getChildren().addAll(title, subtitle, addCard, goalsContainer);
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 70));
        return root;
    }

    private VBox createAddGoalForm() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));

        Label formTitle = new Label("\uD83C\uDFAF Set New Goal");
        formTitle.getStyleClass().add("section-title");

        TextField nameField = new TextField();
        nameField.setPromptText("Goal Name (e.g., Emergency Fund, Vacation)");
        nameField.getStyleClass().add("text-field-modern");
        nameField.setId("goalName");

        TextField targetField = new TextField();
        targetField.setPromptText("Target Amount (\u20B9)");
        targetField.getStyleClass().add("text-field-modern");
        targetField.setPrefWidth(220);
        targetField.setId("goalTarget");

        TextField savedField = new TextField();
        savedField.setPromptText("Already Saved (\u20B9)");
        savedField.getStyleClass().add("text-field-modern");
        savedField.setPrefWidth(220);
        savedField.setText("0");
        savedField.setId("goalSaved");

        DatePicker deadlinePicker = new DatePicker(LocalDate.now().plusMonths(12));
        deadlinePicker.getStyleClass().add("date-picker");
        deadlinePicker.setPrefWidth(220);
        deadlinePicker.setId("goalDeadline");

        HBox row1 = new HBox(16, targetField, savedField, deadlinePicker);
        row1.setAlignment(Pos.CENTER_LEFT);

        Label msgLabel = new Label();

        Button addBtn = new Button("Create Goal");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setId("createGoalBtn");

        addBtn.setOnAction(e -> {
            if (nameField.getText() == null || nameField.getText().isBlank()) {
                msgLabel.setText("Enter a goal name."); msgLabel.getStyleClass().setAll("error-label");
                return;
            }
            if (!ValidationUtils.isValidAmount(targetField.getText())) {
                msgLabel.setText("Enter a valid target amount."); msgLabel.getStyleClass().setAll("error-label");
                return;
            }
            if (deadlinePicker.getValue() == null || deadlinePicker.getValue().isBefore(LocalDate.now())) {
                msgLabel.setText("Select a future deadline."); msgLabel.getStyleClass().setAll("error-label");
                return;
            }

            double saved = ValidationUtils.isValidAmount(savedField.getText()) ?
                ValidationUtils.parseDouble(savedField.getText()) : 0;

            Goal goal = new Goal(currentUser.getUserId(), nameField.getText().trim(),
                ValidationUtils.parseDouble(targetField.getText()), saved,
                deadlinePicker.getValue(), "ACTIVE");

            int id = goalDAO.insert(goal);
            if (id > 0) {
                msgLabel.setText("Goal created!"); msgLabel.getStyleClass().setAll("success-label");
                nameField.clear(); targetField.clear(); savedField.setText("0");
                deadlinePicker.setValue(LocalDate.now().plusMonths(12));
                refreshGoals();
            }
        });

        card.getChildren().addAll(formTitle, nameField, row1, addBtn, msgLabel);
        return card;
    }

    private void refreshGoals() {
        goalsContainer.getChildren().clear();
        ArrayList<Goal> goals = goalDAO.findByUserId(currentUser.getUserId());

        if (goals.isEmpty()) {
            Label empty = new Label("No goals set yet. Create your first savings goal above!");
            empty.getStyleClass().add("label-muted");
            empty.setPadding(new Insets(20));
            goalsContainer.getChildren().add(empty);
            return;
        }

        for (Goal goal : goals) {
            goalsContainer.getChildren().add(createGoalCard(goal));
        }

        AnimationUtils.staggerChildren(goalsContainer, 80);
    }

    private VBox createGoalCard(Goal goal) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(goal.isCompleted() ? "\u2705" : "\uD83C\uDFAF");
        icon.setStyle("-fx-font-size: 24px;");

        VBox info = new VBox(2);
        Label name = new Label(goal.getGoalName());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        name.getStyleClass().add("label-bold");

        Label deadline = new Label("Deadline: " + goal.getDeadline().toString());
        deadline.getStyleClass().add("label-muted");
        info.getChildren().addAll(name, deadline);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status badge
        Label statusBadge = new Label(goal.getStatus());
        String badgeColor = switch (goal.getStatus()) {
            case "COMPLETED" -> "#2ECC71";
            case "FAILED" -> "#E74C3C";
            default -> "#6C63FF";
        };
        statusBadge.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; " +
            "-fx-background-color: " + badgeColor + "; -fx-background-radius: 8; -fx-padding: 4 12;");

        header.getChildren().addAll(icon, info, spacer, statusBadge);

        // Progress section
        double progress = goal.getProgress() / 100.0;
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(14);

        HBox progressLabels = new HBox();
        Label savedLabel = new Label("Saved: " + ValidationUtils.formatCurrency(goal.getSavedAmount()));
        savedLabel.getStyleClass().add("income-text");
        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);
        Label targetLabel = new Label("Target: " + ValidationUtils.formatCurrency(goal.getTargetAmount()));
        targetLabel.getStyleClass().add("label-bold");
        Region sp3 = new Region();
        HBox.setHgrow(sp3, Priority.ALWAYS);
        Label pctLabel = new Label(String.format("%.1f%%", goal.getProgress()));
        pctLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #6C63FF;");
        progressLabels.getChildren().addAll(savedLabel, sp2, targetLabel, sp3, pctLabel);

        // Monthly requirement
        double remaining = goal.getTargetAmount() - goal.getSavedAmount();
        long monthsLeft = java.time.temporal.ChronoUnit.MONTHS.between(LocalDate.now(), goal.getDeadline());
        String monthlyReq = monthsLeft > 0 ?
            ValidationUtils.formatCurrency(remaining / monthsLeft) + "/month needed" :
            "Deadline passed";
        Label reqLabel = new Label("\uD83D\uDCCA " + monthlyReq);
        reqLabel.getStyleClass().add("label-muted");

        // Action buttons
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);

        if (goal.isActive()) {
            TextField addSavedField = new TextField();
            addSavedField.setPromptText("Add savings (\u20B9)");
            addSavedField.getStyleClass().add("text-field-modern");
            addSavedField.setPrefWidth(160);

            Button addSavedBtn = new Button("Add Savings");
            addSavedBtn.getStyleClass().add("btn-success");
            addSavedBtn.setOnAction(e -> {
                if (ValidationUtils.isValidAmount(addSavedField.getText())) {
                    double newSaved = goal.getSavedAmount() + ValidationUtils.parseDouble(addSavedField.getText());
                    goalDAO.updateSavedAmount(goal.getGoalId(), newSaved);
                    if (newSaved >= goal.getTargetAmount()) {
                        goal.setStatus("COMPLETED");
                        goal.setSavedAmount(newSaved);
                        goalDAO.update(goal);
                    }
                    refreshGoals();
                }
            });

            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("btn-danger");
            deleteBtn.setOnAction(e -> {
                goalDAO.delete(goal.getGoalId());
                refreshGoals();
            });

            actions.getChildren().addAll(addSavedField, addSavedBtn, deleteBtn);
        } else {
            Button deleteBtn = new Button("Remove");
            deleteBtn.getStyleClass().add("btn-danger");
            deleteBtn.setOnAction(e -> {
                goalDAO.delete(goal.getGoalId());
                refreshGoals();
            });
            actions.getChildren().add(deleteBtn);
        }

        card.getChildren().addAll(header, progressBar, progressLabels, reqLabel, actions);

        AnimationUtils.addHoverScale(card, 1.01);
        return card;
    }
}
