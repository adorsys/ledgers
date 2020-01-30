package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.CurrencyExchangeRatesService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.*;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import net.objectlab.kit.datecalc.common.HolidayCalendar;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO.SINGLE;
import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.ACSP;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentExecutionServiceTest {
    private static int PMT_ID = 0;
    private static final String IBAN = "DE1234567890";
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final TransactionStatus STATUS_ACSC = TransactionStatus.ACSC;
    private static final TransactionStatus STATUS_RCVD = TransactionStatus.RCVD;
    private static final TransactionStatus STATUS_ACSP = TransactionStatus.ACSP;
    private static final TransactionStatusBO STATUS_BO_ACSC = TransactionStatusBO.ACSC;
    private static final TransactionStatusBO STATUS_BO_ACCC = TransactionStatusBO.ACCC;
    private static final TransactionStatusBO STATUS_BO_ACSP = TransactionStatusBO.ACSP;
    private static final TransactionStatusBO STATUS_BO_RJCT = TransactionStatusBO.RJCT;

    private static final String executionRulePreceding = "preceding";
    private static final String executionRuleFollowing = "following";

    @InjectMocks
    private PaymentExecutionService executionService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private DepositAccountTransactionService txService;
    @Mock
    private HolidayCalendar<LocalDate> calendar;
    @Mock
    private DepositAccountService accountService;
    @Mock
    private CurrencyExchangeRatesService exchangeRatesService;
    @Mock
    private PaymentMapper paymentMapper;

    @Test
    public void executeSinglePayment_status_ACCC() {
        //given
        when(accountService.getAccountDetailsById(anyString(), any(LocalDateTime.class), anyBoolean())).thenReturn(getDepositAccountDetailsBO(EUR));
        when(exchangeRatesService.applyRate(any(), any(), any())).thenReturn(BigDecimal.TEN);
        when(accountService.confirmationOfFunds(any())).thenReturn(true);
        when(accountService.getOptionalAccountByIbanAndCurrency(any(), any())).thenReturn(Optional.of(getDepositAccountBO(EUR)));
        when(paymentRepository.save(any())).thenReturn(getSinglePayment());

        //when
        TransactionStatusBO status = executionService.executePayment(getSinglePayment(), "userName");

        //then
        assertSame(STATUS_BO_ACCC, status);
    }

    @Test
    public void executeSinglePayment_status_ACSC() {
        //given
        when(accountService.getAccountDetailsById(anyString(), any(LocalDateTime.class), anyBoolean())).thenReturn(getDepositAccountDetailsBO(EUR));
        when(exchangeRatesService.applyRate(any(), any(), any())).thenReturn(BigDecimal.TEN);
        when(accountService.confirmationOfFunds(any())).thenReturn(true);
        when(accountService.getOptionalAccountByIbanAndCurrency(any(), any())).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenReturn(getSinglePayment());

        //when
        TransactionStatusBO status = executionService.executePayment(getSinglePayment(), "userName");

        //then
        assertSame(STATUS_BO_ACSC, status);
    }

    @Test
    public void executeSinglePayment_insufficientFunds() {
        //given
        when(accountService.getAccountDetailsById(anyString(), any(LocalDateTime.class), anyBoolean())).thenReturn(getDepositAccountDetailsBO(EUR));
        when(exchangeRatesService.applyRate(any(), any(), any())).thenReturn(BigDecimal.TEN);
        when(accountService.confirmationOfFunds(any())).thenReturn(false);

        //when
        TransactionStatusBO status = executionService.executePayment(getSinglePayment(), "userName");

        //then
        assertSame(STATUS_BO_RJCT, status);
    }

    @Test
    public void executeBulkPayment_status_ACSC() {
        //given
        when(accountService.getAccountDetailsById(anyString(), any(LocalDateTime.class), anyBoolean())).thenReturn(getDepositAccountDetailsBO(EUR));
        when(exchangeRatesService.applyRate(any(), any(), any())).thenReturn(BigDecimal.TEN);
        when(accountService.confirmationOfFunds(any())).thenReturn(true);
        when(accountService.getOptionalAccountByIbanAndCurrency(any(), any())).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenReturn(getBulkPayment());

        //when
        TransactionStatusBO status = executionService.executePayment(getBulkPayment(), "userName");

        //then
        assertSame(STATUS_BO_ACSC, status);
    }

    @Test
    public void executeDailyPeriodicPayment_status_ACSP() {
        //given
        when(accountService.getAccountDetailsById(anyString(), any(LocalDateTime.class), anyBoolean())).thenReturn(getDepositAccountDetailsBO(EUR));
        when(exchangeRatesService.applyRate(any(), any(), any())).thenReturn(BigDecimal.TEN);
        when(accountService.confirmationOfFunds(any())).thenReturn(true);
        when(paymentRepository.save(any())).thenReturn(getDailyPeriodicPaymentChanged(STATUS_ACSP, getDailyPeriodicPayment().getEndDate(), executionRuleFollowing));

        //when
        TransactionStatusBO status = executionService.executePayment(getDailyPeriodicPayment(), "userName");

        //then
        assertSame(STATUS_BO_ACSP, status);
    }

    @Test
    public void executeDailyPeriodicPayment_executionRulePreceding() {
        //given
        when(accountService.getAccountDetailsById(anyString(), any(LocalDateTime.class), anyBoolean())).thenReturn(getDepositAccountDetailsBO(EUR));
        when(exchangeRatesService.applyRate(any(), any(), any())).thenReturn(BigDecimal.TEN);
        when(accountService.confirmationOfFunds(any())).thenReturn(true);
        when(paymentRepository.save(any())).thenReturn(getDailyPeriodicPaymentChanged(STATUS_ACSP, getDailyPeriodicPayment().getEndDate(), executionRulePreceding));

        //when
        TransactionStatusBO status = executionService.executePayment(getDailyPeriodicPaymentChanged(STATUS_RCVD, getDailyPeriodicPayment().getEndDate(), executionRulePreceding), "userName");

        //then
        assertSame(STATUS_BO_ACSP, status);
    }

    @Test
    public void executeMonthlyPeriodicPayment_status_ACSP() {
        //given
        when(accountService.getAccountDetailsById(anyString(), any(LocalDateTime.class), anyBoolean())).thenReturn(getDepositAccountDetailsBO(EUR));
        when(exchangeRatesService.applyRate(any(), any(), any())).thenReturn(BigDecimal.TEN);
        when(accountService.confirmationOfFunds(any())).thenReturn(true);
        when(paymentRepository.save(any())).thenReturn(getMonthlyPeriodicPaymentChanged(STATUS_ACSP, getMonthlyPeriodicPayment().getEndDate()));

        //when
        TransactionStatusBO status = executionService.executePayment(getMonthlyPeriodicPayment(), "userName");

        //then
        assertSame(STATUS_BO_ACSP, status);
    }

    @Test
    public void schedulePayment() {
        //given
        when(paymentRepository.save(any())).thenReturn(getSinglePaymentChanged(STATUS_ACSP, getSinglePayment().getEndDate()));

        //when
        TransactionStatusBO status = executionService.schedulePayment(getSinglePayment());

        //then
        assertSame(STATUS_BO_ACSP, status);
    }

    @Test
    public void schedulePayment_periodicPaymentHasLastExecutedDate() {
        //given
        when(paymentRepository.save(any())).thenReturn(getSinglePaymentChanged(STATUS_ACSC, getSinglePayment().getEndDate()));

        //when
        TransactionStatusBO status = executionService.schedulePayment(getMonthlyPeriodicPaymentChanged(STATUS_RCVD, LocalDate.now().minusDays(5)));

        //then
        assertSame(STATUS_BO_ACSC, status);
    }

    private Payment getSinglePayment() {
        Payment payment = readFile(Payment.class, "PaymentSingle.yml");
        payment.getTargets().forEach(t -> t.setPayment(payment));
        return payment;
    }


    private Payment getSinglePaymentChanged(TransactionStatus status, LocalDate endDate) {
        Payment payment = getSinglePayment();
        payment.setTransactionStatus(status);
        payment.setEndDate(endDate);
        return payment;
    }

    private Payment getBulkPayment() {
        Payment payment = readFile(Payment.class, "PaymentBulk.yml");
        payment.getTargets().forEach(t -> t.setPayment(payment));
        return payment;
    }

    private Payment getDailyPeriodicPayment() {
        Payment payment = readFile(Payment.class, "PaymentPeriodicDaily.yml");
        payment.getTargets().forEach(t -> t.setPayment(payment));
        return payment;
    }

    private Payment getDailyPeriodicPaymentChanged(TransactionStatus status, LocalDate endDate, String executionRule) {
        Payment payment = getDailyPeriodicPayment();
        payment.setTransactionStatus(status);
        payment.setEndDate(endDate);
        payment.setExecutionRule(executionRule);
        return payment;
    }

    private Payment getMonthlyPeriodicPayment() {
        Payment payment = readFile(Payment.class, "PaymentPeriodicMonthly.yml");
        payment.getTargets().forEach(t -> t.setPayment(payment));
        return payment;
    }

    private Payment getMonthlyPeriodicPaymentChanged(TransactionStatus status, LocalDate endDate) {
        Payment payment = getMonthlyPeriodicPayment();
        payment.setTransactionStatus(status);
        payment.setEndDate(endDate);
        return payment;
    }

    private <T> T readFile(Class<T> t, String file) {
        try {
            return YamlReader.getInstance().getObjectFromResource(DepositAccountPaymentServiceImpl.class, file, t);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }

    private PaymentBO getPaymentBO() {
        return new PaymentBO("pmt1", false, null,
                             null, SINGLE, "sepa-credit-transfers", null, null, null, null,
                             null, getReference(EUR), null, null, ACSP, getTargets(EUR, EUR, null), getDepositAccount().getId());
    }

    private List<PaymentTargetBO> getTargets(Currency amount, Currency curr1, Currency curr2) {
        return Stream.of(curr1, curr2)
                       .filter(Objects::nonNull)
                       .map(this::getReference)
                       .map(r -> new PaymentTargetBO(nextTargetId(), "END-TO-END", getAmount(amount), r, null, "name", null, null, null, null, null, null))
                       .collect(Collectors.toList());
    }

    private String nextTargetId() {
        PMT_ID++;
        return "target" + PMT_ID;
    }

    private AmountBO getAmount(Currency currency) {
        return new AmountBO(currency, BigDecimal.TEN);
    }

    private AccountReferenceBO getReference(Currency currency) {
        AccountReferenceBO reference = new AccountReferenceBO();
        reference.setIban(IBAN);
        reference.setCurrency(currency);
        return reference;
    }

    private DepositAccount getDepositAccount() {
        return new DepositAccount("id", "iban", "msisdn", "EUR",
                                  "name", "product", null, AccountType.CASH, AccountStatus.ENABLED, "bic", null,
                                  AccountUsage.PRIV, "details");
    }

    private DepositAccountBO getDepositAccountBO(Currency currency) {
        return new DepositAccountBO("id", "iban", "bban", "pan", "maskedPan", "msisdn", currency, "name", "product", AccountTypeBO.CASH, AccountStatusBO.ENABLED, "bic", null, AccountUsageBO.PRIV, "details");
    }

    private DepositAccountDetailsBO getDepositAccountDetailsBO(Currency currency) {
        return new DepositAccountDetailsBO(getDepositAccountBO(currency), Collections.EMPTY_LIST);
    }
}