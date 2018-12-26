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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentCancellationResponseTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.exception.PaymentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationExpiredMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationUsedOrStolenMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationValidationMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;

@RestController
@MiddlewareUserResource
@RequestMapping(PaymentRestAPI.BASE_PATH)
public class PaymentResource implements PaymentRestAPI {
	private static final Logger logger = LoggerFactory.getLogger(PaymentResource.class);

    private final MiddlewareService middlewareService;
    
    private final MiddlewareAccountManagementService accountManagementService;


    public PaymentResource(MiddlewareService middlewareService, MiddlewareAccountManagementService accountManagementService) {
        this.middlewareService = middlewareService;
        this.accountManagementService = accountManagementService;
    }

    @Override
    public ResponseEntity<TransactionStatusTO> getPaymentStatusById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(middlewareService.getPaymentStatusById(id));
        } catch (PaymentNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
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

    @Override
    public ResponseEntity<?> initiatePayment(@PathVariable PaymentTypeTO paymentType, @RequestBody Object payment) {
        try {
            return new ResponseEntity(middlewareService.initiatePayment(payment, paymentType), HttpStatus.CREATED);
        } catch (Exception e) { //TODO add corresponding exceptions later (initiate payment full procedure with balance checking etc.)
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
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

    @Override
    public ResponseEntity<BearerTokenTO> authorizePayment(@PathVariable(name = "payment-id") String paymentId,
    		@RequestParam(name="authCode") String authCode,
    		@RequestParam(name="opId") String opId) {
        try {
        	BearerTokenTO bearerTokenTO = middlewareService.authorizePayment(paymentId, opId, authCode);
            return ResponseEntity.ok(bearerTokenTO);
        } catch (SCAOperationNotFoundMiddlewareException e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().header("message", e.getMessage()).build(); 
            //TODO Create formal rest error messaging, fix all internal service errors to comply some pattern.
		} catch (SCAOperationValidationMiddlewareException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).header("message", e.getMessage()).build(); 
		} catch (SCAOperationExpiredMiddlewareException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.GONE).header("message", e.getMessage()).build(); 
		} catch (SCAOperationUsedOrStolenMiddlewareException e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).header("message", e.getMessage()).build(); 
		}
    }

    @Override
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

    @Override
    public ResponseEntity<Void> cancelPaymentNoSca(@PathVariable String paymentId) {
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
