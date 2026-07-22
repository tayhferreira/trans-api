package com.wex.finance.domain.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

public final class DateUtils {

    private DateUtils() {}

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Function<LocalDateTime, Integer> EXTRACT_MONTH =
            LocalDateTime::getMonthValue;

    private static final Function<Integer, Integer> MONTH_TO_QUARTER =
            m -> (m - 1) / 3 + 1;

    public static int getQuarter(LocalDateTime date) {
        return MONTH_TO_QUARTER.apply(EXTRACT_MONTH.apply(date));
    }

    public static Function<LocalDateTime, Integer> quarterExtractor() {
        return EXTRACT_MONTH.andThen(MONTH_TO_QUARTER);
    }

    public static boolean isValidDateFormat(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            parseDate(value);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            throw new DateTimeParseException("Date must not be blank", value, 0);
        }

        try {
            return LocalDateTime.parse(value, ISO_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            return LocalDate.parse(value, ISO_DATE).atStartOfDay();
        }
    }
}
