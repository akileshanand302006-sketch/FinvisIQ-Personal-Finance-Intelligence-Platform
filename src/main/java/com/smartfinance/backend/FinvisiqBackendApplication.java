package com.smartfinance.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FinvisIQ Spring Boot Web Backend Application.
 * Headless server entry point designed for Railway deployment.
 * Exposes REST APIs, business services, and database persistence against Aiven MySQL.
 */
@SpringBootApplication(scanBasePackages = {"com.smartfinance"})
public class FinvisiqBackendApplication {

    public static void main(String[] args) {
        // Explicitly declare Server Gateway mode (direct Aiven MySQL persistence)
        com.smartfinance.api.ApiConfig.setClientMode(false);
        // Ensure headless execution for cloud servers
        System.setProperty("java.awt.headless", "true");
        // Initialize DatabaseManager and Lenient SSL Provider for Cloud TLS
        try {
            com.smartfinance.dao.DatabaseManager.getInstance();
        } catch (Exception e) {
            System.err.println("DatabaseManager initial status: " + e.getMessage());
        }
        SpringApplication.run(FinvisiqBackendApplication.class, args);
    }
}
