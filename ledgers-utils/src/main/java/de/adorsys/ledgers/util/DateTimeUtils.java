package de.adorsys.ledgers.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateTimeUtils {
    public static LocalDateTime getTimeAtEndOfTheDay(LocalDate date) {
        return date.atTime(23, 59, 59, 99);
    }
}
