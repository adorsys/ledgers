package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import net.objectlab.kit.datecalc.common.HolidayCalendar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;

@RunWith(MockitoJUnitRunner.class)
public class PaymentExecutionServiceTest {
    private static final String executionRulePreceding = "preceeding";
    @Mock
    PaymentRepository paymentRepository;
    @Mock
    DepositAccountTransactionService txService;
    @Mock
    HolidayCalendar<LocalDate> calendar;
    @InjectMocks
    PaymentExecutionService executionService;

    @Before
    public void setup() {
//        when(paymentRepository.save(any(Payment.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    public void executePayment() { //TODO implement @dmiex
    }

    @Test
    public void schedulePayment() {//TODO implement @dmiex

    }
}