package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.service.OperationService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@MiddlewareUserResource
@RequiredArgsConstructor
@RequestMapping(OperationInitiationRestApi.BASE_PATH)
public class OperationInitiationResource implements OperationInitiationRestApi {
    private final OperationService operationService;
    private final ScaInfoHolder scaInfoHolder;

    @Override
    @PreAuthorize("hasAccessToAccountWithIban(#payment.debtorAccount.iban)")
    public ResponseEntity<GlobalScaResponseTO> initiatePayment(PaymentTypeTO paymentType, PaymentTO payment) {
        return new ResponseEntity<>(operationService.resolveInitiation(OpTypeTO.PAYMENT, null, payment, scaInfoHolder.getScaInfo()), HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasAccessToAccountByPaymentId(#opId)")
    public ResponseEntity<GlobalScaResponseTO> initiatePmtCancellation(String opId) {
        return new ResponseEntity<>(operationService.resolveInitiation(OpTypeTO.CANCEL_PAYMENT, opId, null, scaInfoHolder.getScaInfo()), HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','CUSTOMER') and hasAccessToAccountsWithIbans(#aisConsent.access.listedAccountsIbans)")
    public ResponseEntity<GlobalScaResponseTO> initiateAisConsent(AisConsentTO aisConsent) {
        return new ResponseEntity<>(operationService.resolveInitiation(OpTypeTO.CONSENT, null, aisConsent, scaInfoHolder.getScaInfo()), HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasPartialScope() and hasAccessToAccountByPaymentId(#opId)")
    public ResponseEntity<GlobalScaResponseTO> execution(OpTypeTO opType, String opId) {
        return ResponseEntity.ok(operationService.execute(opType, opId, scaInfoHolder.getScaInfo()));
    }
}