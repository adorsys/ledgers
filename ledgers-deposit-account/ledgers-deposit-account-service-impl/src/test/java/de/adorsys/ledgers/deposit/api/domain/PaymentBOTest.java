package de.adorsys.ledgers.deposit.api.domain;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PaymentBOTest {
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void isValidAmount() {
        PaymentBO given = getPayment(true, true);
        boolean valid = given.isValidAmount();
        assertTrue(valid);
    }

    @Test
    void isValidAmount_negative() {
        PaymentBO given = getPayment(false, true);
        boolean valid = given.isValidAmount();
        assertFalse(valid);
    }

    @Test
    void isValidAmount_overScaled() {
        PaymentBO given = getPayment(true, false);
        boolean valid = given.isValidAmount();
        assertFalse(valid);
    }

    private PaymentBO getPayment(boolean isPositiveAmount, boolean scaleLessThanThree) {
        PaymentTargetBO target1 = new PaymentTargetBO();
        PaymentTargetBO target2 = new PaymentTargetBO();
        target1.setInstructedAmount(new AmountBO(EUR, BigDecimal.valueOf(scaleLessThanThree ? 1.01 : 1.001)));
        target2.setInstructedAmount(new AmountBO(EUR, BigDecimal.valueOf(isPositiveAmount ? 1 : 0)));
        PaymentBO payment = new PaymentBO();
        payment.setTargets(Arrays.asList(target1, target2));
        return payment;
    }
}
