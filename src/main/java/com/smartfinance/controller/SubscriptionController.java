package com.smartfinance.controller;

import com.smartfinance.dao.SubscriptionDAO;
import com.smartfinance.model.Subscription;
import com.smartfinance.model.User;
import com.smartfinance.service.SubscriptionService;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.ValidationUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Subscription Controller — Manage recurring software, media & utility subscriptions.
 */
public class SubscriptionController {
    private final User currentUser;
    private final SubscriptionDAO subscriptionDAO;
    private final SubscriptionService subscriptionService;
    private VBox subListContainer;
    private Label totalMonthlyLabel, totalYearlyLabel;

    public SubscriptionController(User user) {
        this.currentUser = user;
        this.subscriptionDAO = new SubscriptionDAO();
        this.subscriptionService = new SubscriptionService();
    }

    public VBox createView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30, 30, 80, 30));

        Label title = new Label("Subscription Tracker");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Track and control your recurring subscriptions and digital services");
        subtitle.getStyleClass().add("page-subtitle");

        // Overview summary card
        HBox overviewCard = createOverviewCard();

        // Add subscription form
        VBox formCard = createAddForm();

        // Subscription list
        subListContainer = new VBox(14);
        refreshList();

        root.getChildren().addAll(title, subtitle, overviewCard, formCard, subListContainer);
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 70));
        return root;
    }

    private HBox createOverviewCard() {
        HBox card = new HBox(30);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));
        card.setAlignment(Pos.CENTER_LEFT);

        double monthly = subscriptionService.getTotalMonthlyCost(currentUser.getUserId());
        double yearly = subscriptionService.getTotalYearlyCost(currentUser.getUserId());

        VBox mBox = new VBox(4);
        Label mLbl = new Label("Total Monthly Commitments");
        mLbl.getStyleClass().add("label-muted");
        totalMonthlyLabel = new Label(ValidationUtils.formatCurrency(monthly));
        totalMonthlyLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #7C3AED;");
        mBox.getChildren().addAll(mLbl, totalMonthlyLabel);

        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);

        VBox yBox = new VBox(4);
        Label yLbl = new Label("Estimated Annual Spending");
        yLbl.getStyleClass().add("label-muted");
        totalYearlyLabel = new Label(ValidationUtils.formatCurrency(yearly));
        totalYearlyLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #06B6D4;");
        yBox.getChildren().addAll(yLbl, totalYearlyLabel);

        card.getChildren().addAll(mBox, sep, yBox);
        return card;
    }

    private VBox createAddForm() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));

        Label formTitle = new Label("\u2795 Track New Subscription");
        formTitle.getStyleClass().add("section-title");

        TextField nameField = new TextField();
        nameField.setPromptText("Service Name (e.g. Netflix, Spotify, AWS)");
        nameField.getStyleClass().add("text-field-modern");
        nameField.setPrefWidth(220);

        TextField amountField = new TextField();
        amountField.setPromptText("Cost (\u20B9)");
        amountField.getStyleClass().add("text-field-modern");
        amountField.setPrefWidth(140);

        ComboBox<String> cycleCombo = new ComboBox<>();
        cycleCombo.getItems().addAll("MONTHLY", "YEARLY");
        cycleCombo.setValue("MONTHLY");
        cycleCombo.getStyleClass().add("combo-modern");

        DatePicker nextPicker = new DatePicker(LocalDate.now().plusMonths(1));
        nextPicker.getStyleClass().add("date-picker");

        Label msgLabel = new Label();

        Button addBtn = new Button("Add Subscription");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isBlank()) {
                msgLabel.setText("Please enter service name.");
                msgLabel.getStyleClass().setAll("error-label");
                return;
            }
            if (!ValidationUtils.isValidAmount(amountField.getText())) {
                msgLabel.setText("Please enter a valid positive cost.");
                msgLabel.getStyleClass().setAll("error-label");
                return;
            }

            double amount = Double.parseDouble(amountField.getText().trim());
            Subscription sub = new Subscription(currentUser.getUserId(), name, amount, cycleCombo.getValue(), nextPicker.getValue(), "Entertainment", "ACTIVE");

            if (subscriptionDAO.insert(sub) > 0) {
                msgLabel.setText("Subscription added!");
                msgLabel.getStyleClass().setAll("success-label");
                nameField.clear();
                amountField.clear();
                refreshList();
                refreshSummary();
            }
        });

        HBox row = new HBox(14, nameField, amountField, cycleCombo, nextPicker, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(formTitle, row, msgLabel);
        return card;
    }

    private void refreshList() {
        subListContainer.getChildren().clear();
        ArrayList<Subscription> subs = subscriptionDAO.findByUserId(currentUser.getUserId());

        if (subs.isEmpty()) {
            Label empty = new Label("No active subscriptions tracked.");
            empty.getStyleClass().add("label-muted");
            empty.setPadding(new Insets(20));
            subListContainer.getChildren().add(empty);
            return;
        }

        for (Subscription sub : subs) {
            HBox item = new HBox(16);
            item.getStyleClass().add("card");
            item.setPadding(new Insets(16, 20, 16, 20));
            item.setAlignment(Pos.CENTER_LEFT);

            Label icon = new Label("\uD83D\uDCF1");
            icon.setStyle("-fx-font-size: 24px;");

            VBox info = new VBox(2);
            Label name = new Label(sub.getServiceName());
            name.getStyleClass().add("label-bold");

            Label next = new Label("Next Renewal: " + sub.getNextBillingDate());
            next.getStyleClass().add("label-muted");
            info.getChildren().addAll(name, next);

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label cost = new Label(ValidationUtils.formatCurrency(sub.getAmount()) + "/" + sub.getBillingCycle().toLowerCase());
            cost.setStyle("-fx-font-weight: bold; -fx-text-fill: #E2E8F0;");

            Button delBtn = new Button("\u2716");
            delBtn.getStyleClass().add("btn-danger");
            delBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
            delBtn.setOnAction(e -> {
                subscriptionDAO.delete(sub.getSubscriptionId());
                refreshList();
                refreshSummary();
            });

            item.getChildren().addAll(icon, info, sp, cost, delBtn);
            subListContainer.getChildren().add(item);
        }
    }

    private void refreshSummary() {
        double m = subscriptionService.getTotalMonthlyCost(currentUser.getUserId());
        double y = subscriptionService.getTotalYearlyCost(currentUser.getUserId());
        if (totalMonthlyLabel != null) totalMonthlyLabel.setText(ValidationUtils.formatCurrency(m));
        if (totalYearlyLabel != null) totalYearlyLabel.setText(ValidationUtils.formatCurrency(y));
    }
}
