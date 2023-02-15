/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.initiation;

import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.sca.*;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.client.rest.AuthRequestInterceptor;
import de.adorsys.ledgers.middleware.client.rest.PaymentRestClient;
import de.adorsys.ledgers.middleware.client.rest.RedirectScaRestClient;
import de.adorsys.ledgers.util.Ids;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class PaymentRestInitiationService {
    private final PaymentRestClient paymentRestClient;
    private final RedirectScaRestClient scaRestClient;
    private final AuthRequestInterceptor authRequestInterceptor;
    private final KeycloakTokenService tokenService;

    public PaymentRestInitiationService(PaymentRestClient paymentRestClient, RedirectScaRestClient scaRestClient, AuthRequestInterceptor authRequestInterceptor, KeycloakTokenService tokenService) {
        this.paymentRestClient = paymentRestClient;
        this.scaRestClient = scaRestClient;
        this.authRequestInterceptor = authRequestInterceptor;
        this.tokenService = tokenService;
    }

    public void executePayment(UserTO user, PaymentTO payment) {
        try {
            loginUser(user);
            SCAPaymentResponseTO initiationResponse = initiatePayment(payment);
            performScaIfRequired(initiationResponse);
            confirmPayment(initiationResponse.getPaymentId());
            authRequestInterceptor.setAccessToken(null);
        } catch (FeignException e) {
            log.error("Payment from: {}, failed due to: {},{}", user.getLogin(), e.contentUTF8(), e.getMessage());
            authRequestInterceptor.setAccessToken(null);
        }
    }

    private void loginUser(UserTO user) {
        BearerTokenTO login = tokenService.login(user.getLogin(), user.getPin());
        log.info("Logged in user: {}", user.getLogin());
        authRequestInterceptor.setAccessToken(login.getAccess_token());
    }

    private SCAPaymentResponseTO initiatePayment(PaymentTO payment) {
        SCAPaymentResponseTO response = Optional.ofNullable(paymentRestClient.initiatePayment(payment).getBody()).orElse(new SCAPaymentResponseTO());
        log.info("Payment for {} successfully initiated, ScaStatus: {}, transaction status: {}", payment.getDebtorAccount().getIban(),
                 response.getScaStatus(), response.getTransactionStatus());
        authRequestInterceptor.setAccessToken(response.getBearerToken().getAccess_token());
        return response;
    }

    private void performScaIfRequired(SCAPaymentResponseTO response) {
        //Only executes if Sca is required
        if (response.getScaStatus() != ScaStatusTO.EXEMPTED) {
            GlobalScaResponseTO startSca = Optional.ofNullable(startSca(response)).orElse(new GlobalScaResponseTO());
            GlobalScaResponseTO selectMethod = Optional.ofNullable(scaRestClient.selectMethod(startSca.getAuthorisationId(), startSca.getScaMethods().iterator().next().getId()).getBody()).orElse(new GlobalScaResponseTO());
            GlobalScaResponseTO scaFinalized = Optional.ofNullable(scaRestClient.validateScaCode(selectMethod.getAuthorisationId(), selectMethod.getTan()).getBody()).orElse(new GlobalScaResponseTO());
            authRequestInterceptor.setAccessToken(scaFinalized.getBearerToken().getAccess_token());
        }
    }

    private GlobalScaResponseTO startSca(SCAPaymentResponseTO response) {
        StartScaOprTO opr = new StartScaOprTO(response.getPaymentId(), null, Ids.id(), OpTypeTO.PAYMENT);
        authRequestInterceptor.setAccessToken(response.getBearerToken().getAccess_token());
        return scaRestClient.startSca(opr).getBody();
    }

    private void confirmPayment(String paymentId) {
        SCAPaymentResponseTO response = Optional.ofNullable(paymentRestClient.executePayment(paymentId).getBody()).orElse(new SCAPaymentResponseTO());
        log.info("Payment successfully executed! Transaction status: {}", response.getTransactionStatus());
    }
}
