package de.adorsys.ledgers.app.initiation;

import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
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

    public void executePayment(UserTO user, PaymentTypeTO paymentType, PaymentTO payment) {
        try {
            loginUser(user);
            SCAPaymentResponseTO initiationResponse = initiatePayment(paymentType, payment);
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

    private SCAPaymentResponseTO initiatePayment(PaymentTypeTO paymentType, PaymentTO payment) {
        SCAPaymentResponseTO response = paymentRestClient.initiatePayment(paymentType, payment).getBody();
        log.info("Payment for {} successfully initiated, ScaStatus: {}, transaction status: {}", payment.getDebtorAccount().getIban(), response.getScaStatus(), response.getTransactionStatus());
        authRequestInterceptor.setAccessToken(response.getBearerToken().getAccess_token());
        return response;
    }

    private void performScaIfRequired(SCAPaymentResponseTO response) {
        //Only executes if Sca is required
        if (response.getScaStatus() != ScaStatusTO.EXEMPTED) {
            GlobalScaResponseTO startSca = startSca(response);
            GlobalScaResponseTO selectMethod = scaRestClient.selectMethod(startSca.getAuthorisationId(), startSca.getScaMethods().iterator().next().getId()).getBody();
            GlobalScaResponseTO scaFinalized = scaRestClient.validateScaCode(selectMethod.getAuthorisationId(), selectMethod.getTan()).getBody();
            authRequestInterceptor.setAccessToken(scaFinalized.getBearerToken().getAccess_token());
        }
    }

    private GlobalScaResponseTO startSca(SCAPaymentResponseTO response) {
        StartScaOprTO opr = new StartScaOprTO(response.getPaymentId(), Ids.id(), OpTypeTO.PAYMENT);
        authRequestInterceptor.setAccessToken(response.getBearerToken().getAccess_token());
        return scaRestClient.startSca(opr).getBody();
    }

    private void confirmPayment(String paymentId) {
        SCAPaymentResponseTO response = paymentRestClient.executePayment(paymentId).getBody();
        log.info("Payment successfully executed! Transaction status: {}", response.getTransactionStatus());
    }
}
