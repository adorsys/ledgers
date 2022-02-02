package de.adorsys.ledgers.middleware.impl.service.validation;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.middleware.impl.config.PaymentProductsConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PaymentSyntacticalValidatorTest {
    private static final String ID = "ID";
    @InjectMocks
    private PaymentSyntacticalValidator service;

    @Mock
    private PaymentProductsConfig paymentProductsConfig;

    @Test
    void check_single() {
        PaymentBO testPmt = getSinglePmt();

        assertDoesNotThrow(() -> service.check(testPmt, null));
    }

    private void test(PaymentBO payment, boolean valid, boolean allowed) {
        assertEquals(!valid, PaymentFieldValidator.isInvalidRequestedExecutionDateTime(payment, allowed));
    }


    @Test
    void check_periodic() {
        PaymentBO testPmt = getPeriodicPmt();

        assertDoesNotThrow(() -> service.check(testPmt, null));
    }

    void tst(PaymentBO payment, boolean allowedPastDates, boolean valid) {
        assertEquals(valid, !PaymentFieldValidator.isInvalidStartDate(payment, allowedPastDates));
    }

    void tst(PaymentBO payment, boolean valid) {
        assertEquals(valid, !PaymentFieldValidator.isInvalidEndDate(payment));
    }

    @Test
    void check_bulk() {
        PaymentBO testPmt = getBulkPmt();

        assertDoesNotThrow(() -> service.check(testPmt, null));
    }

    private PaymentBO getSinglePmt() {
        return new PaymentBO(ID, false, LocalDate.now(), LocalTime.now().plusSeconds(1), PaymentTypeBO.SINGLE,
                             "sepa-credit-transfers", null, null, null, null,
                             null, getRef(), "anton.brueckner", null, TransactionStatusBO.RCVD,
                             List.of(getTrg("1")), null);
    }

    private PaymentBO getPeriodicPmt() {
        return new PaymentBO(ID, false, null, null, PaymentTypeBO.PERIODIC,
                             "sepa-credit-transfers", LocalDate.now(), null, null, FrequencyCodeBO.DAILY,
                             null, getRef(), "anton.brueckner", null, TransactionStatusBO.RCVD,
                             List.of(getTrg("1")), null);
    }

    private PaymentBO getBulkPmt() {
        return new PaymentBO(ID, true, LocalDate.now(), LocalTime.now().plusSeconds(1), PaymentTypeBO.BULK,
                             "sepa-credit-transfers", null, null, null, null,
                             null, getRef(), "anton.brueckner", null, TransactionStatusBO.RCVD,
                             List.of(getTrg("1")), null);
    }

    private AccountReferenceBO getRef() {
        AccountReferenceBO bo = new AccountReferenceBO();
        bo.setIban("IBAN");
        bo.setCurrency(Currency.getInstance("EUR"));
        return bo;
    }

    private PaymentTargetBO getTrg(String endToEndId) {
        return new PaymentTargetBO(null, endToEndId, new AmountBO(Currency.getInstance("EUR"), BigDecimal.ONE), getRef(), null, "anton.brueckner", getAddress(), null, Collections.singletonList("remitance").toString().getBytes(StandardCharsets.UTF_8), null, null, null);
    }

    private AddressBO getAddress() {
        AddressBO bo = new AddressBO();
        bo.setCountry("DE");
        return bo;
    }
}