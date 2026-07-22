package com.wex.finance.domain.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import java.util.function.Predicate;

public final class DecimalUtils {

    private static final int USD_SCALE = 2;
    private static final int RATE_SCALE = 6;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999.99");

    private DecimalUtils() {}

    private static final Function<BigDecimal, BigDecimal> ROUND_TO_USD =
            value -> value == null ? BigDecimal.ZERO : value.setScale(USD_SCALE, ROUNDING_MODE);

    private static final Function<BigDecimal, BigDecimal> ROUND_TO_RATE =
            value -> value == null ? BigDecimal.ZERO : value.setScale(RATE_SCALE, ROUNDING_MODE);

    private static final Predicate<BigDecimal> IS_POSITIVE =
            value -> value != null && value.signum() > 0;

    private static final Predicate<BigDecimal> IS_WITHIN_BOUNDS =
            value -> value.compareTo(MAX_AMOUNT) <= 0;

    private static final Predicate<BigDecimal> IS_VALID_AMOUNT =
            IS_POSITIVE.and(IS_WITHIN_BOUNDS);

    public static BigDecimal roundToUSD(BigDecimal value) {
        return ROUND_TO_USD.apply(value);
    }

    public static BigDecimal roundToRate(BigDecimal value) {
        return ROUND_TO_RATE.apply(value);
    }

    public static boolean isPositive(BigDecimal value) {
        return IS_POSITIVE.test(value);
    }

    public static boolean isValidAmount(BigDecimal value) {
        return IS_VALID_AMOUNT.test(value);
    }

    public static BigDecimal convertCurrency(BigDecimal amountUSD, BigDecimal exchangeRate) {
        if (amountUSD == null || exchangeRate == null) {
            throw new IllegalArgumentException("Amount and exchange rate cannot be null");
        }
        return ROUND_TO_USD.apply(amountUSD.multiply(exchangeRate));
    }

    public static Function<BigDecimal, BigDecimal> createConverter(BigDecimal exchangeRate) {
        return amount -> convertCurrency(amount, exchangeRate);
    }
}
