package com.smartfinance.model;

import java.time.LocalDateTime;

/**
 * User model representing both Admin and Common users.
 * Extended for FinvisIQ with profile settings, currency, and status.
 */
public class User {
    private int userId;
    private String name;
    private String email;
    private String password;
    private String role; // "ADMIN" or "USER"
    private int age; // User's age for AI insights
    private String phone;
    private String currency = "INR"; // Default INR (₹)
    private String incomeRange = "50k-100k";
    private LocalDateTime createdAt = LocalDateTime.now();
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED

    public User() {}

    public User(String name, String email, String password, String role, int age) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.age = age;
    }

    public User(String name, String email, String password, String role) {
        this(name, email, password, role, 25);
    }

    public User(int userId, String name, String email, String password, String role, int age) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.age = age;
    }

    public User(int userId, String name, String email, String password, String role, int age,
                String phone, String currency, String incomeRange, LocalDateTime createdAt, String status) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.age = age;
        this.phone = phone;
        this.currency = currency != null ? currency : "INR";
        this.incomeRange = incomeRange != null ? incomeRange : "50k-100k";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.status = status != null ? status : "ACTIVE";
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getIncomeRange() { return incomeRange; }
    public void setIncomeRange(String incomeRange) { this.incomeRange = incomeRange; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
    public boolean isActive() { return "ACTIVE".equalsIgnoreCase(status); }

    public String getCurrencySymbol() {
        return switch (currency) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "JPY" -> "¥";
            default -> "₹";
        };
    }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name='" + name + "', email='" + email + "', role='" + role + "', age=" + age + "}";
    }
}
