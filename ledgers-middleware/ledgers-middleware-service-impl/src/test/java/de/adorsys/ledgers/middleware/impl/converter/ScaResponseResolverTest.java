/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.ScaChallengeDataResolver;
import de.adorsys.ledgers.middleware.impl.service.SCAUtils;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScaResponseResolverTest {
    private static final String TEMPLATE = "TEMPLATE";
    private static final String PAYMENT_ID = "payment id";
    private static final String AUTH_ID = "auth id";
    private static final String PSU_MSG = "user msg";
    private static final String SCA_METHOD = "MAIL";
//TODO Review this test!
    @InjectMocks
    ScaResponseResolver service;

    @Mock
    private SCAUtils scaUtils;
    @Mock
    private ScaChallengeDataResolver scaChallengeDataResolver;
    @Mock
    private UserMapper userMapper;
    @Mock
    private SCAOperationService scaOperationService;

    @Test
    void completeResponse() {
        // Given
        SCAConsentResponseTO response = getConsentResponse();

        // When
        service.completeResponse(response, getOperation(), new UserTO(), TEMPLATE, new BearerTokenTO());

        // Then
        assertEquals(getConsentResponse(), response);
    }

    @Test
    void resolveScaStatus() {
        // When
        ScaStatusTO result = service.resolveScaStatus(TokenUsageTO.LOGIN, false);

        // Then
        assertEquals(ScaStatusTO.EXEMPTED, result);
    }

    @Test
    void resolveScaStatus_sca_required() {
        // When
        ScaStatusTO result = service.resolveScaStatus(TokenUsageTO.LOGIN, true);

        // Then
        assertEquals(ScaStatusTO.PSUAUTHENTICATED, result);
    }

    @Test
    void resolveScaStatus_sca_required_delegated_token() {
        // When
        ScaStatusTO result = service.resolveScaStatus(TokenUsageTO.DELEGATED_ACCESS, true);

        // Then
        assertEquals(ScaStatusTO.PSUIDENTIFIED, result);
    }

    private SCAPaymentResponseTO getPaymentResponse() {
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        response.setScaStatus(ScaStatusTO.PSUIDENTIFIED);
        return response;
    }

    @Test
    void updateScaResponseFields() {
        // Given
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        service.updateScaResponseFields(new UserBO(), response, AUTH_ID, PSU_MSG, new BearerTokenTO(), ScaStatusTO.PSUIDENTIFIED, 100);
        SCAPaymentResponseTO expected = getPaymentResponse();
        expected.setBearerToken(new BearerTokenTO());
        expected.setPsuMessage(PSU_MSG);
        expected.setScaMethods(Collections.emptyList());
        expected.setAuthorisationId(AUTH_ID);

        // Then
        assertEquals(expected, response);
    }

    @Test
    void updatePaymentRelatedResponseFields() {
        // Given
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();

        // When
        service.updatePaymentRelatedResponseFields(response, getPayment());

        // Then
        assertEquals(getExpectedPaymentResponse(), response);
    }

    private SCAPaymentResponseTO getExpectedPaymentResponse() {
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatusTO.ACCC);
        response.setPaymentType(PaymentTypeTO.SINGLE);
        response.setPaymentProduct("sepa");
        return response;
    }

    private PaymentBO getPayment() {
        PaymentBO payment = new PaymentBO();
        payment.setPaymentId(PAYMENT_ID);
        payment.setTransactionStatus(TransactionStatusBO.ACCC);
        payment.setPaymentType(PaymentTypeBO.SINGLE);
        payment.setPaymentProduct("sepa");
        return payment;
    }

    private SCAConsentResponseTO getConsentResponse() {
        SCAConsentResponseTO response = new SCAConsentResponseTO();
        response.setBearerToken(new BearerTokenTO());
        response.setPsuMessage(TEMPLATE);
        response.setScaMethods(Collections.emptyList());
        response.setScaStatus(ScaStatusTO.PSUIDENTIFIED);
        return response;
    }

    private SCAOperationBO getOperation() {
        SCAOperationBO op = new SCAOperationBO();
        op.setScaStatus(ScaStatusBO.PSUIDENTIFIED);
        return op;
    }
}