package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.db.domain.FrequencyCode;
import de.adorsys.ledgers.deposit.db.domain.Payment;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ExecutionTimeHolder {
    private static Map<FrequencyCode, Function<Payment, LocalDate>> holder = new HashMap<>();
    private final static int ONE = 1;
    private final static int TWO = 2;
    private final static int THREE = 3;
    private final static int SIX = 6;
    private final static int TWELVE = 12;

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

    public static LocalDate getExecutionDate(Payment payment) {
        LocalDate nextExecution = holder.get(payment.getFrequency()).apply(payment);
        return payment.getDayOfExecution() == null ||
                       EnumSet.of(FrequencyCode.DAILY, FrequencyCode.WEEKLY, FrequencyCode.EVERYTWOWEEKS).contains(payment.getFrequency())
                       ? nextExecution
                       : LocalDate.of(nextExecution.getYear(), nextExecution.getMonth(), payment.getDayOfExecution());
    }

    private static LocalDate getDateForWeeks(Payment payment, int weeksToAdd) {
            return payment.getExecutedDate().toLocalDate().plusWeeks(weeksToAdd);
    }

    private static LocalDate getDateForMonths(Payment payment, int monthsToAdd) {
        return payment.getStartDate().plusMonths(getMonthDifference(payment) + monthsToAdd);
    }

    private static long getMonthDifference(Payment payment) {
        return ChronoUnit.MONTHS.between(payment.getStartDate(), payment.getExecutedDate().toLocalDate());
    }
}
