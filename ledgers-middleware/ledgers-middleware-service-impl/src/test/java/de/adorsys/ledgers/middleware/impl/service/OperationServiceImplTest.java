/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.AccountReferenceBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisAccountAccessInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.impl.config.PaymentValidatorService;
import de.adorsys.ledgers.middleware.impl.converter.AisConsentBOMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.middleware.impl.service.message.PsuMessageResolver;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationServiceImplTest {
    @InjectMocks
    OperationServiceImpl service;

    @Mock
    private DepositAccountPaymentService paymentService;
    @Mock
    private PaymentConverter paymentConverter;
    @Mock
    private SCAUtils scaUtils;
    @Mock
    private AccessService accessService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserService userService;
    @Mock
    private AisConsentBOMapper aisConsentMapper;
    @Mock
    private SCAOperationService scaOperationService;
    @Mock
    private PaymentValidatorService validatorChain;
    @Mock
    private PsuMessageResolver messageResolver;

    @Test
    void resolveInitiation_pmt() {
        when(scaUtils.userBO(any())).thenReturn(getUserBO());
        when(paymentConverter.toPaymentBO(any())).thenReturn(getPmtBO());
        when(accessService.exchangeTokenStartSca(anyBoolean(), any())).thenReturn(new BearerTokenTO());
        when(paymentService.initiatePayment(any(), any())).thenReturn(getPmtBO());

        GlobalScaResponseTO result = service.resolveInitiation(OpTypeTO.PAYMENT, null, new PaymentTO(), getScaInfo());

        verify(scaUtils, times(1)).userBO(any());
        verify(accessService, times(1)).exchangeTokenStartSca(anyBoolean(), any());
        verify(paymentService, times(1)).initiatePayment(any(), any());

        assertEquals("id", result.getOperationObjectId());
        assertEquals(ScaStatusTO.PSUAUTHENTICATED, result.getScaStatus());
        assertEquals(0, result.getScaMethods().size());
        assertFalse(result.isMultilevelScaRequired());
        assertEquals(TransactionStatusTO.ACSP, result.getTransactionStatus());
        assertNotNull(result.getBearerToken());
    }

    @Test
    void resolveInitiation_canc() {
        when(scaUtils.userBO(any())).thenReturn(getUserBO());
        when(paymentService.getPaymentById(any())).thenReturn(getPmtBO());
        when(accessService.exchangeTokenStartSca(anyBoolean(), any())).thenReturn(new BearerTokenTO());

        GlobalScaResponseTO result = service.resolveInitiation(OpTypeTO.CANCEL_PAYMENT, "id", null, getScaInfo());

        verify(scaUtils, times(1)).userBO(any());
        verify(paymentService, times(1)).getPaymentById(any());
        verify(accessService, times(1)).exchangeTokenStartSca(anyBoolean(), any());

        assertEquals("id", result.getOperationObjectId());
        assertEquals(ScaStatusTO.PSUAUTHENTICATED, result.getScaStatus());
        assertEquals(0, result.getScaMethods().size());
        assertFalse(result.isMultilevelScaRequired());
        assertEquals(TransactionStatusTO.ACSP, result.getTransactionStatus());
        assertNotNull(result.getBearerToken());
    }

    @Test
    void resolveInitiation_cons() {
        when(scaUtils.userBO(any())).thenReturn(getUserBO());
        when(aisConsentMapper.toAisConsentBO(any())).thenReturn(getConsentBO());
        when(userService.storeConsent(any())).thenReturn(getConsentBO());
        when(accessService.exchangeTokenStartSca(anyBoolean(), any())).thenReturn(new BearerTokenTO());

        GlobalScaResponseTO result = service.resolveInitiation(OpTypeTO.CONSENT, null, getConsent(), getScaInfo());

        verify(scaUtils, times(1)).userBO(any());
        verify(userService, times(1)).storeConsent(any());
        verify(accessService, times(1)).exchangeTokenStartSca(anyBoolean(), any());

        assertEquals("id", result.getOperationObjectId());
        assertEquals(ScaStatusTO.PSUAUTHENTICATED, result.getScaStatus());
        assertEquals(0, result.getScaMethods().size());
        assertFalse(result.isMultilevelScaRequired());
        assertNull(result.getTransactionStatus());
        assertNotNull(result.getBearerToken());
    }

    @Test
    void execute_pmt() {
        when(paymentService.getPaymentById(any())).thenReturn(getPmtBO());
        when(scaOperationService.authenticationCompleted(any(), any())).thenReturn(true);
        when(scaUtils.userBO(any())).thenReturn(getUserBO());
        when(paymentService.executePayment(any(), any())).thenReturn(TransactionStatusBO.ACCC);
        GlobalScaResponseTO result = service.execute(OpTypeTO.PAYMENT, "id", getScaInfo());

        verify(scaOperationService, times(1)).authenticationCompleted(any(), any());
        verify(paymentService, times(1)).updatePaymentStatus(any(), any());
        verify(paymentService, times(1)).executePayment(any(), any());

        assertEquals("id", result.getOperationObjectId());
        assertEquals(ScaStatusTO.FINALISED, result.getScaStatus());
        assertEquals(0, result.getScaMethods().size());
        assertFalse(result.isMultilevelScaRequired());
        assertEquals(TransactionStatusTO.ACCC, result.getTransactionStatus());
        assertNull(result.getBearerToken());
    }

    @Test
    void execute_pmt_patc() {
        when(paymentService.getPaymentById(any())).thenReturn(getPmtBO());
        when(scaOperationService.authenticationCompleted(any(), any())).thenReturn(false);
        when(paymentService.updatePaymentStatus(any(), any())).thenReturn(TransactionStatusBO.PATC);
        when(scaUtils.userBO(any())).thenReturn(getUserBO());
        ReflectionTestUtils.setField(service, "multilevelScaEnable", true);
        GlobalScaResponseTO result = service.execute(OpTypeTO.PAYMENT, "id", getScaInfo());

        verify(scaOperationService, times(1)).authenticationCompleted(any(), any());
        verify(paymentService, times(1)).updatePaymentStatus(any(), any());
        verify(paymentService, times(0)).executePayment(any(), any());

        assertEquals("id", result.getOperationObjectId());
        assertEquals(ScaStatusTO.FINALISED, result.getScaStatus());
        assertEquals(0, result.getScaMethods().size());
        assertTrue(result.isMultilevelScaRequired());
        assertEquals(TransactionStatusTO.PATC, result.getTransactionStatus());
        assertNull(result.getBearerToken());
    }

    @Test
    void execute_canc() {
        when(paymentService.getPaymentById(any())).thenReturn(getPmtBO());
        when(scaOperationService.authenticationCompleted(any(), any())).thenReturn(true);
        when(scaUtils.userBO(any())).thenReturn(getUserBO());
        when(paymentService.cancelPayment(any())).thenReturn(TransactionStatusBO.CANC);
        GlobalScaResponseTO result = service.execute(OpTypeTO.CANCEL_PAYMENT, "id", getScaInfo());

        verify(scaOperationService, times(1)).authenticationCompleted(any(), any());
        verify(paymentService, times(0)).executePayment(any(), any());

        assertEquals("id", result.getOperationObjectId());
        assertEquals(ScaStatusTO.FINALISED, result.getScaStatus());
        assertEquals(0, result.getScaMethods().size());
        assertFalse(result.isMultilevelScaRequired());
        assertEquals(TransactionStatusTO.CANC, result.getTransactionStatus());
        assertNull(result.getBearerToken());
    }

    @Test
    void execute_fail() {
        ScaInfoTO scaInfo = getScaInfo();
        MiddlewareModuleException exception = assertThrows(MiddlewareModuleException.class, () -> service.execute(OpTypeTO.CONSENT, "id", scaInfo));
        assertEquals(MiddlewareErrorCode.UNSUPPORTED_OPERATION, exception.getErrorCode());
    }

    private AisConsentTO getConsent() {
        AisConsentTO consent = new AisConsentTO();
        consent.setId("id");
        consent.setAccess(new AisAccountAccessInfoTO(List.of("IBAN"), Collections.emptyList(), Collections.emptyList(), null, null));
        return consent;
    }

    private AisConsentBO getConsentBO() {
        AisConsentBO consent = new AisConsentBO();
        consent.setId("id");
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        access.setAccounts(List.of("IBAN"));
        consent.setAccess(access);
        return consent;
    }

    private DepositAccountBO getAccountBO() {
        DepositAccountBO bo = new DepositAccountBO();
        bo.setCurrency(Currency.getInstance("EUR"));
        return bo;
    }

    private UserBO getUserBO() {
        UserBO bo = new UserBO();
        bo.setScaUserData(List.of(new ScaUserDataBO()));
        bo.setAccountAccesses(List.of(new AccountAccessBO("IBAN", null, "accId", 100, AccessTypeBO.OWNER)));
        return bo;
    }

    private ScaInfoTO getScaInfo() {
        return new ScaInfoTO();
    }

    private PaymentBO getPmtBO() {
        PaymentBO payment = new PaymentBO();
        payment.setDebtorAccount(new AccountReferenceBO());
        payment.setPaymentId("id");
        payment.setTransactionStatus(TransactionStatusBO.ACSP);
        return payment;
    }
}
