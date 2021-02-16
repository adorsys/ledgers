package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.impl.config.PaymentProductsConfig;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PaymentSupportServiceTest {
    private static final Currency EUR = Currency.getInstance("EUR");
    public static final String IBAN = "iban";
    @InjectMocks
    private PaymentSupportService service;

    @Mock
    private PaymentProductsConfig paymentProductsConfig;
    @Mock
    private DepositAccountService accountService;

    @Test
    void validatePayment() {
        assertDoesNotThrow(() -> service.validatePayment(getPaymentBO(BigDecimal.TEN)));
    }

    @Test
    void validatePayment_wrong_amount() {
        PaymentBO paymentBO = getPaymentBO(BigDecimal.ZERO);
        MiddlewareModuleException exception = assertThrows(MiddlewareModuleException.class, () -> service.validatePayment(paymentBO));
        assertEquals(REQUEST_VALIDATION_FAILURE, exception.getErrorCode());
        assertEquals("Payment validation failed! Instructed amount is invalid.", exception.getDevMsg());
    }

    @Test
    void validatePayment_unsupported_product() {
        PaymentBO paymentBO = getPaymentBO(BigDecimal.ZERO);
        when(paymentProductsConfig.isNotSupportedPaymentProduct(any())).thenReturn(true);
        MiddlewareModuleException exception = assertThrows(MiddlewareModuleException.class, () -> service.validatePayment(paymentBO));
        assertEquals(REQUEST_VALIDATION_FAILURE, exception.getErrorCode());
        assertEquals("Payment validation failed! Payment Product not Supported!", exception.getDevMsg());
    }

    private PaymentBO getPaymentBO(BigDecimal amount) {
        PaymentBO bo = new PaymentBO();
        bo.setPaymentType(PaymentTypeBO.SINGLE);
        bo.setPaymentProduct("product");
        PaymentTargetBO targetBO = new PaymentTargetBO();
        targetBO.setInstructedAmount(new AmountBO(EUR, amount));
        bo.setTargets(List.of(targetBO));
        return bo;
    }

    @Test
    void getCheckedAccount() {
        when(accountService.getAccountByIbanAndCurrency(any(), any())).thenReturn(getAccount(false));
        PaymentBO paymentBO = getPaymentAcc(EUR);
        DepositAccountBO result = service.getCheckedAccount(paymentBO);
        assertEquals("id", result.getId());
        assertEquals(IBAN, paymentBO.getTargets().iterator().next().getCreditorAccount().getIban());

        verify(accountService, times(2)).getAccountByIbanAndCurrency(any(), any());
    }

    @Test
    void getCheckedAccount_failed_debtor() {
        when(accountService.getAccountByIbanAndCurrency(any(), any())).thenThrow(DepositModuleException.class);
        PaymentBO paymentBO = getPaymentAcc(EUR);
        assertThrows(DepositModuleException.class, () -> service.getCheckedAccount(paymentBO));

        verify(accountService, times(1)).getAccountByIbanAndCurrency(any(), any());
    }

    @Test
    void getCheckedAccount_debtor_blocked() {
        when(accountService.getAccountByIbanAndCurrency(any(), any())).thenReturn(getAccount(true));
        PaymentBO paymentBO = getPaymentAcc(EUR);
        MiddlewareModuleException exception = assertThrows(MiddlewareModuleException.class, () -> service.getCheckedAccount(paymentBO));
        assertEquals(ACCOUNT_DISABLED, exception.getErrorCode());

        verify(accountService, times(1)).getAccountByIbanAndCurrency(any(), any());
    }

    @Test
    void getCheckedAccount_no_curr_in_debtor() {
        when(accountService.getAccountsByIbanAndParamCurrency(any(), any())).thenReturn(List.of(getAccount(false)));
        PaymentBO paymentBO = getPaymentAcc(null);
        DepositAccountBO result = service.getCheckedAccount(paymentBO);
        assertEquals("id", result.getId());
        assertEquals(IBAN, paymentBO.getTargets().iterator().next().getCreditorAccount().getIban());

        verify(accountService, times(2)).getAccountsByIbanAndParamCurrency(any(), any());
    }

    @Test
    void getCheckedAccount_no_curr_in_debtor_many_found() {
        when(accountService.getAccountsByIbanAndParamCurrency(any(), any())).thenReturn(List.of(getAccount(false), getAccount(false)));
        PaymentBO paymentBO = getPaymentAcc(null);
        MiddlewareModuleException exception = assertThrows(MiddlewareModuleException.class, () -> service.getCheckedAccount(paymentBO));

        assertEquals(CURRENCY_MISMATCH, exception.getErrorCode());

        verify(accountService, times(1)).getAccountsByIbanAndParamCurrency(any(), any());
    }

    @Test
    void getCheckedAccount_no_curr_in_debtor_debtor_nf() {
        when(accountService.getAccountsByIbanAndParamCurrency(any(), any())).thenReturn(Collections.emptyList());
        PaymentBO paymentBO = getPaymentAcc(null);
        MiddlewareModuleException exception = assertThrows(MiddlewareModuleException.class, () -> service.getCheckedAccount(paymentBO));

        assertEquals(PAYMENT_PROCESSING_FAILURE, exception.getErrorCode());

        verify(accountService, times(1)).getAccountsByIbanAndParamCurrency(any(), any());
    }

    @Test
    void getCheckedAccount_no_curr_in_creditor_creditor_mult() {
        when(accountService.getAccountsByIbanAndParamCurrency(eq("iban2"), any())).thenReturn(List.of(getAccount(false)));
        when(accountService.getAccountsByIbanAndParamCurrency(eq(IBAN), any())).thenReturn(List.of(getAccount(false), getAccount(false)));
        PaymentBO paymentBO = getPaymentAcc(null);
        paymentBO.getDebtorAccount().setIban("iban2");
        MiddlewareModuleException exception = assertThrows(MiddlewareModuleException.class, () -> service.getCheckedAccount(paymentBO));

        assertEquals(CURRENCY_MISMATCH, exception.getErrorCode());

        verify(accountService, times(2)).getAccountsByIbanAndParamCurrency(any(), any());
    }

    @Test
    void getCheckedAccount_no_curr_in_creditor_creditor_nf() {
        when(accountService.getAccountsByIbanAndParamCurrency(eq("iban2"), any())).thenReturn(List.of(getAccount(false)));
        when(accountService.getAccountsByIbanAndParamCurrency(eq(IBAN), any())).thenThrow(DepositModuleException.class);
        PaymentBO paymentBO = getPaymentAcc(null);
        paymentBO.getDebtorAccount().setIban("iban2");
        DepositAccountBO result = service.getCheckedAccount(paymentBO);
        assertEquals("id", result.getId());
        assertEquals(IBAN, paymentBO.getTargets().iterator().next().getCreditorAccount().getIban());

        verify(accountService, times(2)).getAccountsByIbanAndParamCurrency(any(), any());
    }

    private DepositAccountBO getAccount(boolean isBlocked) {
        DepositAccountBO bo = new DepositAccountBO();
        bo.setId("id");
        bo.setIban(IBAN);
        bo.setBlocked(isBlocked);
        return bo;
    }

    private PaymentBO getPaymentAcc(Currency currency) {
        PaymentBO bo = new PaymentBO();
        bo.setDebtorAccount(getReference(currency));
        PaymentTargetBO targetBO = new PaymentTargetBO();
        targetBO.setCreditorAccount(getReference(currency));
        targetBO.setInstructedAmount(new AmountBO(currency, BigDecimal.ONE));
        bo.setTargets(List.of(targetBO));
        return bo;
    }

    private AccountReferenceBO getReference(Currency currency) {
        AccountReferenceBO bo = new AccountReferenceBO();
        bo.setIban(IBAN);
        bo.setCurrency(currency);
        return bo;
    }
}