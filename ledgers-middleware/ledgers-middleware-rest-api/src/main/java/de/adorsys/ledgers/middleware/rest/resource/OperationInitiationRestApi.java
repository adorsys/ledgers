/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static de.adorsys.ledgers.middleware.rest.utils.Constants.*;

@Tag(name = "LDGXXX - Payment", description = "Provide endpoints for operation initiation")
public interface OperationInitiationRestApi {
    String BASE_PATH = "/operation";

    @PostMapping(value = "/payment", params = "paymentType")
    @Operation(summary = "Initiates a Payment", description = "Initiates a payment")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<GlobalScaResponseTO> initiatePayment(
            @RequestParam(PAYMENT_TYPE) PaymentTypeTO paymentType,
            @RequestBody PaymentTO payment);

    @PostMapping(value = "/cancellation/{opId}")
    @Operation(summary = "Initiates a Payment Cancellation", description = "Initiates a Payment Cancellation")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<GlobalScaResponseTO> initiatePmtCancellation(@PathVariable("opId") String opId);

    @PostMapping(value = "/consent")
    @Operation(summary = "Initiate AIS consent", description = "Validates AIS consent and stores it for future usage")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<GlobalScaResponseTO> initiateAisConsent(@RequestBody AisConsentTO aisConsent);

    @PostMapping("/{opType}/{opId}/execution")
    @Operation(summary = "Executes a Payment", description = "Confirms payment execution")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<GlobalScaResponseTO> execution(@PathVariable("opType") OpTypeTO opType,
                                                  @PathVariable("opId") String opId);

}
