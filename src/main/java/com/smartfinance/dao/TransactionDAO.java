package com.smartfinance.dao;

import com.smartfinance.model.Transaction;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Transaction Data Access Object - CRUD and analytics queries.
 * Updated for FinvisIQ with paymentMethod support.
 * Demonstrates: JDBC, ArrayList, HashMap, List Interface.
 */
public class TransactionDAO {
    private final DatabaseManager dbManager;

    public TransactionDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /** Insert a new transaction. */
    public int insert(Transaction txn) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().createTransaction(txn);
        }
        String sql = "INSERT INTO transactions (user_id, amount, type, category, date, description, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, txn.getUserId());
            pstmt.setDouble(2, txn.getAmount());
            pstmt.setString(3, txn.getType());
            pstmt.setString(4, txn.getCategory());
            pstmt.setString(5, txn.getDate().toString());
            pstmt.setString(6, txn.getDescription());
            pstmt.setString(7, txn.getPaymentMethod() != null ? txn.getPaymentMethod() : "Cash");
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                txn.setTransactionId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting transaction: " + e.getMessage());
        }
        return -1;
    }

    /** Get all transactions for a user (ArrayList). */
    public ArrayList<Transaction> findByUserId(int userId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().getTransactions(userId);
        }
        ArrayList<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return transactions;
    }

    /** Find a transaction by id. */
    public Transaction findById(int transactionId) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transaction by id: " + e.getMessage());
        }
        return null;
    }

    /** Get ALL transactions across all users (for admin). */
    public ArrayList<Transaction> findAll() {
        ArrayList<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                transactions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all transactions: " + e.getMessage());
        }
        return transactions;
    }

    /** Get transactions for a specific month. */
    public List<Transaction> findByMonth(int userId, int year, int month) {
        List<Transaction> transactions = new ArrayList<>();
        String monthStr = String.format("%d-%02d", year, month);
        String sql = "SELECT * FROM transactions WHERE user_id = ? AND date LIKE ? ORDER BY date DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, monthStr + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching monthly transactions: " + e.getMessage());
        }
        return transactions;
    }

    /** Get category-wise totals using HashMap. */
    public HashMap<String, Double> getCategoryTotals(int userId, String type) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            HashMap<String, Double> categoryMap = new HashMap<>();
            for (Transaction t : findByUserId(userId)) {
                if (t != null && (type == null || type.equalsIgnoreCase(t.getType()))) {
                    if (t.getCategory() != null) {
                        categoryMap.merge(t.getCategory(), t.getAmount(), (a, b) -> (a != null ? a : 0.0) + (b != null ? b : 0.0));
                    }
                }
            }
            return categoryMap;
        }
        HashMap<String, Double> categoryMap = new HashMap<>();
        String sql = "SELECT category, SUM(amount) as total FROM transactions WHERE user_id = ? AND type = ? GROUP BY category";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                categoryMap.put(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching category totals: " + e.getMessage());
        }
        return categoryMap;
    }

    /** Get monthly totals (HashMap: "YYYY-MM" -> total). */
    public HashMap<String, Double> getMonthlyTotals(int userId, String type) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            HashMap<String, Double> monthlyMap = new HashMap<>();
            for (Transaction t : findByUserId(userId)) {
                if (t != null && (type == null || type.equalsIgnoreCase(t.getType()))) {
                    if (t.getDate() != null) {
                        String monthKey = String.format("%d-%02d", t.getDate().getYear(), t.getDate().getMonthValue());
                        monthlyMap.merge(monthKey, t.getAmount(), (a, b) -> (a != null ? a : 0.0) + (b != null ? b : 0.0));
                    }
                }
            }
            return monthlyMap;
        }
        HashMap<String, Double> monthlyMap = new HashMap<>();
        String sql = "SELECT SUBSTR(date, 1, 7) as month, SUM(amount) as total FROM transactions WHERE user_id = ? AND type = ? GROUP BY month ORDER BY month";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                monthlyMap.put(rs.getString("month"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching monthly totals: " + e.getMessage());
        }
        return monthlyMap;
    }

    /** Get total income for a user. */
    public double getTotalIncome(int userId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return findByUserId(userId).stream()
                    .filter(t -> t != null && "INCOME".equalsIgnoreCase(t.getType()))
                    .mapToDouble(t -> t.getAmount())
                    .sum();
        }
        return getTotal(userId, "INCOME");
    }

    /** Get total expenses for a user. */
    public double getTotalExpenses(int userId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return findByUserId(userId).stream()
                    .filter(t -> t != null && "EXPENSE".equalsIgnoreCase(t.getType()))
                    .mapToDouble(t -> t.getAmount())
                    .sum();
        }
        return getTotal(userId, "EXPENSE");
    }

    private double getTotal(int userId, String type) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error calculating total: " + e.getMessage());
        }
        return 0;
    }

    /** Delete a transaction. */
    public boolean delete(int transactionId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().deleteTransaction(transactionId);
        }
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
        }
        return false;
    }

    /** Update a transaction. */
    public boolean update(Transaction txn) {
        String sql = "UPDATE transactions SET amount=?, type=?, category=?, date=?, description=?, payment_method=? WHERE transaction_id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, txn.getAmount());
            pstmt.setString(2, txn.getType());
            pstmt.setString(3, txn.getCategory());
            pstmt.setString(4, txn.getDate().toString());
            pstmt.setString(5, txn.getDescription());
            pstmt.setString(6, txn.getPaymentMethod() != null ? txn.getPaymentMethod() : "Cash");
            pstmt.setInt(7, txn.getTransactionId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
        }
        return false;
    }

    /** Get transaction count for a user. */
    public int getTransactionCount(int userId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting transactions: " + e.getMessage());
        }
        return 0;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        String pm = "Cash";
        try {
            if (rs.getString("payment_method") != null) pm = rs.getString("payment_method");
        } catch (SQLException ignore) {}

        return new Transaction(
            rs.getInt("transaction_id"),
            rs.getInt("user_id"),
            rs.getDouble("amount"),
            rs.getString("type"),
            rs.getString("category"),
            LocalDate.parse(rs.getString("date")),
            rs.getString("description"),
            pm
        );
    }
}
