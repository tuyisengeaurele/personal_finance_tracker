package com.financetracker.util;

/**
 * Centralised input validation helpers used across the service layer.
 * All methods throw standard runtime exceptions so callers don't need
 * to declare checked exceptions for validation failures.
 */
public final class ValidationUtil {

    private ValidationUtil() {}

    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null.");
        }
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }

    public static void requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }

    public static void requireMaxLength(String value, int max, String fieldName) {
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(
                    fieldName + " must not exceed " + max + " characters.");
        }
    }

    /** Returns true if the string is a valid positive number. */
    public static boolean isValidAmount(String text) {
        if (text == null || text.isBlank()) return false;
        try {
            double v = Double.parseDouble(text.strip());
            return v > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
