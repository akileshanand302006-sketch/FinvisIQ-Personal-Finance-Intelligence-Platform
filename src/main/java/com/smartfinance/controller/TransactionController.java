package com.smartfinance.controller;

import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.model.Transaction;
import com.smartfinance.model.User;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.ValidationUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Transaction Controller - Add, view, search, and manage transactions.
 * Demonstrates: TableView, Event Handling, ArrayList, Input Validation.
 */
public class TransactionController {
    private final User currentUser;
    @SuppressWarnings("unused")
    private final MainController mainController;
    private final TransactionDAO transactionDAO;
    private TableView<Transaction> table;
    private ObservableList<Transaction> tableData;
    private Label totalIncomeLabel, totalExpenseLabel, balLabel;
    private TextField searchField;
    private ComboBox<String> filterCombo;
    private final ArrayList<Transaction> allTransactions = new ArrayList<>();

    private static final String[] EXPENSE_CATEGORIES = {
        "Food & Dining", "Transportation", "Shopping", "Entertainment",
        "Bills & Utilities", "Healthcare", "Education", "Housing/Rent", "Travel", "Other"
    };
    private static final String[] INCOME_CATEGORIES = {
        "Salary", "Freelance", "Investment Returns", "Gift", "Business", "Other"
    };

    public TransactionController(User user, MainController mainController) {
        this.currentUser = user;
        this.mainController = mainController;
        this.transactionDAO = new TransactionDAO();
    }

