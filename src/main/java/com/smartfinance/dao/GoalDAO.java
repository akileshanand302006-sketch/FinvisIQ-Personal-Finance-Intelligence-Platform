package com.smartfinance.dao;

import com.smartfinance.model.Goal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Goal Data Access Object - CRUD operations for goals.
 * Updated for FinvisIQ with priority, category, and monthly contributions.
 * Demonstrates: JDBC, ArrayList, Exception Handling.
 */
public class GoalDAO {
    private final DatabaseManager dbManager;

    public GoalDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(Goal goal) {
        String sql = "INSERT INTO goals (user_id, goal_name, target_amount, saved_amount, deadline, status, priority, category, monthly_contribution, expected_return) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, goal.getUserId());
            pstmt.setString(2, goal.getGoalName());
            pstmt.setDouble(3, goal.getTargetAmount());
            pstmt.setDouble(4, goal.getSavedAmount());
            pstmt.setString(5, goal.getDeadline().toString());
            pstmt.setString(6, goal.getStatus());
            pstmt.setString(7, goal.getPriority() != null ? goal.getPriority() : "MEDIUM");
            pstmt.setString(8, goal.getCategory() != null ? goal.getCategory() : "General");
            pstmt.setDouble(9, goal.getMonthlyContribution());
            pstmt.setDouble(10, goal.getExpectedReturn());
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                goal.setGoalId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting goal: " + e.getMessage());
        }
        return -1;
    }

    public ArrayList<Goal> findByUserId(int userId) {
        ArrayList<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE user_id = ? ORDER BY deadline";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                goals.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching goals: " + e.getMessage());
        }
        return goals;
    }

    public boolean update(Goal goal) {
        String sql = "UPDATE goals SET goal_name=?, target_amount=?, saved_amount=?, deadline=?, status=?, priority=?, category=?, monthly_contribution=?, expected_return=? WHERE goal_id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, goal.getGoalName());
            pstmt.setDouble(2, goal.getTargetAmount());
            pstmt.setDouble(3, goal.getSavedAmount());
            pstmt.setString(4, goal.getDeadline().toString());
            pstmt.setString(5, goal.getStatus());
            pstmt.setString(6, goal.getPriority());
            pstmt.setString(7, goal.getCategory());
            pstmt.setDouble(8, goal.getMonthlyContribution());
            pstmt.setDouble(9, goal.getExpectedReturn());
            pstmt.setInt(10, goal.getGoalId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating goal: " + e.getMessage());
        }
        return false;
    }

    public boolean updateSavedAmount(int goalId, double savedAmount) {
        String sql = "UPDATE goals SET saved_amount = ? WHERE goal_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, savedAmount);
            pstmt.setInt(2, goalId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating saved amount: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int goalId) {
        String sql = "DELETE FROM goals WHERE goal_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, goalId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting goal: " + e.getMessage());
        }
        return false;
    }

    public int getActiveGoalCount(int userId) {
        String sql = "SELECT COUNT(*) FROM goals WHERE user_id = ? AND status = 'ACTIVE'";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting goals: " + e.getMessage());
        }
        return 0;
    }

    private Goal mapRow(ResultSet rs) throws SQLException {
        String priority = "MEDIUM";
        try { if (rs.getString("priority") != null) priority = rs.getString("priority"); } catch (SQLException ignore) {}

        String category = "General";
        try { if (rs.getString("category") != null) category = rs.getString("category"); } catch (SQLException ignore) {}

        double mc = 0;
        try { mc = rs.getDouble("monthly_contribution"); } catch (SQLException ignore) {}

        double er = 0;
        try { er = rs.getDouble("expected_return"); } catch (SQLException ignore) {}

        return new Goal(
            rs.getInt("goal_id"),
            rs.getInt("user_id"),
            rs.getString("goal_name"),
            rs.getDouble("target_amount"),
            rs.getDouble("saved_amount"),
            LocalDate.parse(rs.getString("deadline")),
            rs.getString("status"),
            priority,
            category,
            mc,
            er
        );
    }
}
