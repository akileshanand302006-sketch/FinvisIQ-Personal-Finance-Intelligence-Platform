package com.smartfinance.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password Security Utility.
 * Provides SHA-256 hashing with salt for secure credential storage.
 */
public class PasswordUtil {
    private static final int SALT_LENGTH = 16;

    /** Generate a random salt string. */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Hash password with provided salt. Format: salt:hash */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            String hashStr = Base64.getEncoder().encodeToString(hashedPassword);
            return salt + ":" + hashStr;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /** Hash password generating a fresh salt. */
    public static String hashPassword(String password) {
        String salt = generateSalt();
        return hashPassword(password, salt);
    }

    /** Verify plaintext password against stored salted hash (salt:hash format). */
    public static boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || password == null) return false;

        // If stored password isn't salted (legacy format salt:hash missing)
        if (!storedHash.contains(":")) {
            return password.equals(storedHash);
        }

        String[] parts = storedHash.split(":", 2);
        if (parts.length != 2) return false;

        String salt = parts[0];
        String expectedHash = hashPassword(password, salt);
        return expectedHash.equals(storedHash);
    }

    /** Check password strength score (0 to 100). */
    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;
        int score = 0;

        if (password.length() >= 4) score += 20;
        if (password.length() >= 8) score += 20;
        if (password.matches(".*[a-z].*")) score += 15;
        if (password.matches(".*[A-Z].*")) score += 15;
        if (password.matches(".*[0-9].*")) score += 15;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score += 15;

        return Math.min(100, score);
    }
}
