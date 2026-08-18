package com.smartfinance.util;

import java.util.regex.Pattern;

/**
 * Input Validation Utilities.
 * Demonstrates: String Handling, Input Validation, Regex.
 */
public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** Validate email format. */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /** Validate amount (positive number). */
    public static boolean isValidAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) return false;
        try {
            double amount = Double.parseDouble(amountStr.trim());
            return amount > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Validate name (non-empty, letters and spaces only). */
    public static boolean isValidName(String name) {
        if (name == null || name.isBlank()) return false;
        return name.trim().length() >= 2 && name.trim().matches("[a-zA-Z\\s]+");
    }

    /** Validate password (minimum 4 characters). */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 4;
    }

    /** Validate rate (0-100). */
    public static boolean isValidRate(String rateStr) {
        if (rateStr == null || rateStr.isBlank()) return false;
        try {
            double rate = Double.parseDouble(rateStr.trim());
            return rate >= 0 && rate <= 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Validate year (1-50). */
    public static boolean isValidYears(String yearsStr) {
        if (yearsStr == null || yearsStr.isBlank()) return false;
        try {
            int years = Integer.parseInt(yearsStr.trim());
            return years >= 1 && years <= 50;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Format amount in Indian Rupees. */
    public static String formatCurrency(double amount) {
        if (amount < 0) {
            return "-\u20B9" + String.format("%,.2f", Math.abs(amount));
        }
        return "\u20B9" + String.format("%,.2f", amount);
    }

    /** Format amount without decimals. */
    public static String formatCurrencyShort(double amount) {
        if (amount >= 10000000) { // 1 Crore
            return "\u20B9" + String.format("%.1f Cr", amount / 10000000);
        } else if (amount >= 100000) { // 1 Lakh
            return "\u20B9" + String.format("%.1f L", amount / 100000);
        } else if (amount >= 1000) {
            return "\u20B9" + String.format("%.1f K", amount / 1000);
        }
        return "\u20B9" + String.format("%,.0f", amount);
    }

    /** Parse a double from string safely. */
    public static double parseDouble(String str) {
        try {
            return Double.parseDouble(str.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /** Parse an int from string safely. */
    public static int parseInt(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
