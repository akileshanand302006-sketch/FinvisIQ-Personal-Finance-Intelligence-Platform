package com.smartfinance.dao;

import com.smartfinance.model.Notification;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Notification Data Access Object - CRUD for notifications.
 * Demonstrates: JDBC, ArrayList, Exception Handling.
 */
public class NotificationDAO {
    private final DatabaseManager dbManager;

    public NotificationDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, message, date, is_read) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, notification.getUserId());
            pstmt.setString(2, notification.getMessage());
            pstmt.setString(3, notification.getDate().toString());
            pstmt.setInt(4, notification.isRead() ? 1 : 0);
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                notification.setNotificationId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting notification: " + e.getMessage());
        }
        return -1;
    }

    public ArrayList<Notification> findByUserId(int userId) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().getNotifications(userId);
        }
        ArrayList<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY date DESC, notification_id DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                notifications.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notifications: " + e.getMessage());
        }
        return notifications;
    }

    public ArrayList<Notification> findUnread(int userId) {
        ArrayList<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = 0 ORDER BY date DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                notifications.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching unread notifications: " + e.getMessage());
        }
        return notifications;
    }

    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
        }
        return false;
    }

    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking all as read: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int notificationId) {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting notification: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteAll(int userId) {
        String sql = "DELETE FROM notifications WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error clearing notifications: " + e.getMessage());
        }
        return false;
    }

    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting unread: " + e.getMessage());
        }
        return 0;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        return new Notification(
            rs.getInt("notification_id"),
            rs.getInt("user_id"),
            rs.getString("message"),
            LocalDate.parse(rs.getString("date")),
            rs.getInt("is_read") == 1
        );
    }
}
