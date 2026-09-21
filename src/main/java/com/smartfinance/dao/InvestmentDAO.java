package com.smartfinance.dao;

import com.smartfinance.model.Investment;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Investment Data Access Object - CRUD operations for investments.
 * Demonstrates: JDBC, ArrayList, HashMap.
 */
public class InvestmentDAO {
    private final DatabaseManager dbManager;

    public InvestmentDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(Investment inv) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().createInvestment(inv);
        }
        String sql = "INSERT INTO investments (user_id, type, amount, return_rate, start_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, inv.getUserId());
            pstmt.setString(2, inv.getType());
            pstmt.setDouble(3, inv.getAmount());
            pstmt.setDouble(4, inv.getReturnRate());
            pstmt.setString(5, inv.getStartDate().toString());
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                inv.setInvestmentId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting investment: " + e.getMessage());
        }
        return -1;
    }

    public ArrayList<Investment> findByUserId(int userId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().getInvestments(userId);
        }
        ArrayList<Investment> investments = new ArrayList<>();
        String sql = "SELECT * FROM investments WHERE user_id = ? ORDER BY start_date DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                investments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching investments: " + e.getMessage());
        }
        return investments;
    }

    public Investment findById(int investmentId) {
        String sql = "SELECT * FROM investments WHERE investment_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, investmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching investment by id: " + e.getMessage());
        }
        return null;
    }

    /** Get investment type distribution using HashMap. */
    public HashMap<String, Double> getTypeDistribution(int userId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            HashMap<String, Double> distribution = new HashMap<>();
            for (Investment inv : findByUserId(userId)) {
                if (inv != null && inv.getType() != null) {
                    distribution.merge(inv.getType(), inv.getAmount(), (a, b) -> (a != null ? a : 0.0) + (b != null ? b : 0.0));
                }
            }
            return distribution;
        }
        HashMap<String, Double> distribution = new HashMap<>();
        String sql = "SELECT type, SUM(amount) as total FROM investments WHERE user_id = ? GROUP BY type";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                distribution.put(rs.getString("type"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching investment distribution: " + e.getMessage());
        }
        return distribution;
    }

    /** Get total investment amount. */
    public double getTotalInvestment(int userId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return findByUserId(userId).stream().filter(inv -> inv != null).mapToDouble(inv -> inv.getAmount()).sum();
        }
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM investments WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error calculating total investment: " + e.getMessage());
        }
        return 0;
    }

    public boolean delete(int investmentId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().deleteInvestment(investmentId);
        }
        String sql = "DELETE FROM investments WHERE investment_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, investmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting investment: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Investment inv) {
        String sql = "UPDATE investments SET type=?, amount=?, return_rate=?, start_date=? WHERE investment_id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, inv.getType());
            pstmt.setDouble(2, inv.getAmount());
            pstmt.setDouble(3, inv.getReturnRate());
            pstmt.setString(4, inv.getStartDate().toString());
            pstmt.setInt(5, inv.getInvestmentId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating investment: " + e.getMessage());
        }
        return false;
    }

    private Investment mapRow(ResultSet rs) throws SQLException {
        return new Investment(
            rs.getInt("investment_id"),
            rs.getInt("user_id"),
            rs.getString("type"),
            rs.getDouble("amount"),
            rs.getDouble("return_rate"),
            LocalDate.parse(rs.getString("start_date"))
        );
    }
}
