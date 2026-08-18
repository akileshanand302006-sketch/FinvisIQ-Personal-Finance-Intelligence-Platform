package com.smartfinance.dao;

import com.smartfinance.model.AuditLog;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Audit Log DAO — Database access for security audit logs.
 */
public class AuditLogDAO {
    private final DatabaseManager dbManager;

    public AuditLogDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, action, description, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, log.getUserId());
            pstmt.setString(2, log.getAction());
            pstmt.setString(3, log.getDescription());
            pstmt.setString(4, log.getTimestamp() != null ? log.getTimestamp().toString() : LocalDateTime.now().toString());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                log.setLogId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting audit log: " + e.getMessage());
        }
        return -1;
    }

    public ArrayList<AuditLog> findAll() {
        ArrayList<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY log_id DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching audit logs: " + e.getMessage());
        }
        return list;
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        LocalDateTime dt = LocalDateTime.now();
        try {
            String s = rs.getString("timestamp");
            if (s != null) dt = LocalDateTime.parse(s);
        } catch (Exception ignore) {}

        return new AuditLog(
            rs.getInt("log_id"),
            rs.getInt("user_id"),
            rs.getString("action"),
            rs.getString("description"),
            dt
        );
    }
}
