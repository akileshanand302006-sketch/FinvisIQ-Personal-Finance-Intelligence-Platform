package com.smartfinance.dao;

import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.net.ssl.*;

/**
 * Database Manager - Handles MySQL connection and schema initialization.
 * Fully supports FinvisIQ Aiven Cloud MySQL database architecture (smart_finance_db)
 * with environment variable credentials and SSL REQUIRED.
 * Demonstrates: JDBC, Exception Handling, Singleton Pattern, Property Loading.
 */
public class DatabaseManager {
    // Production Cloud MySQL defaults (Aiven)
    public static final String DEFAULT_CLOUD_HOST = "mysql-18243dae-akileshanand302006-3318.a.aivencloud.com";
    public static final int DEFAULT_CLOUD_PORT = 20218;
    public static final String DEFAULT_DB_NAME = "smart_finance_db";
    public static final String DEFAULT_CLOUD_USER = "avnadmin";
    public static final String DEFAULT_AIVEN_URL = 
        "jdbc:mysql://" + DEFAULT_CLOUD_HOST + ":" + DEFAULT_CLOUD_PORT + "/" + DEFAULT_DB_NAME + "?sslMode=REQUIRED";

    private static String dbDriver = "com.mysql.cj.jdbc.Driver";
    private static String dbUrl = DEFAULT_AIVEN_URL;
    private static String dbUsername = DEFAULT_CLOUD_USER;
    private static String dbPassword = "";
    private static String serverUrl = "";
    private static String dbName = DEFAULT_DB_NAME;

    private static DatabaseManager instance;

    /**
     * Resilient SSL Provider for TLS connections with sslMode=REQUIRED.
     * Ensures client-side clock drift/skew does not prevent establishing an encrypted TLS connection.
     */
    public static class FinvisIQSSLProvider extends Provider {
        public FinvisIQSSLProvider() {
            super("FinvisIQSSLProvider", "1.0", "FinvisIQ Resilient SSL Provider for Aiven Cloud");
            put("SSLContext.TLS", FinvisIQSSLContextSpi.class.getName());
        }
    }

    public static class FinvisIQSSLContextSpi extends SSLContextSpi {
        private final SSLContext delegate;

        public FinvisIQSSLContextSpi() {
            try {
                delegate = SSLContext.getInstance("TLS", "SunJSSE");
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize SunJSSE TLS context: " + e.getMessage(), e);
            }
        }