    public VBox createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));

        // Title
        Label title = new Label("Transactions");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Track and manage your income & expenses");
        subtitle.getStyleClass().add("page-subtitle");

        // Load transactions once
        allTransactions.clear();
        allTransactions.addAll(transactionDAO.findByUserId(currentUser.getUserId()));

        // Add transaction form
        VBox formCard = createAddForm();

        // Summary bar
        HBox summaryBar = createSummaryBar();

        // Search and filter bar
        HBox filterBar = createFilterBar();

        // Transactions table
        VBox tableCard = createTableCard();

        root.getChildren().addAll(title, subtitle, formCard, summaryBar, filterBar, tableCard);
        return root;
    }

    private VBox createAddForm() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));

        Label formTitle = new Label("\u2795 Add Transaction");
        formTitle.getStyleClass().add("section-title");

        // Type selection
        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton incomeRadio = new RadioButton("Income");
        incomeRadio.setToggleGroup(typeGroup);
        incomeRadio.setId("typeIncome");

        RadioButton expenseRadio = new RadioButton("Expense");
        expenseRadio.setToggleGroup(typeGroup);
        expenseRadio.setSelected(true);
        expenseRadio.setId("typeExpense");

        HBox typeBox = new HBox(20, incomeRadio, expenseRadio);
        typeBox.setAlignment(Pos.CENTER_LEFT);

        // Amount
        TextField amountField = new TextField();
        amountField.setPromptText("Amount (\u20B9)");
        amountField.getStyleClass().add("text-field-modern");
        amountField.setMaxWidth(250);
        amountField.setId("txnAmount");

        // Category
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(EXPENSE_CATEGORIES);
        categoryCombo.setPromptText("Select Category");
        categoryCombo.getStyleClass().add("combo-modern");
        categoryCombo.setMaxWidth(250);
        categoryCombo.setId("txnCategory");

        // Update categories when type changes
        typeGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            categoryCombo.getItems().clear();
            if (incomeRadio.isSelected()) {
                categoryCombo.getItems().addAll(INCOME_CATEGORIES);
            } else {
                categoryCombo.getItems().addAll(EXPENSE_CATEGORIES);
            }
        });

        // Date
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("date-picker");
        datePicker.setMaxWidth(250);
        datePicker.setId("txnDate");

        // Description
        TextField descField = new TextField();
        descField.setPromptText("Description (optional)");
        descField.getStyleClass().add("text-field-modern");
        descField.setId("txnDesc");

        // Message label
        Label msgLabel = new Label();
        msgLabel.setWrapText(true);

        // Submit button
        Button submitBtn = new Button("Add Transaction");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setId("addTxnBtn");

        submitBtn.setOnAction(e -> {
            if (!ValidationUtils.isValidAmount(amountField.getText())) {
                msgLabel.setText("Please enter a valid positive amount.");
                msgLabel.getStyleClass().setAll("error-label");
                AnimationUtils.shake(amountField);
                return;
            }
            if (categoryCombo.getValue() == null) {
                msgLabel.setText("Please select a category.");
                msgLabel.getStyleClass().setAll("error-label");
                return;
            }
            if (datePicker.getValue() == null) {
                msgLabel.setText("Please select a date.");
                msgLabel.getStyleClass().setAll("error-label");
                return;
            }

            String type = incomeRadio.isSelected() ? "INCOME" : "EXPENSE";
            double amount = Double.parseDouble(amountField.getText().trim());
            Transaction txn = new Transaction(
                currentUser.getUserId(), amount, type,
                categoryCombo.getValue(), datePicker.getValue(),
                descField.getText().trim()
            );

            int id = transactionDAO.insert(txn);
            if (id > 0) {
                msgLabel.setText("Transaction added successfully!");
                msgLabel.getStyleClass().setAll("success-label");
                amountField.clear();
                descField.clear();
                categoryCombo.setValue(null);
                datePicker.setValue(LocalDate.now());
                refreshTable();
                refreshSummary();
                AnimationUtils.fadeIn(msgLabel, 200);
            } else {
                msgLabel.setText("Failed to add transaction.");
                msgLabel.getStyleClass().setAll("error-label");
            }
        });

        // Layout
        HBox row1 = new HBox(16, amountField, categoryCombo, datePicker);
        row1.setAlignment(Pos.CENTER_LEFT);

        HBox row2 = new HBox(16, descField, submitBtn);
        row2.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(descField, Priority.ALWAYS);

        card.getChildren().addAll(formTitle, typeBox, row1, row2, msgLabel);
        return card;
    }

    private HBox createSummaryBar() {
        double income = allTransactions.stream().filter(t -> t != null && t.isIncome()).mapToDouble(Transaction::getAmount).sum();
        double expenses = allTransactions.stream().filter(t -> t != null && t.isExpense()).mapToDouble(Transaction::getAmount).sum();

        totalIncomeLabel = new Label("Income: " + ValidationUtils.formatCurrency(income));
        totalIncomeLabel.getStyleClass().add("income-text");
        totalIncomeLabel.setStyle(totalIncomeLabel.getStyle() + "-fx-font-size: 16px;");

        totalExpenseLabel = new Label("Expenses: " + ValidationUtils.formatCurrency(expenses));
        totalExpenseLabel.getStyleClass().add("expense-text");
        totalExpenseLabel.setStyle(totalExpenseLabel.getStyle() + "-fx-font-size: 16px;");

        balLabel = new Label("Balance: " + ValidationUtils.formatCurrency(income - expenses));
        balLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #6C63FF;");

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox bar = new HBox(20, totalIncomeLabel, spacer1, totalExpenseLabel, spacer2, balLabel);
        bar.getStyleClass().add("card");
        bar.setPadding(new Insets(16, 24, 16, 24));
        bar.setAlignment(Pos.CENTER);
        return bar;
    }

    private HBox createFilterBar() {
        searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search transactions...");
        searchField.getStyleClass().add("text-field-modern");
        searchField.setPrefWidth(300);
        searchField.setId("txnSearch");

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All", "Income", "Expense");
        filterCombo.setValue("All");
        filterCombo.getStyleClass().add("combo-modern");
        filterCombo.setId("txnFilter");

        searchField.textProperty().addListener((obs, o, n) -> applyFilter(searchField.getText(), filterCombo.getValue()));
        filterCombo.valueProperty().addListener((obs, o, n) -> applyFilter(searchField.getText(), filterCombo.getValue()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(12, searchField, filterCombo, spacer);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    @SuppressWarnings("unchecked")
    private VBox createTableCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        table = new TableView<>();
        table.setId("transactionsTable");

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate().toString()));
        dateCol.setPrefWidth(110);

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));
        typeCol.setPrefWidth(90);

        TableColumn<Transaction, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        catCol.setPrefWidth(150);

        TableColumn<Transaction, String> amtCol = new TableColumn<>("Amount (\u20B9)");
        amtCol.setCellValueFactory(d -> new SimpleStringProperty(
            ValidationUtils.formatCurrency(d.getValue().getAmount())
        ));
        amtCol.setPrefWidth(130);

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getDescription() != null ? d.getValue().getDescription() : ""
        ));
        descCol.setPrefWidth(200);

        TableColumn<Transaction, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(80);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("\u2716");
            {
                deleteBtn.getStyleClass().add("btn-danger");
                deleteBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
                deleteBtn.setOnAction(e -> {
                    Transaction txn = getTableView().getItems().get(getIndex());
                    transactionDAO.delete(txn.getTransactionId());
                    refreshTable();
                    refreshSummary();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        table.getColumns().addAll(dateCol, typeCol, catCol, amtCol, descCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(350);
        table.setPlaceholder(new Label("No transactions found"));

        table.setItems(FXCollections.observableArrayList(allTransactions));

        card.getChildren().add(table);
        return card;
    }

    private void refreshTable() {
        allTransactions.clear();
        allTransactions.addAll(transactionDAO.findByUserId(currentUser.getUserId()));
        if (searchField != null && filterCombo != null) {
            applyFilter(searchField.getText(), filterCombo.getValue());
        } else if (table != null) {
            table.setItems(FXCollections.observableArrayList(allTransactions));
        }
    }

    private void refreshSummary() {
        double income = allTransactions.stream().filter(t -> t != null && t.isIncome()).mapToDouble(Transaction::getAmount).sum();
        double expenses = allTransactions.stream().filter(t -> t != null && t.isExpense()).mapToDouble(Transaction::getAmount).sum();
        if (totalIncomeLabel != null) totalIncomeLabel.setText("Income: " + ValidationUtils.formatCurrency(income));
        if (totalExpenseLabel != null) totalExpenseLabel.setText("Expenses: " + ValidationUtils.formatCurrency(expenses));
        if (balLabel != null) balLabel.setText("Balance: " + ValidationUtils.formatCurrency(income - expenses));
    }

    private void applyFilter(String searchText, String typeFilter) {
        ArrayList<Transaction> filtered = new ArrayList<>();

        for (Transaction txn : allTransactions) {
            if (txn == null) continue;
            boolean matchesType = typeFilter == null || typeFilter.equals("All") ||
                (typeFilter.equals("Income") && txn.isIncome()) ||
                (typeFilter.equals("Expense") && txn.isExpense());

            boolean matchesSearch = searchText == null || searchText.isBlank() ||
                (txn.getCategory() != null && txn.getCategory().toLowerCase().contains(searchText.toLowerCase())) ||
                (txn.getDescription() != null && txn.getDescription().toLowerCase().contains(searchText.toLowerCase()));

            if (matchesType && matchesSearch) {
                filtered.add(txn);
            }
        }

        if (table != null) {
            table.setItems(FXCollections.observableArrayList(filtered));
        }
    }
}
