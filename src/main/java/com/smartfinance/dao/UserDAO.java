package com.smartfinance.dao;

import com.smartfinance.model.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * User Data Access Object - CRUD operations for User table.
 * Updated for Finora with user preferences, status, and extended fields.
 * Demonstrates: JDBC, ArrayList, Exception Handling.
 */
public class UserDAO {
    private final DatabaseManager dbManager;

    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(User user) {
        String sql = "INSERT INTO users (name, email, password, role, age, phone, currency, income_range, created_at, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole());
            pstmt.setInt(5, user.getAge());
            pstmt.setString(6, user.getPhone());
            pstmt.setString(7, user.getCurrency() != null ? user.getCurrency() : "INR");
            pstmt.setString(8, user.getIncomeRange() != null ? user.getIncomeRange() : "50k-100k");
            pstmt.setString(9, user.getCreatedAt() != null ? user.getCreatedAt().toString() : LocalDateTime.now().toString());
            pstmt.setString(10, user.getStatus() != null ? user.getStatus() : "ACTIVE");
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                user.setUserId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
        }
        return -1;
    }

    /** Find user by email. */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
        }
        return null;
    }

    /** Find user by name (username). */
    public User findByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by name: " + e.getMessage());
        }
        return null;
    }

    /** Find user by ID. */
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
        }
        return null;
    }

    /** Get all users (ArrayList). */
    public ArrayList<User> findAll() {
        ArrayList<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
        }
        return users;
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET name=?, email=?, password=?, role=?, age=?, phone=?, currency=?, income_range=?, status=? WHERE user_id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole());
            pstmt.setInt(5, user.getAge());
            pstmt.setString(6, user.getPhone());
            pstmt.setString(7, user.getCurrency());
            pstmt.setString(8, user.getIncomeRange());
            pstmt.setString(9, user.getStatus());
            pstmt.setInt(10, user.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
        return false;
    }

    /** Delete user by ID. */
    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
        return false;
    }

    /** Check if email already exists. */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
        }
        return false;
    }

    /** Get total user count. */
    public int getUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting users: " + e.getMessage());
        }
        return 0;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        int age = 25;
        try { age = rs.getInt("age"); } catch (SQLException ignore) {}

        String phone = null;
        try { phone = rs.getString("phone"); } catch (SQLException ignore) {}

        String currency = "INR";
        try { if (rs.getString("currency") != null) currency = rs.getString("currency"); } catch (SQLException ignore) {}

        String incomeRange = "50k-100k";
        try { if (rs.getString("income_range") != null) incomeRange = rs.getString("income_range"); } catch (SQLException ignore) {}

        LocalDateTime createdAt = LocalDateTime.now();
        try {
            String strDate = rs.getString("created_at");
            if (strDate != null && !strDate.isBlank()) {
                createdAt = LocalDateTime.parse(strDate);
            }
        } catch (Exception ignore) {}

        String status = "ACTIVE";
        try { if (rs.getString("status") != null) status = rs.getString("status"); } catch (SQLException ignore) {}

        return new User(
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("role"),
            age,
            phone,
            currency,
            incomeRange,
            createdAt,
            status
        );
    }
}
