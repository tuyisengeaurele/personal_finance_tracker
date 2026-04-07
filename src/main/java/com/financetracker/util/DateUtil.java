package com.financetracker.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Convenience methods for date formatting and common date ranges.
 */
public final class DateUtil {

    public static final DateTimeFormatter ISO_DATE   = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter MONTH_FMT  = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    private DateUtil() {}

    /** Returns the current month as "YYYY-MM". */
    public static String currentMonthKey() {
        return YearMonth.now().format(MONTH_FMT);
    }

    /** Returns the first day of the current month. */
    public static LocalDate firstDayOfCurrentMonth() {
        return YearMonth.now().atDay(1);
    }

    /** Returns the last day of the current month. */
    public static LocalDate lastDayOfCurrentMonth() {
        return YearMonth.now().atEndOfMonth();
    }

    /** Formats a date for display in the UI (e.g. "Apr 7, 2026"). */
    public static String formatDisplay(LocalDate date) {
        return date != null ? date.format(DISPLAY_FMT) : "";
    }

    /** Parses an ISO date string safely, returning null on failure. */
    public static LocalDate parseOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value, ISO_DATE);
        } catch (Exception e) {
            return null;
        }
    }
}
