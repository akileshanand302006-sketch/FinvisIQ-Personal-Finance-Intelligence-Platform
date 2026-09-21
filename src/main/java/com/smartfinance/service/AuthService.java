package com.smartfinance.service;

import com.smartfinance.dao.UserDAO;
import com.smartfinance.model.User;
import com.smartfinance.util.PasswordUtil;

/**
 * Authentication Service - Handles secure login and registration.
 * Supports SHA-256 salted password hashing & legacy password auto-migration.
 * Demonstrates: Encapsulation, Security, String Handling.
 */
public class AuthService {
    private final UserDAO userDAO;
    private User currentUser;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticate user with username and password.
     * @return User if successful, null otherwise
     */
    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            User user = com.smartfinance.api.ApiClient.getInstance().login(username, password);
            if (user != null) {
                this.currentUser = user;
            }
            return user;
        }

        User user = userDAO.findByName(username.trim());
        if (user == null) {
            // Also try email login fallback
            user = userDAO.findByEmail(username.trim().toLowerCase());
        }

        if (user != null && user.isActive()) {
            boolean valid = PasswordUtil.verifyPassword(password, user.getPassword());
            if (valid) {
                // If stored password was plain text, auto-migrate to salted hash
                if (!user.getPassword().contains(":")) {
                    String newHash = PasswordUtil.hashPassword(password);
                    user.setPassword(newHash);
                    userDAO.update(user);
                }
                this.currentUser = user;
                return user;
            }
        }
        return null;
    }

    /**
     * Register a new user with salted password hashing.
     * @return User if successful, null if email exists
     */
    public User register(String name, String email, String password, String role, int age) {
        if (name == null || name.isBlank()) return null;
        if (email == null || email.isBlank()) return null;
        if (password == null || password.length() < 4) return null;
        if (age < 18 || age > 100) return null;

        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            User user = com.smartfinance.api.ApiClient.getInstance().register(name, email, password, role, age);
            if (user != null) {
                this.currentUser = user;
            }
            return user;
        }

        String cleanEmail = email.trim().toLowerCase();

        if (userDAO.emailExists(cleanEmail)) {
            return null; // Email already registered
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        User newUser = new User(name.trim(), cleanEmail, hashedPassword, role.toUpperCase(), age);
        int id = userDAO.insert(newUser);
        if (id > 0) {
            this.currentUser = newUser;
            return newUser;
        }
        return null;
    }

    /** Get currently logged-in user. */
    public User getCurrentUser() {
        return currentUser;
    }

    /** Logout - clear current user. */
    public void logout() {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            com.smartfinance.api.ApiClient.getInstance().logout();
        }
        this.currentUser = null;
    }

    /** Check if user is logged in. */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Check if current user is an admin. */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /** Update user profile. */
    public boolean updateProfile(User user) {
        if (com.smartfinance.api.ApiConfig.isClientMode()) {
            return com.smartfinance.api.ApiClient.getInstance().updateProfile(user);
        }
        return userDAO.update(user);
    }
}
