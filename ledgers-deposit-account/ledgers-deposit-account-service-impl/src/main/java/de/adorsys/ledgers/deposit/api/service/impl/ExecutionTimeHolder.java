/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.db.domain.FrequencyCode;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.function.Function;

@UtilityClass
@SuppressWarnings("PMD.FinalFieldCouldBeStatic")
public class ExecutionTimeHolder {
    private final EnumMap<FrequencyCode, Function<Payment, LocalDate>> holder = new EnumMap<>(FrequencyCode.class);
    private final int ONE = 1;
    private final int TWO = 2;
    private final int THREE = 3;
    private final int SIX = 6;
    private final int TWELVE = 12;

    static {
        holder.put(FrequencyCode.DAILY, p -> p.getExecutedDate().plusDays(ONE).toLocalDate());
        holder.put(FrequencyCode.WEEKLY, p -> getDateForWeeks(p, ONE));
        holder.put(FrequencyCode.EVERYTWOWEEKS, p -> getDateForWeeks(p, TWO));
        holder.put(FrequencyCode.MONTHLY, p -> getDateForMonths(p, ONE));
        holder.put(FrequencyCode.EVERYTWOMONTHS, p -> getDateForMonths(p, TWO));
        holder.put(FrequencyCode.QUARTERLY, p -> getDateForMonths(p, THREE));
        holder.put(FrequencyCode.SEMIANNUAL, p -> getDateForMonths(p, SIX));
        holder.put(FrequencyCode.ANNUAL, p -> getDateForMonths(p, TWELVE));
    }

    public LocalDate getExecutionDate(Payment payment) {
        LocalDate nextExecution = holder.get(payment.getFrequency()).apply(payment);
        return payment.getDayOfExecution() == null ||
                       EnumSet.of(FrequencyCode.DAILY, FrequencyCode.WEEKLY, FrequencyCode.EVERYTWOWEEKS).contains(payment.getFrequency())
                       ? nextExecution
                       : LocalDate.of(nextExecution.getYear(), nextExecution.getMonth(), payment.getDayOfExecution());
    }

    private LocalDate getDateForWeeks(Payment payment, int weeksToAdd) {
        return payment.getExecutedDate().toLocalDate().plusWeeks(weeksToAdd);
    }

    private LocalDate getDateForMonths(Payment payment, int monthsToAdd) {
        return payment.getStartDate().plusMonths(getMonthDifference(payment) + monthsToAdd);
    }

    private long getMonthDifference(Payment payment) {
        return ChronoUnit.MONTHS.between(payment.getStartDate(), payment.getExecutedDate().toLocalDate());
    }
}
