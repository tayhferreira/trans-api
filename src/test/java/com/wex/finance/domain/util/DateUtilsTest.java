package com.wex.finance.domain.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateUtilsTest {

    @Test
    void getQuarterShouldMapMonthToExpectedQuarter() {
        assertEquals(1, DateUtils.getQuarter(LocalDateTime.of(2026, 1, 1, 0, 0)));
        assertEquals(4, DateUtils.getQuarter(LocalDateTime.of(2026, 12, 1, 0, 0)));
    }

    @Test
    void isValidDateFormatShouldAcceptIsoDateAndIsoDateTime() {
        assertTrue(DateUtils.isValidDateFormat("2026-05-10"));
        assertTrue(DateUtils.isValidDateFormat("2026-05-10T12:30:45"));
    }

    @Test
    void isValidDateFormatShouldRejectInvalidValues() {
        assertFalse(DateUtils.isValidDateFormat(null));
        assertFalse(DateUtils.isValidDateFormat(""));
        assertFalse(DateUtils.isValidDateFormat("10/05/2026"));
    }

    @Test
    void parseDateShouldParseDateOnlyAsStartOfDay() {
        var parsed = DateUtils.parseDate("2026-05-10");

        assertEquals(LocalDateTime.of(2026, 5, 10, 0, 0), parsed);
    }

    @Test
    void parseDateShouldParseIsoDateTimeAsProvided() {
        var parsed = DateUtils.parseDate("2026-05-10T12:30:45");

        assertEquals(LocalDateTime.of(2026, 5, 10, 12, 30, 45), parsed);
    }

    @Test
    void parseDateShouldThrowForBlankValues() {
        assertThrows(DateTimeParseException.class, () -> DateUtils.parseDate(" "));
    }
}