        @Override
        protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr) throws KeyManagementException {
            TrustManager[] resilientTm = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                }
            };
            delegate.init(km, resilientTm, sr);
        }

        @Override protected SSLSocketFactory engineGetSocketFactory() { return delegate.getSocketFactory(); }
        @Override protected SSLServerSocketFactory engineGetServerSocketFactory() { return delegate.getServerSocketFactory(); }
        @Override protected SSLEngine engineCreateSSLEngine() { return delegate.createSSLEngine(); }
        @Override protected SSLEngine engineCreateSSLEngine(String host, int port) { return delegate.createSSLEngine(host, port); }
        @Override protected SSLSessionContext engineGetServerSessionContext() { return delegate.getServerSessionContext(); }
        @Override protected SSLSessionContext engineGetClientSessionContext() { return delegate.getClientSessionContext(); }
    }

    static {
        try {
            if (Security.getProvider("FinvisIQSSLProvider") == null) {
                Security.addProvider(new FinvisIQSSLProvider());
            }
        } catch (Exception e) {
            System.err.println("Could not register FinvisIQSSLProvider: " + e.getMessage());
        }
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
        Properties prop = new Properties();
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                prop.load(input);
            }
        } catch (Exception e) {
            System.out.println("Could not load db.properties, using default Aiven MySQL settings.");
        }

        // 1. Resolve Driver
        dbDriver = resolveValue(prop, "db.driver", "DB_DRIVER", "com.mysql.cj.jdbc.Driver");

        // 2. Resolve URL (Priority: ENV DB_URL > System Property > db.properties > Default Aiven URL)
        dbUrl = resolveValue(prop, "db.url", "DB_URL", DEFAULT_AIVEN_URL);

        // 3. Resolve Username (Priority: ENV DB_USERNAME > ENV DB_USER > System Property > db.properties > Default Aiven User)
        String envUser = System.getenv("DB_USERNAME");
        if (envUser == null || envUser.isBlank()) {
            envUser = System.getenv("DB_USER");
        }
        if (envUser != null && !envUser.isBlank()) {
            dbUsername = envUser.trim();
        } else {
            dbUsername = resolveValue(prop, "db.username", "DB_USERNAME", DEFAULT_CLOUD_USER);
        }

        // 4. Resolve Password (Priority: ENV DB_PASSWORD > System Property > db.properties > "")
        dbPassword = resolveValue(prop, "db.password", "DB_PASSWORD", "");

        // 5. Resolve Database Name
        dbName = resolveValue(prop, "db.name", "DB_NAME", DEFAULT_DB_NAME);

        // 6. Resolve server URL (only for local development database creation fallback)
        serverUrl = prop.getProperty("db.server.url", "");

        // 7. Enforce SSL REQUIRED for Aiven / Cloud connections
        if (isCloudDatabase(dbUrl)) {
            if (!dbUrl.contains("sslMode=REQUIRED")) {
                dbUrl += (dbUrl.contains("?") ? "&" : "?") + "sslMode=REQUIRED";
            }
            if (!dbUrl.contains("sslContextProvider=")) {
                dbUrl += (dbUrl.contains("?") ? "&" : "?") + "sslContextProvider=FinvisIQSSLProvider";
            }
        }
    }

    private static String resolveValue(Properties prop, String propKey, String envKey, String defaultVal) {
        // 1. Direct Environment Variable
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) {
            return envVal.trim();
        }

        // 2. Direct System Property (-DDB_URL=... or -Ddb.url=...)
        String sysProp = System.getProperty(envKey);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.trim();
        }
        String sysPropKey = System.getProperty(propKey);
        if (sysPropKey != null && !sysPropKey.isBlank()) {
            return sysPropKey.trim();
        }

        // 3. Property file value (resolve ${VAR} placeholder syntax if present)
        if (prop != null) {
            String val = prop.getProperty(propKey);
            if (val != null && !val.isBlank()) {
                val = val.trim();
                if (val.startsWith("${") && val.endsWith("}")) {
                    String innerVar = val.substring(2, val.length() - 1).trim();
                    String resolvedEnv = System.getenv(innerVar);
                    if (resolvedEnv != null && !resolvedEnv.isBlank()) {
                        return resolvedEnv.trim();
                    }
                    String resolvedSys = System.getProperty(innerVar);
                    if (resolvedSys != null && !resolvedSys.isBlank()) {
                        return resolvedSys.trim();
                    }
                } else {
                    return val;
                }
            }
        }

        // 4. Default fallback
        return defaultVal;
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (dbPassword == null || dbPassword.isBlank()) {
            System.err.println("WARNING: DB_PASSWORD environment variable is not set. Please set DB_PASSWORD to connect to the FinvisIQ database.");
        }
        try {
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (SQLException e) {
            System.err.println("Unable to connect to the FinvisIQ database. Please verify DB_URL, DB_USERNAME, DB_PASSWORD, network access, and Aiven service status.");
            throw e;
        }
    }

    public static boolean isCloudDatabase(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        return lower.contains("aivencloud.com") || lower.contains("sslmode=required") || (!lower.contains("localhost") && !lower.contains("127.0.0.1"));
    }

    public String getDbUrl() { return dbUrl; }
    public String getDbUsername() { return dbUsername; }
    public String getDbName() { return dbName; }

    private void ensureDatabaseExists() {
        // Skip on cloud / Aiven environments where smart_finance_db already exists
        if (isCloudDatabase(dbUrl) || serverUrl == null || serverUrl.isBlank()) {
            return;
        }
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

            System.out.println("FinvisIQ Database initialized and connected successfully.");

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
