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
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "LDG004 - Payment", description = "Provide endpoint for initiating and executing payment.")
public interface PaymentRestAPI {
    String BASE_PATH = "/payments";

    @GetMapping("/{paymentId}/status")
    @Operation(summary = "Read Payment Status", description = "Returns the status of a payment")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<TransactionStatusTO> getPaymentStatusById(@PathVariable("paymentId") String paymentId);

    @GetMapping(value = "/{paymentId}")
    @Operation(summary = "Load Payment", description = "Returns the payment")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<PaymentTO> getPaymentById(@PathVariable(name = "paymentId") String paymentId);

    @GetMapping(value = "/pending/periodic")
    @Operation(summary = "Load Pending Periodic Payments", description = "Returns a list of pending periodic payment")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<List<PaymentTO>> getPendingPeriodicPayments();

    @PostMapping(params = "paymentType")
    @Operation(summary = "Initiates a Payment", description = "Initiates a payment")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SCAPaymentResponseTO.class)), description = "Success. ScaToken contained in the returned response object."),
            @ApiResponse(responseCode = "404", description = "Specified account not found."),
            @ApiResponse(responseCode = "403", description = "Not authorized to execute payment on this account"),
            @ApiResponse(responseCode = "409", description = "Payment with specified paymentId exists. Either leaved it blank or generate a new one.")
    })
    ResponseEntity<SCAPaymentResponseTO> initiatePayment(
            @RequestParam("paymentType") PaymentTypeTO paymentType,
            @RequestBody PaymentTO payment);


    @PostMapping("/execution")
    @Operation(summary = "Executes a Payment", description = "Executes a payment")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SCAPaymentResponseTO.class)), description = "Success. ScaToken contained in the returned response object."),
            @ApiResponse(responseCode = "404", description = "Specified account not found."),
            @ApiResponse(responseCode = "403", description = "Not authorized to execute payment on this account"),
            @ApiResponse(responseCode = "409", description = "Payment with specified paymentId exists.")
    })
    ResponseEntity<SCAPaymentResponseTO> executePayment(@RequestBody PaymentTO payment);

    @GetMapping(value = "/{paymentId}/authorisations/{authorisationId}")
    @Operation(summary = "Get SCA", description = "Get the authorization response object eventually containing the list of selected sca methods.")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<SCAPaymentResponseTO> getSCA(@PathVariable("paymentId") String paymentId,
                                                @PathVariable("authorisationId") String authorisationId);

    @PutMapping(value = "/{paymentId}/authorisations/{authorisationId}/scaMethods/{scaMethodId}")
    @Operation(summary = "Select SCA Method", description = "Select teh given sca method and request for authentication code generation.")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<SCAPaymentResponseTO> selectMethod(@PathVariable("paymentId") String paymentId,
                                                      @PathVariable("authorisationId") String authorisationId,
                                                      @PathVariable("scaMethodId") String scaMethodId);

    @PutMapping(value = "/{paymentId}/authorisations/{authorisationId}/authCode")
    @Operation(summary = "Send an authentication code for validation", description = "Validate an authentication code and returns the consent token")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<SCAPaymentResponseTO> authorizePayment(@PathVariable("paymentId") String paymentId,
                                                          @PathVariable("authorisationId") String authorisationId,
                                                          @RequestParam(name = "authCode") String authCode);

    // =======
    @PostMapping(value = "/{paymentId}/cancellation-authorisations")
    @Operation(summary = "Initiates a Payment Cancellation", description = "Initiates a Payment Cancellation")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<SCAPaymentResponseTO> initiatePmtCancellation(@PathVariable("paymentId") String paymentId);

    @GetMapping(value = "/{paymentId}/cancellation-authorisations/{cancellationId}")
    @Operation(summary = "Get SCA", description = "Get the authorization response object eventually containing the list of selected sca methods.")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<SCAPaymentResponseTO> getCancelSCA(@PathVariable("paymentId") String paymentId,
                                                      @PathVariable("cancellationId") String cancellationId);

    @PutMapping(value = "/{paymentId}/cancellation-authorisations/{cancellationId}/scaMethods/{scaMethodId}")
    @Operation(summary = "Select SCA Method", description = "Select teh given sca method and request for authentication code generation.")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<SCAPaymentResponseTO> selecCancelPaymentSCAtMethod(@PathVariable("paymentId") String paymentId,
                                                                      @PathVariable("cancellationId") String cancellationId,
                                                                      @PathVariable("scaMethodId") String scaMethodId);

    @PutMapping(value = "/{paymentId}/cancellation-authorisations/{cancellationId}/authCode")
    @Operation(summary = "Send an authentication code for validation", description = "Validate an authentication code and returns the consent token")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<SCAPaymentResponseTO> authorizeCancelPayment(@PathVariable("paymentId") String paymentId,
                                                                @PathVariable("cancellationId") String cancellationId,
                                                                @RequestParam(name = "authCode") String authCode);
}
