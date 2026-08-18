package com.smartfinance.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Database Manager - Handles MySQL connection and schema initialization.
 * Fully supports Finora database architecture with schema migrations.
 * Demonstrates: JDBC, Exception Handling, Singleton Pattern, Property Loading.
 */
public class DatabaseManager {
    private static String dbDriver = "com.mysql.cj.jdbc.Driver";
    private static String dbUrl = "jdbc:mysql://localhost:3306/smart_finance_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String dbUsername = "root";
    private static String dbPassword = "1234";
    private static String serverUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String dbName = "smart_finance_db";

    private static DatabaseManager instance;

    static {
        loadProperties();
    }

    private DatabaseManager() {
        try {
            Class.forName(dbDriver);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        }
        ensureDatabaseExists();
        initializeDatabase();
    }

    private static void loadProperties() {
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                if (prop.getProperty("db.driver") != null) dbDriver = prop.getProperty("db.driver");
                if (prop.getProperty("db.url") != null) dbUrl = prop.getProperty("db.url");
                if (prop.getProperty("db.username") != null) dbUsername = prop.getProperty("db.username");
                if (prop.getProperty("db.password") != null) dbPassword = prop.getProperty("db.password");
                if (prop.getProperty("db.server.url") != null) serverUrl = prop.getProperty("db.server.url");
                if (prop.getProperty("db.name") != null) dbName = prop.getProperty("db.name");
            }
        } catch (Exception e) {
            System.out.println("Could not load db.properties, using default MySQL settings.");
        }

        // System property or Environment variable overrides
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isBlank()) dbUrl = envUrl;
        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.isBlank()) dbUsername = envUser;
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) dbPassword = envPass;
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    private void ensureDatabaseExists() {
        try (Connection conn = DriverManager.getConnection(serverUrl, dbUsername, dbPassword);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
        } catch (SQLException e) {
            // DB may already exist or URL auto-creates it
        }
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Users Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL DEFAULT 'USER',
                    age INT DEFAULT 25
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // 2. Transactions Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    amount DOUBLE NOT NULL CHECK(amount > 0),
                    type VARCHAR(20) NOT NULL CHECK(type IN ('INCOME', 'EXPENSE')),
                    category VARCHAR(100) NOT NULL,
                    date VARCHAR(20) NOT NULL,
                    description VARCHAR(255),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // 3. Goals Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS goals (
                    goal_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    goal_name VARCHAR(255) NOT NULL,
                    target_amount DOUBLE NOT NULL CHECK(target_amount > 0),
                    saved_amount DOUBLE NOT NULL DEFAULT 0,
                    deadline VARCHAR(20) NOT NULL,
                    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // 4. Investments Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS investments (
                    investment_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    type VARCHAR(100) NOT NULL,
                    amount DOUBLE NOT NULL CHECK(amount > 0),
                    return_rate DOUBLE NOT NULL,
                    start_date VARCHAR(20) NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // 5. Notifications Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    notification_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    message VARCHAR(500) NOT NULL,
                    date VARCHAR(20) NOT NULL,
                    is_read TINYINT(1) NOT NULL DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // 6. User Settings Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_settings (
                    setting_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    setting_key VARCHAR(100) NOT NULL,
                    setting_value VARCHAR(255) NOT NULL,
                    UNIQUE KEY user_key (user_id, setting_key),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // 7. Budgets Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS budgets (
                    budget_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    category VARCHAR(100) NOT NULL,
                    budget_amount DOUBLE NOT NULL CHECK(budget_amount > 0),
                    period VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
                    warning_threshold DOUBLE NOT NULL DEFAULT 80.0,
                    created_at VARCHAR(30),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // 8. Subscriptions Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS subscriptions (
                    subscription_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    service_name VARCHAR(255) NOT NULL,
                    amount DOUBLE NOT NULL CHECK(amount > 0),
                    billing_cycle VARCHAR(50) NOT NULL DEFAULT 'MONTHLY',
                    next_billing_date VARCHAR(20) NOT NULL,
                    category VARCHAR(100) NOT NULL DEFAULT 'Entertainment',
                    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // 9. Recurring Transactions Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS recurring_transactions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    amount DOUBLE NOT NULL CHECK(amount > 0),
                    type VARCHAR(20) NOT NULL CHECK(type IN ('INCOME', 'EXPENSE')),
                    category VARCHAR(100) NOT NULL,
                    frequency VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
                    next_date VARCHAR(20) NOT NULL,
                    description VARCHAR(255),
                    is_active TINYINT(1) NOT NULL DEFAULT 1,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // 10. Assets Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS assets (
                    asset_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    type VARCHAR(100) NOT NULL,
                    value DOUBLE NOT NULL CHECK(value >= 0),
                    notes VARCHAR(255),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // 11. Liabilities Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS liabilities (
                    liability_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    principal DOUBLE NOT NULL CHECK(principal >= 0),
                    interest_rate DOUBLE NOT NULL DEFAULT 0,
                    tenure_months INT NOT NULL DEFAULT 12,
                    emi DOUBLE NOT NULL DEFAULT 0,
                    remaining_balance DOUBLE NOT NULL DEFAULT 0,
                    due_date INT NOT NULL DEFAULT 5,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // 12. Audit Logs Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS audit_logs (
                    log_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT,
                    action VARCHAR(100) NOT NULL,
                    description VARCHAR(500) NOT NULL,
                    timestamp VARCHAR(30) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

            // Execute Column Migrations safely
            addColumnIfNotExists(stmt, "users", "phone", "VARCHAR(30)");
            addColumnIfNotExists(stmt, "users", "currency", "VARCHAR(10) DEFAULT 'INR'");
            addColumnIfNotExists(stmt, "users", "income_range", "VARCHAR(50) DEFAULT '50k-100k'");
            addColumnIfNotExists(stmt, "users", "created_at", "VARCHAR(30)");
            addColumnIfNotExists(stmt, "users", "status", "VARCHAR(20) DEFAULT 'ACTIVE'");

            addColumnIfNotExists(stmt, "transactions", "payment_method", "VARCHAR(50) DEFAULT 'Cash'");

            addColumnIfNotExists(stmt, "goals", "priority", "VARCHAR(20) DEFAULT 'MEDIUM'");
            addColumnIfNotExists(stmt, "goals", "category", "VARCHAR(100) DEFAULT 'General'");
            addColumnIfNotExists(stmt, "goals", "monthly_contribution", "DOUBLE DEFAULT 0");
            addColumnIfNotExists(stmt, "goals", "expected_return", "DOUBLE DEFAULT 0");

            addColumnIfNotExists(stmt, "investments", "name", "VARCHAR(255)");
            addColumnIfNotExists(stmt, "investments", "current_value", "DOUBLE DEFAULT 0");
            addColumnIfNotExists(stmt, "investments", "risk_level", "VARCHAR(20) DEFAULT 'MEDIUM'");

            addColumnIfNotExists(stmt, "notifications", "type", "VARCHAR(50) DEFAULT 'SYSTEM'");
            addColumnIfNotExists(stmt, "notifications", "priority", "VARCHAR(20) DEFAULT 'INFO'");

            System.out.println("Finora Database initialized and migrated successfully.");

        } catch (SQLException e) {
            System.err.println("MySQL Database initialization failed: " + e.getMessage());
        }
    }

    private void addColumnIfNotExists(Statement stmt, String tableName, String columnName, String columnSpec) {
        try {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnSpec);
        } catch (SQLException ignore) {
            // Column already exists or table issue
        }
    }
}
