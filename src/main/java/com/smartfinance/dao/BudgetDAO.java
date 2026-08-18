package com.smartfinance.dao;

import com.smartfinance.model.Budget;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Budget DAO — Database access for category budgets.
 */
public class BudgetDAO {
    private final DatabaseManager dbManager;

    public BudgetDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(Budget budget) {
        String sql = "INSERT INTO budgets (user_id, category, budget_amount, period, warning_threshold, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, budget.getUserId());
            pstmt.setString(2, budget.getCategory());
            pstmt.setDouble(3, budget.getBudgetAmount());
            pstmt.setString(4, budget.getPeriod());
            pstmt.setDouble(5, budget.getWarningThreshold());
            pstmt.setString(6, budget.getCreatedAt() != null ? budget.getCreatedAt().toString() : LocalDateTime.now().toString());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                budget.setBudgetId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting budget: " + e.getMessage());
        }
        return -1;
    }

    public ArrayList<Budget> findByUserId(int userId) {
        ArrayList<Budget> list = new ArrayList<>();
        String sql = "SELECT * FROM budgets WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching budgets: " + e.getMessage());
        }
        return list;
    }

    public boolean update(Budget budget) {
        String sql = "UPDATE budgets SET budget_amount=?, period=?, warning_threshold=? WHERE budget_id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, budget.getBudgetAmount());
            pstmt.setString(2, budget.getPeriod());
            pstmt.setDouble(3, budget.getWarningThreshold());
            pstmt.setInt(4, budget.getBudgetId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating budget: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int budgetId) {
        String sql = "DELETE FROM budgets WHERE budget_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, budgetId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting budget: " + e.getMessage());
        }
        return false;
    }

    private Budget mapRow(ResultSet rs) throws SQLException {
        LocalDateTime dt = LocalDateTime.now();
        try {
            String s = rs.getString("created_at");
            if (s != null) dt = LocalDateTime.parse(s);
        } catch (Exception ignore) {}

        return new Budget(
            rs.getInt("budget_id"),
            rs.getInt("user_id"),
            rs.getString("category"),
            rs.getDouble("budget_amount"),
            rs.getString("period"),
            rs.getDouble("warning_threshold"),
            dt
        );
    }
}
