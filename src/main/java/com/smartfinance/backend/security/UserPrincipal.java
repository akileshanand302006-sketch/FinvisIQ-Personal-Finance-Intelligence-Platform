package com.smartfinance.backend.security;

public class UserPrincipal {
    private final int userId;
    private final String email;
    private final String name;
    private final String role;

    public UserPrincipal(int userId, String email, String name, String role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getRole() { return role; }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
