package com.smartfinance.dao;

import com.smartfinance.model.Liability;
import java.sql.*;
import java.util.ArrayList;

/**
 * Liability DAO — Database operations for user liabilities and loans.
 */
public class LiabilityDAO {
    private final DatabaseManager dbManager;

    public LiabilityDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(Liability item) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().createLiability(item);
        }
        String sql = "INSERT INTO liabilities (user_id, name, principal, interest_rate, tenure_months, emi, remaining_balance, due_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, item.getUserId());
            pstmt.setString(2, item.getName());
            pstmt.setDouble(3, item.getPrincipal());
            pstmt.setDouble(4, item.getInterestRate());
            pstmt.setInt(5, item.getTenureMonths());
            pstmt.setDouble(6, item.getEmi());
            pstmt.setDouble(7, item.getRemainingBalance());
            pstmt.setInt(8, item.getDueDate());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                item.setLiabilityId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting liability: " + e.getMessage());
        }
        return -1;
    }

    public ArrayList<Liability> findByUserId(int userId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().getLiabilities(userId);
        }
        ArrayList<Liability> list = new ArrayList<>();
        String sql = "SELECT * FROM liabilities WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching liabilities: " + e.getMessage());
        }
        return list;
    }

    public Liability findById(int liabilityId) {
        String sql = "SELECT * FROM liabilities WHERE liability_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, liabilityId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching liability by id: " + e.getMessage());
        }
        return null;
    }

    public boolean update(Liability item) {
        String sql = "UPDATE liabilities SET name=?, principal=?, interest_rate=?, tenure_months=?, emi=?, remaining_balance=?, due_date=? WHERE liability_id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrincipal());
            pstmt.setDouble(3, item.getInterestRate());
            pstmt.setInt(4, item.getTenureMonths());
            pstmt.setDouble(5, item.getEmi());
            pstmt.setDouble(6, item.getRemainingBalance());
            pstmt.setInt(7, item.getDueDate());
            pstmt.setInt(8, item.getLiabilityId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating liability: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int liabilityId) {
        String sql = "DELETE FROM liabilities WHERE liability_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, liabilityId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting liability: " + e.getMessage());
        }
        return false;
    }

    private Liability mapRow(ResultSet rs) throws SQLException {
        return new Liability(
            rs.getInt("liability_id"),
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getDouble("principal"),
            rs.getDouble("interest_rate"),
            rs.getInt("tenure_months"),
            rs.getDouble("emi"),
            rs.getDouble("remaining_balance"),
            rs.getInt("due_date")
        );
    }
}
