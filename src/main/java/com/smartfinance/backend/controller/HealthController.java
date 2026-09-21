package com.smartfinance.backend.controller;

import com.smartfinance.backend.dto.ApiResponse;
import com.smartfinance.dao.DatabaseManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "FinvisIQ Backend");
        data.put("version", "2.0.0");
        data.put("timestamp", Instant.now().toString());

        boolean dbConnected = DatabaseManager.getInstance().isDbHealthy();
        data.put("databaseConnected", dbConnected);
        data.put("dbConnected", dbConnected);

        return ResponseEntity.ok(ApiResponse.success(dbConnected ? "Service and Database are healthy" : "Service healthy, database reconnecting", data));
    }

    @GetMapping("/db")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkDatabaseHealth() {
        Map<String, Object> data = new HashMap<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DATABASE(), @@version")) {

            if (rs.next()) {
                data.put("status", "UP");
                data.put("database", rs.getString(1));
                data.put("serverVersion", rs.getString(2));
                data.put("sslActive", true);
            }
            return ResponseEntity.ok(ApiResponse.success("Database connection successful", data));
        } catch (Exception e) {
            data.put("status", "DOWN");
            data.put("error", "Database connection failed");
            return ResponseEntity.status(503).body(ApiResponse.error("Unable to connect to database", 503));
        }
    }
}
