package com.smartfinance.controller;

import com.smartfinance.dao.GoalDAO;
import com.smartfinance.dao.SubscriptionDAO;
import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.model.Goal;
import com.smartfinance.model.Subscription;
import com.smartfinance.model.Transaction;
import com.smartfinance.model.User;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.ValidationUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

/**
 * Premium Calendar Controller — Financial calendar displaying transactions, bill due dates, and goal deadlines.
 * Redesigned with high-contrast dates, bold day headers, colorful event badges, and 100% full scrollability.
 */
public class CalendarController {
    private final User currentUser;
    private final TransactionDAO transactionDAO;
    private final SubscriptionDAO subscriptionDAO;
    private final GoalDAO goalDAO;

    private YearMonth currentYearMonth;
    private Label monthYearLabel;
    private GridPane calendarGrid;

    public CalendarController(User user) {
        this.currentUser = user;
        this.transactionDAO = new TransactionDAO();
        this.subscriptionDAO = new SubscriptionDAO();
        this.goalDAO = new GoalDAO();
        this.currentYearMonth = YearMonth.now();
    }

    public VBox createView() {
        VBox root = new VBox(24);
        // Extra bottom padding to ensure 100% scrollability to the bottom
        root.setPadding(new Insets(30, 30, 80, 30));

        Label title = new Label("Financial Calendar");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Visual schedule of recurring payments, income, expenses, and goal deadlines");
        subtitle.getStyleClass().add("page-subtitle");

        // Header controls (Prev Month, Next Month, Month Title)
        HBox header = createHeader();

        // Calendar grid
        calendarGrid = new GridPane();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        calendarGrid.setMaxWidth(Double.MAX_VALUE);

        populateCalendar();

        root.getChildren().addAll(title, subtitle, header, calendarGrid);
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 60));
        return root;
    }

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Button prevBtn = new Button("\u25C0 Previous");
        prevBtn.getStyleClass().add("btn-secondary");
        prevBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        Button nextBtn = new Button("Next \u25B6");
        nextBtn.getStyleClass().add("btn-secondary");
        nextBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        monthYearLabel = new Label(currentYearMonth.getMonth().toString() + " " + currentYearMonth.getYear());
        monthYearLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        monthYearLabel.getStyleClass().add("label-bold");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button todayBtn = new Button("Today");
        todayBtn.getStyleClass().add("btn-primary");
        todayBtn.setOnAction(e -> {
            currentYearMonth = YearMonth.now();
            updateCalendar();
        });

        header.getChildren().addAll(prevBtn, monthYearLabel, nextBtn, sp, todayBtn);
        return header;
    }

    private void updateCalendar() {
        monthYearLabel.setText(currentYearMonth.getMonth().toString() + " " + currentYearMonth.getYear());
        populateCalendar();
    }

    private void populateCalendar() {
        calendarGrid.getChildren().clear();

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < days.length; i++) {
            Label dayLbl = new Label(days[i]);
            dayLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-alignment: center; -fx-padding: 8; -fx-background-color: rgba(124, 58, 237, 0.15); -fx-background-radius: 8;");
            dayLbl.getStyleClass().add("label-bold");
            dayLbl.setMaxWidth(Double.MAX_VALUE);
            calendarGrid.add(dayLbl, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Mon, 7 = Sun
        int daysInMonth = currentYearMonth.lengthOfMonth();

        ArrayList<Transaction> txns = transactionDAO.findByUserId(currentUser.getUserId());
        ArrayList<Subscription> subs = subscriptionDAO.findByUserId(currentUser.getUserId());
        ArrayList<Goal> goals = goalDAO.findByUserId(currentUser.getUserId());

        int row = 1;
        int col = dayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);

            VBox cell = new VBox(6);
            cell.setPadding(new Insets(10));
            cell.setMinHeight(110);
            cell.getStyleClass().add("calendar-cell");

            if (date.equals(LocalDate.now())) {
                cell.getStyleClass().add("calendar-today");
            }

            // High contrast Date Number
            Label dateNum = new Label(String.valueOf(day));
            dateNum.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
            dateNum.getStyleClass().add("label-bold");
            cell.getChildren().add(dateNum);

            // Add Event Badges for this date
            for (Transaction t : txns) {
                if (t.getDate().equals(date)) {
                    Label badge = new Label((t.isIncome() ? "+ " : "- ") + ValidationUtils.formatCurrencyShort(t.getAmount()));
                    String color = t.isIncome() ? "#10B981" : "#EF4444";
                    badge.setStyle("-fx-background-color: " + color + "25; -fx-text-fill: " + color + "; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 6; -fx-border-color: " + color + "40; -fx-border-radius: 6;");
                    cell.getChildren().add(badge);
                }
            }

            for (Subscription s : subs) {
                if (s.getNextBillingDate() != null && s.getNextBillingDate().equals(date)) {
                    Label subBadge = new Label("\u26A1 " + s.getServiceName());
                    subBadge.setStyle("-fx-background-color: rgba(6, 182, 212, 0.20); -fx-text-fill: #06B6D4; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 6; -fx-border-color: rgba(6, 182, 212, 0.35); -fx-border-radius: 6;");
                    cell.getChildren().add(subBadge);
                }
            }

            for (Goal g : goals) {
                if (g.getDeadline() != null && g.getDeadline().equals(date)) {
                    Label gBadge = new Label("\uD83C\uDFC6 " + g.getGoalName());
                    gBadge.setStyle("-fx-background-color: rgba(245, 158, 11, 0.20); -fx-text-fill: #F59E0B; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 6; -fx-border-color: rgba(245, 158, 11, 0.35); -fx-border-radius: 6;");
                    cell.getChildren().add(gBadge);
                }
            }

            calendarGrid.add(cell, col, row);
            GridPane.setHgrow(cell, Priority.ALWAYS);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }
}
