package com.financetracker.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Precision-safe arithmetic helpers for financial calculations.
 * Avoids floating-point rounding surprises when displaying totals.
 */
public final class NumberUtil {

    private NumberUtil() {}

    /**
     * Rounds a double to 2 decimal places (standard currency precision).
     */
    public static double round2(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Safely parses a string to double, returning {@code defaultValue} on failure.
     */
    public static double parseOrDefault(String text, double defaultValue) {
        if (text == null || text.isBlank()) return defaultValue;
        try {
            return Double.parseDouble(text.strip());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Returns {@code true} if {@code value} is a finite, positive number.
     */
    public static boolean isPositiveFinite(double value) {
        return Double.isFinite(value) && value > 0;
    }

    /**
     * Clamps a percentage value to the range [0, 100].
     */
    public static double clampPercent(double pct) {
        return Math.max(0.0, Math.min(100.0, pct));
    }
}
