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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScaResponseResolverTest {
    private static final String TEMPLATE = "TEMPLATE";
    private static final String PAYMENT_ID = "payment id";
    private static final String AUTH_ID = "auth id";
    private static final String PSU_MSG = "user msg";
    private static final String SCA_METHOD = "MAIL";

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
    public void completeResponse() {
        SCAConsentResponseTO response = getConsentResponse();
        service.completeResponse(response, getOperation(), new UserTO(), TEMPLATE, new BearerTokenTO());
        assertThat(response).isEqualToComparingFieldByFieldRecursively(getConsentResponse());
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

    @Test
    public void resolveScaStatus() {
        ScaStatusTO result = service.resolveScaStatus(TokenUsageTO.LOGIN, false);
        assertThat(result).isEqualTo(ScaStatusTO.EXEMPTED);
    }

    @Test
    public void resolveScaStatus_sca_required() {
        ScaStatusTO result = service.resolveScaStatus(TokenUsageTO.LOGIN, true);
        assertThat(result).isEqualTo(ScaStatusTO.PSUAUTHENTICATED);
    }

    @Test
    public void resolveScaStatus_sca_required_delegated_token() {
        ScaStatusTO result = service.resolveScaStatus(TokenUsageTO.DELEGATED_ACCESS, true);
        assertThat(result).isEqualTo(ScaStatusTO.PSUIDENTIFIED);
    }

    @Test
    public void prepareScaAndUpdateResponse() {
        SCAPaymentResponseTO response = getPaymentResponse();
        when(scaOperationService.createAuthCode(any(), any())).thenReturn(new SCAOperationBO());
        service.prepareScaAndUpdateResponse(PAYMENT_ID, response, AUTH_ID, PSU_MSG, 100, new UserBO(), OpTypeBO.PAYMENT);
        assertThat(response).isEqualToComparingFieldByFieldRecursively(getPaymentResponse());
    }

    private SCAPaymentResponseTO getPaymentResponse() {
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        response.setScaStatus(ScaStatusTO.PSUIDENTIFIED);
        return response;
    }

    @Test
    public void generateCodeAndUpdateResponse() {
        when(scaOperationService.generateAuthCode(any(), any(), any())).thenReturn(new SCAOperationBO());
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        service.generateCodeAndUpdateResponse(PAYMENT_ID, response, AUTH_ID, PSU_MSG, 100, new UserBO(), OpTypeBO.PAYMENT, SCA_METHOD);
        assertThat(response).isEqualToComparingFieldByFieldRecursively(new SCAPaymentResponseTO());
    }

    @Test
    public void updateScaResponseFields() {
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        service.updateScaResponseFields(new UserBO(), response, AUTH_ID, PSU_MSG, new BearerTokenTO(), ScaStatusTO.PSUIDENTIFIED, 100);
        SCAPaymentResponseTO expected = getPaymentResponse();
        expected.setBearerToken(new BearerTokenTO());
        expected.setPsuMessage(PSU_MSG);
        expected.setScaMethods(Collections.emptyList());
        expected.setAuthorisationId(AUTH_ID);
        assertThat(response).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void updatePaymentRelatedResponseFields() {
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        service.updatePaymentRelatedResponseFields(response, getPayment());
        assertThat(response).isEqualToComparingFieldByFieldRecursively(getExpectedPaymentResponse());
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
}