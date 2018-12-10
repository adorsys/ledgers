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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentCancellationResponseTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.exception.PaymentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@RestController
@RequestMapping(PaymentResource.BASE_PATH)
@Api(tags = "Payment" , description= "Provide endpoint for initiating and executing payment.")
@MiddlewareUserResource
public class PaymentResource {
    public static final String EXECUTE_NO_SCA_PATH = "/execute-no-sca/{payment-id}/{payment-product}/{payment-type}";

	public static final String PAYMENT_TYPE_PATH_VARIABLE = "/{paymentType}";

	public static final String BASE_PATH = "/payments";

	private static final Logger logger = LoggerFactory.getLogger(PaymentResource.class);

    private final MiddlewareService middlewareService;

    public PaymentResource(MiddlewareService middlewareService) {
        this.middlewareService = middlewareService;
    }

    @GetMapping("/{id}/status")
    @ApiOperation(value="Read Payment Status", notes="Returns the status of a payment", authorizations =@Authorization(value="apiKey"))
    public TransactionStatusTO getPaymentStatusById(@PathVariable String id) {
        try {
            return middlewareService.getPaymentStatusById(id);
        } catch (PaymentNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @GetMapping(value = "/{payment-type}/{payment-product}/{paymentId}"/*, produces = {"application/json", "application/xml", "multipart/form-data"}*/)
    @PreAuthorize("paymentInitById(#paymentId)")
    @ApiOperation(value="Load Payment", notes="Returns a payment", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<?> getPaymentById(@PathVariable(name = "payment-type") PaymentTypeTO paymentType,
                                            @PathVariable(name = "payment-product") PaymentProductTO paymentProduct,
                                            @PathVariable(name = "paymentId") String paymentId) {
        try {
            return ResponseEntity.ok(middlewareService.getPaymentById(paymentType, paymentProduct, paymentId));
        } catch (PaymentNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @PostMapping(PAYMENT_TYPE_PATH_VARIABLE)
    @PreAuthorize("paymentInit(#payment)")
    @ApiOperation(value="Initiates a Payment", notes="Initiates a payment", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<?> initiatePayment(@PathVariable PaymentTypeTO paymentType, @RequestBody Object payment) {
        try {
            return new ResponseEntity(middlewareService.initiatePayment(payment, paymentType), HttpStatus.CREATED);
        } catch (Exception e) { //TODO add corresponding exceptions later (initiate payment full procedure with balance checking etc.)
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @PostMapping(EXECUTE_NO_SCA_PATH)
    @PreAuthorize("paymentInitById(#paymentId)")
    @ApiOperation(value="Execute a Payment", notes="Executes a payment", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<TransactionStatusTO> executePaymentNoSca(@PathVariable(name = "payment-id") String paymentId,
                                                                   @PathVariable(name = "payment-product") PaymentProductTO paymentProduct,
                                                                   @PathVariable(name = "payment-type") PaymentTypeTO paymentType) {
        try {
            TransactionStatusTO status = middlewareService.executePayment(paymentId);
            return ResponseEntity.ok(status);
        } catch (PaymentProcessingMiddlewareException e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().header("message", e.getMessage()).build(); //TODO Create formal rest error messaging, fix all internal service errors to comply some pattern.
        }
    }

    @PostMapping(value = "/cancel-initiation/{psuId}/{paymentId}")
    @PreAuthorize("paymentInitById(#paymentId)")
    @ApiOperation(value="Initiates a Payment Cancelation", notes="Initiates a Payment Cancelation", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<PaymentCancellationResponseTO> initiatePmtCancellation(@PathVariable String psuId, @PathVariable String paymentId) {
        try {
            PaymentCancellationResponseTO response = middlewareService.initiatePaymentCancellation(psuId, paymentId);
            return ResponseEntity.ok(response);
        } catch (UserNotFoundMiddlewareException | PaymentNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        } catch (PaymentProcessingMiddlewareException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @DeleteMapping("/cancel/{paymentId}")
    @PreAuthorize("paymentInitById(#paymentId)")
    @ApiOperation(value="Confirm Cancelation", notes="Confirms the Cancelation of a Payment", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity cancelPaymentNoSca(@PathVariable String paymentId) {
        try {
            middlewareService.cancelPayment(paymentId);
        } catch (UserNotFoundMiddlewareException | PaymentNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        } catch (PaymentProcessingMiddlewareException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
        return ResponseEntity.noContent().build();
    }
}
