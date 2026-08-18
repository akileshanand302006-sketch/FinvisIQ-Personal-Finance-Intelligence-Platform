package com.smartfinance.dao;

import com.smartfinance.model.RecurringTransaction;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Recurring Transaction DAO — Database access for periodic income & expenses.
 */
public class RecurringTransactionDAO {
    private final DatabaseManager dbManager;

    public RecurringTransactionDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(RecurringTransaction item) {
        String sql = "INSERT INTO recurring_transactions (user_id, amount, type, category, frequency, next_date, description, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, item.getUserId());
            pstmt.setDouble(2, item.getAmount());
            pstmt.setString(3, item.getType());
            pstmt.setString(4, item.getCategory());
            pstmt.setString(5, item.getFrequency());
            pstmt.setString(6, item.getNextDate() != null ? item.getNextDate().toString() : LocalDate.now().toString());
            pstmt.setString(7, item.getDescription());
            pstmt.setBoolean(8, item.isActive());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                item.setId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting recurring transaction: " + e.getMessage());
        }
        return -1;
    }

    public ArrayList<RecurringTransaction> findByUserId(int userId) {
        ArrayList<RecurringTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM recurring_transactions WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching recurring transactions: " + e.getMessage());
        }
        return list;
    }

    public boolean update(RecurringTransaction item) {
        String sql = "UPDATE recurring_transactions SET amount=?, type=?, category=?, frequency=?, next_date=?, description=?, is_active=? WHERE id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, item.getAmount());
            pstmt.setString(2, item.getType());
            pstmt.setString(3, item.getCategory());
            pstmt.setString(4, item.getFrequency());
            pstmt.setString(5, item.getNextDate().toString());
            pstmt.setString(6, item.getDescription());
            pstmt.setBoolean(7, item.isActive());
            pstmt.setInt(8, item.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating recurring transaction: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM recurring_transactions WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting recurring transaction: " + e.getMessage());
        }
        return false;
    }

    private RecurringTransaction mapRow(ResultSet rs) throws SQLException {
        LocalDate d = LocalDate.now();
        try {
            String dateStr = rs.getString("next_date");
            if (dateStr != null) d = LocalDate.parse(dateStr);
        } catch (Exception ignore) {}

        return new RecurringTransaction(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getDouble("amount"),
            rs.getString("type"),
            rs.getString("category"),
            rs.getString("frequency"),
            d,
            rs.getString("description"),
            rs.getBoolean("is_active")
        );
    }
}
