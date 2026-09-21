package com.smartfinance.backend.dto;

public class AuthResponse {
    private String token;
    private int userId;
    private String name;
    private String email;
    private String role;
    private String currency;

    public AuthResponse() {}

    public AuthResponse(String token, int userId, String name, String email, String role, String currency) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.currency = currency != null ? currency : "INR";
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
