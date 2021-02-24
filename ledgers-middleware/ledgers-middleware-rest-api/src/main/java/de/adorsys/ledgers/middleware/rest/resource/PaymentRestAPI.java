/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.adorsys.ledgers.middleware.rest.utils.Constants.*;

@Tag(name = "LDG004 - Payment", description = "Provide endpoint for initiating and executing payment.")
public interface PaymentRestAPI {
    String BASE_PATH = "/payments";

    @GetMapping("/{paymentId}/status")
    @Operation(summary = "Read Payment Status", description = "Returns the status of a payment")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<TransactionStatusTO> getPaymentStatusById(@PathVariable(PAYMENT_ID) String paymentId);

    @GetMapping(value = "/{paymentId}")
    @Operation(summary = "Load Payment", description = "Returns the payment")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<PaymentTO> getPaymentById(@PathVariable(name = PAYMENT_ID) String paymentId);

    @GetMapping(value = "/pending/periodic")
    @Operation(summary = "Load Pending Periodic Payments", description = "Returns a list of pending periodic payment")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<List<PaymentTO>> getPendingPeriodicPayments();

    @GetMapping(value = "/pending/periodic/paged")
    @Operation(summary = "Load Pending Periodic Payments paged view", description = "Returns a page of pending periodic payment")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<CustomPageImpl<PaymentTO>> getPendingPeriodicPaymentsPaged(
            @RequestParam(PAGE) int page,
            @RequestParam(SIZE) int size);

    @PostMapping
    @Operation(summary = "Initiates a Payment", description = "Initiates a payment")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<SCAPaymentResponseTO> initiatePayment(@RequestBody PaymentTO payment);

    @PostMapping(value = "/{paymentId}/execution")
    @Operation(summary = "Executes a Payment", description = "Confirms payment execution")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<SCAPaymentResponseTO> executePayment(@PathVariable(PAYMENT_ID) String paymentId);

    // =======
    @PostMapping(value = "/{paymentId}/cancellation-authorisations")
    @Operation(summary = "Initiates a Payment Cancellation", description = "Initiates a Payment Cancellation")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<SCAPaymentResponseTO> initiatePmtCancellation(@PathVariable(PAYMENT_ID) String paymentId);

    @PostMapping(value = "/{paymentId}/cancellation-authorisations/execute")
    @Operation(summary = "Executes a Payment Cancellation", description = "Confirms payment cancellation execution")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<SCAPaymentResponseTO> executeCancelPayment(@PathVariable(PAYMENT_ID) String paymentId);
}
