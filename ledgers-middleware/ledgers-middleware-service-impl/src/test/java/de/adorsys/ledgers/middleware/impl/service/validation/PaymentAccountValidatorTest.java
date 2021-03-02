package de.adorsys.ledgers.middleware.impl.service.validation;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentAccountValidatorTest {
    private static final String ID = "ID";
    private static final String IBAN = "IBAN";
    private static final String IBAN_OTHER = "IBAN_OTHER";
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final String NAME = "anton.brueckner";

    @InjectMocks
    private PaymentAccountValidator service;
    @Mock
    private DepositAccountService accountService;

    @Test
    void check_success() {
        when(accountService.getAccountByIbanAndCurrency(eq(IBAN), any())).thenReturn(getDepAcc(false));
        PaymentBO testPmt = getPayment(true, true, true, true, true);
        assertDoesNotThrow(() -> service.check(testPmt, getUser()));
        assertEquals(ID, testPmt.getAccountId());
        assertEquals(NAME, testPmt.getDebtorName());
        assertEquals(EUR, testPmt.getDebtorAccount().getCurrency());
        assertEquals(IBAN, testPmt.getTargets().get(0).getCreditorAccount().getIban());
        assertEquals(EUR, testPmt.getTargets().get(0).getCreditorAccount().getCurrency());
    }

    @Test
    void check_error_debtor_acc_absent() {
        when(accountService.getAccountByIbanAndCurrency(eq(IBAN), any())).thenReturn(getDepAcc(false));
        when(accountService.getAccountByIbanAndCurrency(eq(IBAN_OTHER), any())).thenThrow(DepositModuleException.class);
        PaymentBO testPmt = getPayment(false, true, true, true, true);
        UserBO user = getUser();
        assertThrows(DepositModuleException.class, () -> service.check(testPmt, user));
    }

    @Test
    void check_success_debt_name_n_currency_absent() {
        when(accountService.getAccountsByIbanAndParamCurrency(eq(IBAN), any())).thenReturn(List.of(getDepAcc(false)));
        PaymentBO testPmt = getPayment(true, false, false, true, true);
        assertDoesNotThrow(() -> service.check(testPmt, getUser()));
        assertEquals(ID, testPmt.getAccountId());
        assertEquals(NAME, testPmt.getDebtorName());
        assertEquals(EUR, testPmt.getDebtorAccount().getCurrency());
        assertEquals(IBAN, testPmt.getTargets().get(0).getCreditorAccount().getIban());
        assertEquals(EUR, testPmt.getTargets().get(0).getCreditorAccount().getCurrency());
    }

    @Test
    void check_success_cred_acc_not_in_ledger_curr_not_set() {
        when(accountService.getAccountByIbanAndCurrency(eq(IBAN), any())).thenReturn(getDepAcc(false));
        when(accountService.getAccountsByIbanAndParamCurrency(eq(IBAN_OTHER), any())).thenReturn(List.of(getDepAcc(false)));
        PaymentBO testPmt = getPayment(true, true, true, false, false);
        assertDoesNotThrow(() -> service.check(testPmt, getUser()));
        assertEquals(ID, testPmt.getAccountId());
        assertEquals(NAME, testPmt.getDebtorName());
        assertEquals(EUR, testPmt.getDebtorAccount().getCurrency());
        assertEquals(IBAN, testPmt.getTargets().get(0).getCreditorAccount().getIban());
        assertEquals(EUR, testPmt.getTargets().get(0).getCreditorAccount().getCurrency());
    }

    @Test
    void check_success_multicurrency_acc() {
        when(accountService.getAccountsByIbanAndParamCurrency(eq(IBAN_OTHER), any())).thenReturn(List.of(getDepAcc(false), getDepAcc(false)));
        PaymentBO testPmt = getPayment(true, true, true, false, false);
        UserBO user = getUser();
        assertThrows(MiddlewareModuleException.class, () -> service.check(testPmt, user));
    }

    @Test
    void check_success_blocked_account() {
        when(accountService.getAccountsByIbanAndParamCurrency(eq(IBAN_OTHER), any())).thenReturn(List.of(getDepAcc(true)));
        PaymentBO testPmt = getPayment(true, true, true, false, false);
        UserBO user = getUser();
        assertThrows(MiddlewareModuleException.class, () -> service.check(testPmt, user));
    }

    private UserBO getUser() {
        UserBO bo = new UserBO();
        bo.setLogin(NAME);
        return bo;
    }

    private DepositAccountBO getDepAcc(boolean blocked) {
        return DepositAccountBO.builder()
                       .iban(IBAN)
                       .currency(EUR)
                       .blocked(blocked)
                       .id(ID)
                       .build();
    }

    private PaymentBO getPayment(boolean dbtAccInLedger, boolean dbtCurrSet, boolean dbtName, boolean crdInLedger, boolean crdCurSet) {
        PaymentBO bo = new PaymentBO();
        bo.setDebtorAccount(getAccount(dbtAccInLedger, dbtCurrSet));
        bo.setDebtorName(dbtName ? NAME : null);
        bo.setTargets(List.of(getTarget(crdInLedger, crdCurSet)));
        return bo;
    }

    private PaymentTargetBO getTarget(boolean accountInLedger, boolean currencySet) {
        PaymentTargetBO trg = new PaymentTargetBO();
        trg.setCreditorAccount(getAccount(accountInLedger, currencySet));
        trg.setInstructedAmount(new AmountBO(EUR, BigDecimal.ONE));
        return trg;
    }

    private AccountReferenceBO getAccount(boolean isInLedger, boolean currencySet) {
        AccountReferenceBO ref = new AccountReferenceBO();
        ref.setIban(isInLedger ? IBAN : IBAN_OTHER);
        ref.setCurrency(currencySet ? EUR : null);
        return ref;
    }
}