package de.adorsys.ledgers.app.initiation;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.client.rest.AuthRequestInterceptor;
import de.adorsys.ledgers.middleware.client.rest.PaymentRestClient;
import de.adorsys.ledgers.middleware.client.rest.UserMgmtRestClient;
import de.adorsys.ledgers.util.Ids;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentRestInitiationService {
    private final UserMgmtRestClient userMgmtRestClient;
    private final PaymentRestClient paymentRestClient;
    private final AuthRequestInterceptor authRequestInterceptor;
    private final Logger logger = LoggerFactory.getLogger(PaymentRestInitiationService.class);


    public PaymentRestInitiationService(UserMgmtRestClient userMgmtRestClient, PaymentRestClient paymentRestClient, AuthRequestInterceptor authRequestInterceptor) {
        this.userMgmtRestClient = userMgmtRestClient;
        this.paymentRestClient = paymentRestClient;
        this.authRequestInterceptor = authRequestInterceptor;
    }

    public void executePayment(UserTO user, PaymentTypeTO paymentType, PaymentTO payment) {
        try {
            loginUser(user);
            SCAPaymentResponseTO response = paymentRestClient.initiatePayment(paymentType, payment).getBody();
            logger.info("Payment from: {}, successfully committed, payment ID: {}, transaction status: {}", user.getLogin(), response.getPaymentId(), response.getTransactionStatus());
            performScaIfRequired(response);
        } catch (FeignException e) {
            logger.error("Payment from: {}, failed due to: {}", user.getLogin(), e.contentUTF8());
        }
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    private void performScaIfRequired(SCAPaymentResponseTO response) {
        //Select Sca Method
        try {
            if (response.getScaStatus() == ScaStatusTO.PSUAUTHENTICATED) {
                response = paymentRestClient.selectMethod(response.getPaymentId(), response.getAuthorisationId(), response.getScaMethods().iterator().next().getId()).getBody();
                authRequestInterceptor.setAccessToken(response.getBearerToken().getAccess_token());
            }
            //Confirm TAN
            if (response.getScaStatus() == ScaStatusTO.SCAMETHODSELECTED) {
                paymentRestClient.authorizePayment(response.getPaymentId(), response.getAuthorisationId(), "123456").getBody();
                authRequestInterceptor.setAccessToken(null);
                logger.info("Payment finalized!");
            }
        } catch (FeignException e) {
            logger.error("Failed authorising payment: {}, authId: {}", response.getPaymentId(), response.getAuthorisationId());
        }
    }

    private void loginUser(UserTO user) {
        try {
            //Login
            String id = Ids.id();
            SCALoginResponseTO response = userMgmtRestClient.authoriseForConsent(user.getLogin(), user.getPin(), id, id, OpTypeTO.PAYMENT).getBody();
            authRequestInterceptor.setAccessToken(response.getBearerToken().getAccess_token());
        } catch (FeignException e) {
            logger.error("Could not Login user: {}, error: {}", user.getLogin(), e.getMessage());
        }
    }
}
