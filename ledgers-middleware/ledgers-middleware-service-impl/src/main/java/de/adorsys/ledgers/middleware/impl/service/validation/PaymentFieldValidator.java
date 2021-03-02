package de.adorsys.ledgers.middleware.impl.service.validation;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTargetBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.deposit.api.domain.ExecutionRules.FOLLOWING;
import static de.adorsys.ledgers.deposit.api.domain.ExecutionRules.PRECEDING;

@UtilityClass
public class PaymentFieldValidator {

    public static boolean isValidAmount(PaymentBO payment) {
        return payment.getTargets().stream()
                       .map(PaymentTargetBO::getInstructedAmount)
                       .allMatch(a -> a.getAmount().compareTo(BigDecimal.ZERO) > 0
                                              && a.getAmount().scale() < 3);
    }

    public static boolean isInvalidExecutionRule(PaymentBO payment) {
        if (payment.getExecutionRule() == null || payment.getExecutionRule().isBlank()) {
            return false;
        }
        return !payment.getExecutionRule().equals(PRECEDING) && !payment.getExecutionRule().equals(FOLLOWING);
    }

    public static boolean isInvalidEndToEndIds(PaymentBO payment, boolean allowSameIds) {
        return !allowSameIds && payment.getTargets().stream()
                                        .map(PaymentTargetBO::getEndToEndIdentification)
                                        .collect(Collectors.toSet()).size() != payment.getTargets().size();
    }

    public static boolean isInvalidRequestedExecutionDateTime(PaymentBO payment, boolean allowDatesInThePast) {
        if (allowDatesInThePast) {
            return false;
        }
        boolean datePresent = payment.getRequestedExecutionDate() != null;
        return datePresent && payment.getRequestedExecutionDate().isBefore(LocalDate.now()) ||
                       payment.getRequestedExecutionTime() != null
                               && LocalDateTime.of(datePresent ? payment.getRequestedExecutionDate() : LocalDate.now(),
                                                   payment.getRequestedExecutionTime()).isBefore(LocalDateTime.now());
    }

    public static boolean isInvalidStartDate(PaymentBO payment, boolean allowDatesInThePast) {
        boolean presentStartDate = payment.getStartDate() != null;
        if (allowDatesInThePast) {
            return !presentStartDate;
        }

        return presentStartDate && payment.getStartDate().isBefore(LocalDate.now());
    }

    public static boolean isInvalidEndDate(PaymentBO payment) {
        return payment.getEndDate() != null && payment.getEndDate().isBefore(payment.getStartDate());
    }

    public static boolean isInvalidStartingTransactionStatus(PaymentBO payment) {
        return payment.getTransactionStatus() != null && payment.getTransactionStatus() != TransactionStatusBO.RCVD;
    }

    public static boolean isInvalidExecutionDay(PaymentBO payment) {
        return payment.getDayOfExecution() != null && payment.getDayOfExecution() > 31
                       || payment.getDayOfExecution() != null && payment.getDayOfExecution() < 1;
    }
}
