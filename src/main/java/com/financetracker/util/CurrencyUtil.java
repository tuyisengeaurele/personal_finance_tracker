package com.financetracker.util;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Utility for formatting monetary values according to the user's preferred currency.
 * The selected currency symbol is persisted via Java {@link Preferences}.
 */
public final class CurrencyUtil {

    private static final String PREF_CURRENCY = "currency_code";
    private static final String DEFAULT_CODE  = "USD";

    private static final Preferences prefs =
            Preferences.userNodeForPackage(CurrencyUtil.class);

    private CurrencyUtil() {}

    /** Returns the currently selected currency code (e.g. "USD"). */
    public static String getCurrencyCode() {
        return prefs.get(PREF_CURRENCY, DEFAULT_CODE);
    }

    /** Persists a new currency code. */
    public static void setCurrencyCode(String code) {
        prefs.put(PREF_CURRENCY, code);
    }

    /**
     * Formats a value as currency string using the saved locale.
     * e.g. {@code format(1234.5)} → "$ 1,234.50"
     */
    public static String format(double amount) {
        try {
            Currency currency = Currency.getInstance(getCurrencyCode());
            NumberFormat fmt  = NumberFormat.getCurrencyInstance(Locale.US);
            fmt.setCurrency(currency);
            return fmt.format(amount);
        } catch (Exception e) {
            return String.format("%.2f", amount);
        }
    }

    /** Returns only the currency symbol (e.g. "$"). */
    public static String getSymbol() {
        try {
            return Currency.getInstance(getCurrencyCode()).getSymbol(Locale.US);
        } catch (Exception e) {
            return "$";
        }
    }
}
