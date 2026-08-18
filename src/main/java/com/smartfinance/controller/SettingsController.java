package com.smartfinance.controller;

import com.smartfinance.model.User;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.ThemeManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Settings Controller — User application preferences, theme controls & configuration.
 * Demonstrates: Preferences, UI state management.
 */
public class SettingsController {
    private final User currentUser;
    private final ThemeManager themeManager;

    public SettingsController(User user) {
        this.currentUser = user;
        this.themeManager = ThemeManager.getInstance();
    }

    public VBox createView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30));

        Label title = new Label("Application Settings");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Customize your experience and system preferences for " + currentUser.getName());
        subtitle.getStyleClass().add("page-subtitle");

        // Preferences Card
        VBox prefCard = new VBox(16);
        prefCard.getStyleClass().add("card");
        prefCard.setPadding(new Insets(24));

        Label prefTitle = new Label("\u2699\uFE0F General Preferences");
        prefTitle.getStyleClass().add("section-title");

        // Theme Toggle Row
        HBox themeRow = new HBox(16);
        themeRow.setAlignment(Pos.CENTER_LEFT);

        VBox themeText = new VBox(2);
        Label themeLbl = new Label("Interface Theme");
        themeLbl.getStyleClass().add("label-bold");
        Label themeDesc = new Label("Choose between Dark Glass or Light Glass theme");
        themeDesc.getStyleClass().add("label-muted");
        themeText.getChildren().addAll(themeLbl, themeDesc);

        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);

        Button toggleThemeBtn = new Button(themeManager.getThemeName() + " " + themeManager.getThemeIcon());
        toggleThemeBtn.getStyleClass().add("glass-button");
        toggleThemeBtn.setOnAction(e -> {
            themeManager.toggleTheme();
            toggleThemeBtn.setText(themeManager.getThemeName() + " " + themeManager.getThemeIcon());
        });
        themeRow.getChildren().addAll(themeText, sp1, toggleThemeBtn);

        // Budget Warning Threshold Row
        HBox budgetRow = new HBox(16);
        budgetRow.setAlignment(Pos.CENTER_LEFT);

        VBox budgetText = new VBox(2);
        Label budgetLbl = new Label("Budget Warning Threshold");
        budgetLbl.getStyleClass().add("label-bold");
        Label budgetDesc = new Label("Receive warning alerts when spending reaches threshold");
        budgetDesc.getStyleClass().add("label-muted");
        budgetText.getChildren().addAll(budgetLbl, budgetDesc);

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        ComboBox<String> thresholdCombo = new ComboBox<>();
        thresholdCombo.getItems().addAll("70% of budget", "80% of budget", "90% of budget");
        thresholdCombo.setValue("80% of budget");
        thresholdCombo.getStyleClass().add("combo-modern");
        budgetRow.getChildren().addAll(budgetText, sp2, thresholdCombo);

        // Auto-refresh Row
        HBox refreshRow = new HBox(16);
        refreshRow.setAlignment(Pos.CENTER_LEFT);

        VBox refreshText = new VBox(2);
        Label refreshLbl = new Label("Live Dashboard Refresh");
        refreshLbl.getStyleClass().add("label-bold");
        Label refreshDesc = new Label("Automatically recalculate charts when adding transactions");
        refreshDesc.getStyleClass().add("label-muted");
        refreshText.getChildren().addAll(refreshLbl, refreshDesc);

        Region sp3 = new Region();
        HBox.setHgrow(sp3, Priority.ALWAYS);

        CheckBox autoRefreshCheck = new CheckBox("Enabled");
        autoRefreshCheck.setSelected(true);
        autoRefreshCheck.getStyleClass().add("check-box");
        refreshRow.getChildren().addAll(refreshText, sp3, autoRefreshCheck);

        prefCard.getChildren().addAll(prefTitle, themeRow, new Separator(), budgetRow, new Separator(), refreshRow);

        // System Info Card
        VBox infoCard = new VBox(12);
        infoCard.getStyleClass().add("card");
        infoCard.setPadding(new Insets(20));

        Label infoTitle = new Label("\u2139\uFE0F System Information");
        infoTitle.getStyleClass().add("section-title");

        Label vInfo = new Label("Finora Engine Version: 2.0.0 (Build 2026.08)");
        vInfo.getStyleClass().add("label-muted");
        Label dbInfo = new Label("Database Backend: MySQL (smart_finance_db)");
        dbInfo.getStyleClass().add("label-muted");
        Label archInfo = new Label("Architecture: JavaFX Desktop Client (JDBC Direct)");
        archInfo.getStyleClass().add("label-muted");

        infoCard.getChildren().addAll(infoTitle, vInfo, dbInfo, archInfo);

        root.getChildren().addAll(title, subtitle, prefCard, infoCard);
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 70));
        return root;
    }

    public User getCurrentUser() { return currentUser; }
}
