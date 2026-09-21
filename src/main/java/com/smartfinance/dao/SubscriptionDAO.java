package com.smartfinance.dao;

import com.smartfinance.model.Subscription;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Subscription DAO — Database operations for recurring subscriptions.
 */
public class SubscriptionDAO {
    private final DatabaseManager dbManager;

    public SubscriptionDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(Subscription sub) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().createSubscription(sub);
        }
        String sql = "INSERT INTO subscriptions (user_id, service_name, amount, billing_cycle, next_billing_date, category, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, sub.getUserId());
            pstmt.setString(2, sub.getServiceName());
            pstmt.setDouble(3, sub.getAmount());
            pstmt.setString(4, sub.getBillingCycle());
            pstmt.setString(5, sub.getNextBillingDate() != null ? sub.getNextBillingDate().toString() : LocalDate.now().toString());
            pstmt.setString(6, sub.getCategory());
            pstmt.setString(7, sub.getStatus());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                sub.setSubscriptionId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting subscription: " + e.getMessage());
        }
        return -1;
    }

    public ArrayList<Subscription> findByUserId(int userId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().getSubscriptions(userId);
        }
        ArrayList<Subscription> list = new ArrayList<>();
        String sql = "SELECT * FROM subscriptions WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subscriptions: " + e.getMessage());
        }
        return list;
    }

    public Subscription findById(int subscriptionId) {
        String sql = "SELECT * FROM subscriptions WHERE subscription_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subscriptionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subscription by id: " + e.getMessage());
        }
        return null;
    }

    public boolean update(Subscription sub) {
        String sql = "UPDATE subscriptions SET service_name=?, amount=?, billing_cycle=?, next_billing_date=?, category=?, status=? WHERE subscription_id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sub.getServiceName());
            pstmt.setDouble(2, sub.getAmount());
            pstmt.setString(3, sub.getBillingCycle());
            pstmt.setString(4, sub.getNextBillingDate() != null ? sub.getNextBillingDate().toString() : LocalDate.now().toString());
            pstmt.setString(5, sub.getCategory());
            pstmt.setString(6, sub.getStatus());
            pstmt.setInt(7, sub.getSubscriptionId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating subscription: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int subscriptionId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().deleteSubscription(subscriptionId);
        }
        String sql = "DELETE FROM subscriptions WHERE subscription_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subscriptionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting subscription: " + e.getMessage());
        }
        return false;
    }

    private Subscription mapRow(ResultSet rs) throws SQLException {
        LocalDate d = LocalDate.now();
        try {
            String dateStr = rs.getString("next_billing_date");
            if (dateStr != null) d = LocalDate.parse(dateStr);
        } catch (Exception ignore) {}

        return new Subscription(
            rs.getInt("subscription_id"),
            rs.getInt("user_id"),
            rs.getString("service_name"),
            rs.getDouble("amount"),
            rs.getString("billing_cycle"),
            d,
            rs.getString("category"),
            rs.getString("status")
        );
    }
}
