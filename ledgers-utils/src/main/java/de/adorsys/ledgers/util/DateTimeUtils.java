/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateTimeUtils {
    private DateTimeUtils() {
    }

    public static LocalDateTime getTimeAtEndOfTheDay(LocalDate date) {
        return date.atTime(23, 59, 59, 99);
    }
}
