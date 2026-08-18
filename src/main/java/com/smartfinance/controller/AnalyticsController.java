package com.smartfinance.controller;

import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.model.User;
import com.smartfinance.util.AnimationUtils;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.HashMap;

/**
 * Analytics Controller — Visual analytics dashboard hub.
 */
public class AnalyticsController {
    private final User currentUser;
    private final TransactionDAO transactionDAO;

    public AnalyticsController(User user) {
        this.currentUser = user;
        this.transactionDAO = new TransactionDAO();
    }

    public VBox createView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30, 30, 80, 30));

        Label title = new Label("Advanced Analytics");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Deep dive analysis into category distributions and financial metrics");
        subtitle.getStyleClass().add("page-subtitle");

        HBox chartsRow = new HBox(20);

        // Expense Pie
        VBox pieBox = createPieChartBox();

        // Expense Bar
        VBox barBox = createBarChartBox();

        HBox.setHgrow(pieBox, Priority.ALWAYS);
        HBox.setHgrow(barBox, Priority.ALWAYS);
        chartsRow.getChildren().addAll(pieBox, barBox);

        root.getChildren().addAll(title, subtitle, chartsRow);
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 70));
        return root;
    }

    private VBox createPieChartBox() {
        VBox box = new VBox(12);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(20));

        Label t = new Label("\uD83D\uDD35 Category Share");
        t.getStyleClass().add("section-title");

        HashMap<String, Double> map = transactionDAO.getCategoryTotals(currentUser.getUserId(), "EXPENSE");
        PieChart pie = new PieChart();
        if (!map.isEmpty()) {
            var data = FXCollections.observableArrayList(
                map.entrySet().stream().map(e -> new PieChart.Data(e.getKey(), e.getValue())).toList()
            );
            pie.setData(data);
        }
        pie.setAnimated(true);
        pie.setPrefSize(400, 320);

        box.getChildren().addAll(t, pie);
        return box;
    }

    private VBox createBarChartBox() {
        VBox box = new VBox(12);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(20));

        Label t = new Label("\uD83D\uDCCA Spending Comparison");
        t.getStyleClass().add("section-title");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        BarChart<String, Number> bar = new BarChart<>(xAxis, yAxis);
        bar.setAnimated(true);
        bar.setPrefSize(400, 320);
        bar.setLegendVisible(false);

        HashMap<String, Double> map = transactionDAO.getCategoryTotals(currentUser.getUserId(), "EXPENSE");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        map.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
        var barData = bar.getData();
        barData.add(series);

        box.getChildren().addAll(t, bar);
        return box;
    }
}
