package com.wex.finance.domain.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecimalUtilsTest {

    @Test
    void roundToUsdShouldRoundToTwoDecimals() {
        assertEquals(new BigDecimal("10.13"), DecimalUtils.roundToUSD(new BigDecimal("10.129")));
    }

    @Test
    void roundToRateShouldRoundToSixDecimals() {
        assertEquals(new BigDecimal("5.123457"), DecimalUtils.roundToRate(new BigDecimal("5.1234567")));
    }

    @Test
    void isValidAmountShouldValidateBoundsAndPositivity() {
        assertTrue(DecimalUtils.isValidAmount(new BigDecimal("0.01")));
        assertTrue(DecimalUtils.isValidAmount(new BigDecimal("999999999.99")));
        assertFalse(DecimalUtils.isValidAmount(BigDecimal.ZERO));
        assertFalse(DecimalUtils.isValidAmount(new BigDecimal("1000000000.00")));
    }

    @Test
    void convertCurrencyShouldMultiplyAndRoundResult() {
        var converted = DecimalUtils.convertCurrency(new BigDecimal("10.00"), new BigDecimal("5.123456"));

        assertEquals(new BigDecimal("51.23"), converted);
    }

    @Test
    void convertCurrencyShouldThrowWhenInputsAreNull() {
        assertThrows(IllegalArgumentException.class, () -> DecimalUtils.convertCurrency(null, new BigDecimal("1.0")));
        assertThrows(IllegalArgumentException.class, () -> DecimalUtils.convertCurrency(new BigDecimal("1.0"), null));
    }

    @Test
    void createConverterShouldUseProvidedExchangeRate() {
        var converter = DecimalUtils.createConverter(new BigDecimal("4.200000"));

        assertEquals(new BigDecimal("8.40"), converter.apply(new BigDecimal("2.00")));
    }
}
