package com.financetracker.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberUtilTest {

    @Test
    void round2_shouldRoundHalfUp() {
        assertEquals(1.23, NumberUtil.round2(1.225),  0.001);
        assertEquals(1.24, NumberUtil.round2(1.235),  0.001);
        assertEquals(0.00, NumberUtil.round2(0.0),    0.001);
        assertEquals(100.00, NumberUtil.round2(100.0), 0.001);
    }

    @Test
    void parseOrDefault_validString_shouldReturnParsedValue() {
        assertEquals(42.5, NumberUtil.parseOrDefault("42.5", 0), 0.001);
    }

    @Test
    void parseOrDefault_invalidString_shouldReturnDefault() {
        assertEquals(-1.0, NumberUtil.parseOrDefault("abc", -1.0), 0.001);
        assertEquals(-1.0, NumberUtil.parseOrDefault(null,  -1.0), 0.001);
        assertEquals(-1.0, NumberUtil.parseOrDefault("",    -1.0), 0.001);
    }

    @Test
    void isPositiveFinite_shouldReturnCorrectResult() {
        assertTrue(NumberUtil.isPositiveFinite(1.0));
        assertFalse(NumberUtil.isPositiveFinite(0.0));
        assertFalse(NumberUtil.isPositiveFinite(-1.0));
        assertFalse(NumberUtil.isPositiveFinite(Double.NaN));
        assertFalse(NumberUtil.isPositiveFinite(Double.POSITIVE_INFINITY));
    }

    @Test
    void clampPercent_shouldClampToZeroAndHundred() {
        assertEquals(0.0,   NumberUtil.clampPercent(-10.0), 0.001);
        assertEquals(100.0, NumberUtil.clampPercent(150.0), 0.001);
        assertEquals(50.0,  NumberUtil.clampPercent(50.0),  0.001);
    }
}
