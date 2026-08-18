package com.smartfinance.controller;

import com.smartfinance.model.FinancialInsight;
import com.smartfinance.model.User;
import com.smartfinance.service.InsightEngine;
import com.smartfinance.util.AnimationUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

/**
 * Insights Controller — Display rule-based financial intelligence, risk warnings & smart suggestions.
 * Fully theme-aware with high-contrast text for both Light Mode and Dark Mode.
 */
public class InsightsController {
    private final User currentUser;
    private final InsightEngine insightEngine;
    private VBox insightsContainer;

    public InsightsController(User user) {
        this.currentUser = user;
        this.insightEngine = new InsightEngine();
    }

    public VBox createView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30, 30, 80, 30));

        Label title = new Label("AI & Financial Insights");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Rule-based intelligence recommendations tailored to your finances");
        subtitle.getStyleClass().add("page-subtitle");

        insightsContainer = new VBox(16);
        refreshInsights();

        root.getChildren().addAll(title, subtitle, insightsContainer);
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 70));
        return root;
    }

    private void refreshInsights() {
        insightsContainer.getChildren().clear();
        ArrayList<FinancialInsight> insights = insightEngine.generateInsights(currentUser.getUserId());

        if (insights.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60, 0, 60, 0));

            Label emptyIcon = new Label("\uD83D\uDCA1");
            emptyIcon.getStyleClass().add("empty-state-icon");

            Label emptyTitle = new Label("No Active Insights");
            emptyTitle.getStyleClass().add("empty-state-title");

            Label emptyDesc = new Label("Add more transactions or set up budgets to receive tailored financial intelligence.");
            emptyDesc.getStyleClass().add("empty-state-desc");

            empty.getChildren().addAll(emptyIcon, emptyTitle, emptyDesc);
            insightsContainer.getChildren().add(empty);
            return;
        }

        for (FinancialInsight insight : insights) {
            VBox card = new VBox(10);
            card.getStyleClass().addAll("glass-card", insight.getPriorityStyleClass());
            card.setPadding(new Insets(22));

            HBox header = new HBox(12);
            header.setAlignment(Pos.CENTER_LEFT);

            Label badge = new Label(insight.getPriority().name());
            String badgeColor = switch (insight.getPriority()) {
                case CRITICAL -> "#EF4444";
                case WARNING -> "#D97706";
                case SUCCESS -> "#059669";
                default -> "#4F46E5";
            };
            badge.setStyle("-fx-background-color: " + badgeColor + "25; -fx-text-fill: " + badgeColor +
                    "; -fx-font-size: 11.5px; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12; -fx-border-color: " + badgeColor + "40; -fx-border-radius: 12;");

            Label catLbl = new Label(insight.getCategory());
            catLbl.getStyleClass().add("label-muted");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            header.getChildren().addAll(badge, catLbl, sp);

            Label cardTitle = new Label(insight.getTitle());
            cardTitle.getStyleClass().add("insight-card-title");
            cardTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            Label cardMsg = new Label(insight.getMessage());
            cardMsg.getStyleClass().add("insight-card-msg");
            cardMsg.setWrapText(true);
            cardMsg.setStyle("-fx-font-size: 14px; -fx-line-spacing: 3px;");

            card.getChildren().addAll(header, cardTitle, cardMsg);
            AnimationUtils.addHoverScale(card, 1.01);
            insightsContainer.getChildren().add(card);
        }
    }
}
