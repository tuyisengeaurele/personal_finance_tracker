package com.financetracker.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void requireNonNull_nullValue_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.requireNonNull(null, "field"));
    }

    @Test
    void requireNonNull_nonNullValue_shouldNotThrow() {
        assertDoesNotThrow(() -> ValidationUtil.requireNonNull("value", "field"));
    }

    @Test
    void requireNonBlank_blankValue_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.requireNonBlank("", "field"));
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.requireNonBlank("   ", "field"));
    }

    @Test
    void requirePositive_zeroOrNegative_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.requirePositive(0, "amount"));
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.requirePositive(-5, "amount"));
    }

    @Test
    void requirePositive_positiveValue_shouldNotThrow() {
        assertDoesNotThrow(() -> ValidationUtil.requirePositive(0.01, "amount"));
    }

    @Test
    void requireMaxLength_tooLong_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.requireMaxLength("A".repeat(51), 50, "field"));
    }

    @Test
    void isValidAmount_validStrings_shouldReturnTrue() {
        assertTrue(ValidationUtil.isValidAmount("100"));
        assertTrue(ValidationUtil.isValidAmount("0.01"));
        assertTrue(ValidationUtil.isValidAmount("9999.99"));
    }

    @Test
    void isValidAmount_invalidStrings_shouldReturnFalse() {
        assertFalse(ValidationUtil.isValidAmount(""));
        assertFalse(ValidationUtil.isValidAmount("abc"));
        assertFalse(ValidationUtil.isValidAmount("0"));
        assertFalse(ValidationUtil.isValidAmount("-1"));
        assertFalse(ValidationUtil.isValidAmount(null));
    }
}
