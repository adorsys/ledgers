/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.validation;

import de.adorsys.ledgers.deposit.api.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentFieldValidatorTest {
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void isValidAmount() {
        PaymentBO given = getPayment(true, true);
        boolean valid = PaymentFieldValidator.isValidAmount(given);
        assertTrue(valid);
    }

    @Test
    void isValidAmount_negative() {
        PaymentBO given = getPayment(false, true);
        boolean valid = PaymentFieldValidator.isValidAmount(given);
        assertFalse(valid);
    }

    @Test
    void isValidAmount_overScaled() {
        PaymentBO given = getPayment(true, false);
        boolean valid = PaymentFieldValidator.isValidAmount(given);
        assertFalse(valid);
    }

    private PaymentBO getPayment(boolean isPositiveAmount, boolean scaleLessThanThree) {
        PaymentTargetBO target1 = new PaymentTargetBO();
        PaymentTargetBO target2 = new PaymentTargetBO();
        target1.setInstructedAmount(new AmountBO(EUR, BigDecimal.valueOf(scaleLessThanThree ? 1.01 : 1.001)));
        target2.setInstructedAmount(new AmountBO(EUR, BigDecimal.valueOf(isPositiveAmount ? 1 : 0)));
        target1.setEndToEndIdentification("1");
        target2.setEndToEndIdentification("1");
        PaymentBO payment = new PaymentBO();
        payment.setTargets(Arrays.asList(target1, target2));
        return payment;
    }

    @Test
    void isInvalidExecutionRule() {
        PaymentBO given = getPayment(true, false);
        boolean valid = !PaymentFieldValidator.isInvalidExecutionRule(given);
        assertTrue(valid);

        given.setExecutionRule("some weird stuff");
        valid = !PaymentFieldValidator.isInvalidExecutionRule(given);
        assertFalse(valid);

        given.setExecutionRule(ExecutionRules.PRECEDING);
        valid = !PaymentFieldValidator.isInvalidExecutionRule(given);
        assertTrue(valid);
    }

    @Test
    void isInvalidEndToEndIds() {
        PaymentBO given = getPayment(true, false);
        boolean valid = !PaymentFieldValidator.isInvalidEndToEndIds(given, false);
        assertFalse(valid);

        given.getTargets().get(1).setEndToEndIdentification("2");
        valid = !PaymentFieldValidator.isInvalidEndToEndIds(given, false);
        assertTrue(valid);
    }

    @Test
    void isInvalidRequestedExecutionDateTime() {
        PaymentBO given = getPayment(true, false);
        boolean valid = !PaymentFieldValidator.isInvalidRequestedExecutionDateTime(given, false);
        assertTrue(valid);

        given.setRequestedExecutionDate(LocalDate.now().minusDays(1));
        valid = !PaymentFieldValidator.isInvalidRequestedExecutionDateTime(given, false);
        assertFalse(valid);

        given.setRequestedExecutionDate(LocalDate.now().plusDays(1));
        valid = !PaymentFieldValidator.isInvalidRequestedExecutionDateTime(given, false);
        assertTrue(valid);

        given.setRequestedExecutionDate(LocalDate.now());
        given.setRequestedExecutionTime(LocalTime.MAX);
        valid = !PaymentFieldValidator.isInvalidRequestedExecutionDateTime(given, false);
        assertTrue(valid);

        given.setRequestedExecutionTime(LocalTime.MIN);
        valid = !PaymentFieldValidator.isInvalidRequestedExecutionDateTime(given, false);
        assertFalse(valid);
    }

    @Test
    void isInvalidStartDate() {
        PaymentBO given = getPayment(true, false);
        boolean valid = !PaymentFieldValidator.isInvalidStartDate(given, false);
        assertTrue(valid);

        given.setStartDate(LocalDate.now().minusDays(1));
        valid = !PaymentFieldValidator.isInvalidStartDate(given, false);
        assertFalse(valid);

        given.setStartDate(LocalDate.now().plusDays(1));
        valid = !PaymentFieldValidator.isInvalidStartDate(given, false);
        assertTrue(valid);
    }

    @Test
    void isInvalidEndDate() {
        PaymentBO given = getPayment(true, false);
        given.setStartDate(LocalDate.now());
        boolean valid = !PaymentFieldValidator.isInvalidEndDate(given);
        assertTrue(valid);

        given.setEndDate(LocalDate.now().minusDays(1));
        valid = !PaymentFieldValidator.isInvalidEndDate(given);
        assertFalse(valid);

        given.setEndDate(LocalDate.now().plusDays(1));
        valid = !PaymentFieldValidator.isInvalidEndDate(given);
        assertTrue(valid);
    }

    @Test
    void isInvalidStartingTransactionStatus() {
        PaymentBO given = getPayment(true, false);
        given.setTransactionStatus(TransactionStatusBO.ACCC);
        boolean valid = !PaymentFieldValidator.isInvalidStartingTransactionStatus(given);
        assertFalse(valid);

        given.setTransactionStatus(TransactionStatusBO.RCVD);
        valid = !PaymentFieldValidator.isInvalidStartingTransactionStatus(given);
        assertTrue(valid);
    }

    @Test
    void isInvalidExecutionDay() {
        PaymentBO given = getPayment(true, false);
        given.setDayOfExecution(0);
        boolean valid = !PaymentFieldValidator.isInvalidExecutionDay(given);
        assertFalse(valid);

        given.setDayOfExecution(32);
        valid = !PaymentFieldValidator.isInvalidExecutionDay(given);
        assertFalse(valid);

        given.setDayOfExecution(12);
        valid = !PaymentFieldValidator.isInvalidExecutionDay(given);
        assertTrue(valid);
    }
}