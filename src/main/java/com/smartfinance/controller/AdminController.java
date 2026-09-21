package com.smartfinance.controller;

import com.smartfinance.dao.AuditLogDAO;
import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.dao.UserDAO;
import com.smartfinance.model.AuditLog;
import com.smartfinance.model.Transaction;
import com.smartfinance.model.User;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.ValidationUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

/**
 * Admin Controller — User management, system overview & audit log monitor.
 * Demonstrates: TableView, ArrayList, Admin security.
 */
public class AdminController {
    private final User currentUser;
    private final UserDAO userDAO;
    private final TransactionDAO transactionDAO;
    private final AuditLogDAO auditLogDAO;
    private TableView<User> userTable;
    private TableView<AuditLog> auditTable;

    public AdminController(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO();
        this.transactionDAO = new TransactionDAO();
        this.auditLogDAO = new AuditLogDAO();
    }

    public VBox createView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30));

        Label title = new Label("Admin & System Dashboard");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Manage system users, view system health and security audit logs");
        subtitle.getStyleClass().add("page-subtitle");

        // Stats
        HBox stats = createAdminStats();

        // User management
        VBox userSection = createUserSection();

        // Audit Logs
        VBox auditSection = createAuditSection();

        root.getChildren().addAll(title, subtitle, stats, userSection, auditSection);
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 70));
        return root;
    }

    private HBox createAdminStats() {
        int userCount = userDAO.getUserCount();
        ArrayList<Transaction> allTxns = transactionDAO.findAll();
        double totalVolume = allTxns.stream().filter(t -> t != null).mapToDouble(t -> t.getAmount()).sum();

        VBox usersCard = makeStatCard("Total Registered Users", String.valueOf(userCount), "#7C3AED");
        VBox txnCard = makeStatCard("System Transactions", String.valueOf(allTxns.size()), "#10B981");
        VBox volCard = makeStatCard("Total Tracked Volume", ValidationUtils.formatCurrencyShort(totalVolume), "#F59E0B");

        HBox stats = new HBox(16, usersCard, txnCard, volCard);
        HBox.setHgrow(usersCard, Priority.ALWAYS);
        HBox.setHgrow(txnCard, Priority.ALWAYS);
        HBox.setHgrow(volCard, Priority.ALWAYS);
        return stats;
    }

    private VBox makeStatCard(String label, String value, String color) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("label-muted");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(lbl, val);
        AnimationUtils.addHoverScale(card, 1.03);
        return card;
    }

    @SuppressWarnings("unchecked")
    private VBox createUserSection() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        Label sectionTitle = new Label("\uD83D\uDC65 User Accounts Management");
        sectionTitle.getStyleClass().add("section-title");

        userTable = new TableView<>();
        userTable.setId("adminUserTable");
        userTable.setPrefHeight(220);

        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getUserId())));
        idCol.setPrefWidth(50);

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole()));
        roleCol.setPrefWidth(80);

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        statusCol.setPrefWidth(90);

        TableColumn<User, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button delBtn = new Button("Delete");
            {
                delBtn.getStyleClass().add("btn-danger");
                delBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
                delBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u.getUserId() != currentUser.getUserId()) {
                        userDAO.delete(u.getUserId());
                        refreshUsers();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableView().getItems().get(getIndex());
                setGraphic(u.getUserId() == currentUser.getUserId() ? null : delBtn);
            }
        });

        userTable.getColumns().addAll(idCol, nameCol, emailCol, roleCol, statusCol, actionCol);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        refreshUsers();

        card.getChildren().addAll(sectionTitle, userTable);
        return card;
    }

    @SuppressWarnings("unchecked")
    private VBox createAuditSection() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        Label sectionTitle = new Label("\uD83D\uDD0D Security Audit Trail");
        sectionTitle.getStyleClass().add("section-title");

        auditTable = new TableView<>();
        auditTable.setPrefHeight(220);

        TableColumn<AuditLog, String> logIdCol = new TableColumn<>("Log ID");
        logIdCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getLogId())));
        logIdCol.setPrefWidth(60);

        TableColumn<AuditLog, String> userCol = new TableColumn<>("User ID");
        userCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getUserId())));
        userCol.setPrefWidth(70);

        TableColumn<AuditLog, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAction()));
        actionCol.setPrefWidth(120);

        TableColumn<AuditLog, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));

        TableColumn<AuditLog, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTimestamp().toString()));
        timeCol.setPrefWidth(160);

        auditTable.getColumns().addAll(logIdCol, userCol, actionCol, descCol, timeCol);
        auditTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        auditTable.setPlaceholder(new Label("No audit logs recorded yet."));

        refreshAuditLogs();

        card.getChildren().addAll(sectionTitle, auditTable);
        return card;
    }

    private void refreshUsers() {
        ArrayList<User> users = userDAO.findAll();
        userTable.setItems(FXCollections.observableArrayList(users));
    }

    private void refreshAuditLogs() {
        ArrayList<AuditLog> logs = auditLogDAO.findAll();
        auditTable.setItems(FXCollections.observableArrayList(logs));
    }
}
