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

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import io.swagger.annotations.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "LDG003 - Payment", description = "Provide endpoint for initiating and executing payment.")
public interface PaymentRestAPI {
    String BASE_PATH = "/payments";

    @GetMapping("/{paymentId}/status")
    @ApiOperation(value = "Read Payment Status", notes = "Returns the status of a payment", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<TransactionStatusTO> getPaymentStatusById(@PathVariable("paymentId") String paymentId);

    @GetMapping(value = "/{paymentId}")
    @ApiOperation(value = "Load Payment", notes = "Returns the payment", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<?> getPaymentById(@PathVariable(name = "paymentId") String paymentId);

    @PostMapping(params = "paymentType")
    @ApiOperation(value = "Initiates a Payment", notes = "Initiates a payment", authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = SCAPaymentResponseTO.class, message = "Success. ScaToken contained in the returned response object."),
            @ApiResponse(code = 404, message = "Specified account not found."),
            @ApiResponse(code = 403, message = "Not authorized to execute payment on this account"),
            @ApiResponse(code = 409, message = "Payment with specified paymentId exists. Either leaved it blank or generate a new one.")
    })
    ResponseEntity<SCAPaymentResponseTO> initiatePayment(
            @RequestParam("paymentType") PaymentTypeTO paymentType,
            @RequestBody Object payment);

    @PostMapping(value = "/pain", params = "paymentType", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    @ApiOperation(value = "Initiates a pain Payment", notes = "Initiates a pain payment", authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = String.class, message = "Success"),
            @ApiResponse(code = 404, message = "Specified account not found."),
            @ApiResponse(code = 403, message = "Not authorized to execute payment on this account"),
            @ApiResponse(code = 409, message = "Payment with specified paymentId exists. Either leaved it blank or generate a new one.")
    })
    ResponseEntity<String> initiatePainPayment(
            @RequestParam("paymentType") PaymentTypeTO paymentType,
            @RequestBody String payment);

    @GetMapping(value = "/{paymentId}/authorisations/{authorisationId}")
    @ApiOperation(value = "Get SCA", notes = "Get the authorization response object eventually containing the list of selected sca methods.",
            authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<SCAPaymentResponseTO> getSCA(@PathVariable("paymentId") String paymentId,
                                                @PathVariable("authorisationId") String authorisationId);

    @PutMapping(value = "/{paymentId}/authorisations/{authorisationId}/scaMethods/{scaMethodId}")
    @ApiOperation(value = "Select SCA Method", notes = "Select teh given sca method and request for authentication code generation.",
            authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<SCAPaymentResponseTO> selectMethod(@PathVariable("paymentId") String paymentId,
                                                      @PathVariable("authorisationId") String authorisationId,
                                                      @PathVariable("scaMethodId") String scaMethodId);

    @PutMapping(value = "/{paymentId}/authorisations/{authorisationId}/authCode")
    @ApiOperation(value = "Send an authentication code for validation", notes = "Validate an authetication code and returns the cosent token", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<SCAPaymentResponseTO> authorizePayment(@PathVariable("paymentId") String paymentId,
                                                          @PathVariable("authorisationId") String authorisationId,
                                                          @RequestParam(name = "authCode") String authCode);

    // =======
    @PostMapping(value = "/{paymentId}/cancellation-authorisations")
    @ApiOperation(value = "Initiates a Payment Cancelation", notes = "Initiates a Payment Cancelation", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<SCAPaymentResponseTO> initiatePmtCancellation(@PathVariable("paymentId") String paymentId);

    @GetMapping(value = "/{paymentId}/cancellation-authorisations/{cancellationId}")
    @ApiOperation(value = "Get SCA", notes = "Get the authorization response object eventually containing the list of selected sca methods.",
            authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<SCAPaymentResponseTO> getCancelSCA(@PathVariable("paymentId") String paymentId,
                                                      @PathVariable("cancellationId") String cancellationId);

    @PutMapping(value = "/{paymentId}/cancellation-authorisations/{cancellationId}/scaMethods/{scaMethodId}")
    @ApiOperation(value = "Select SCA Method", notes = "Select teh given sca method and request for authentication code generation.",
            authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<SCAPaymentResponseTO> selecCancelPaymentSCAtMethod(@PathVariable("paymentId") String paymentId,
                                                                      @PathVariable("cancellationId") String cancellationId,
                                                                      @PathVariable("scaMethodId") String scaMethodId);

    @PutMapping(value = "/{paymentId}/cancellation-authorisations/{cancellationId}/authCode")
    @ApiOperation(value = "Send an authentication code for validation", notes = "Validate an authetication code and returns the cosent token", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<SCAPaymentResponseTO> authorizeCancelPayment(@PathVariable("paymentId") String paymentId,
                                                                @PathVariable("cancellationId") String cancellationId,
                                                                @RequestParam(name = "authCode") String authCode);
}
