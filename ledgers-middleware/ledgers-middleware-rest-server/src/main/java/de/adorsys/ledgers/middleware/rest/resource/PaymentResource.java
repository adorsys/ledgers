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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.NoAccessMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentWithIdMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAMethodNotSupportedMiddleException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationExpiredMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationUsedOrStolenMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationValidationMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserScaDataNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ExpectationFailedRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.GoneRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotAcceptableRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.ValidationRestException;

@RestController
@MiddlewareUserResource
@RequestMapping(PaymentRestAPI.BASE_PATH)
public class PaymentResource implements PaymentRestAPI {
	private static final Logger logger = LoggerFactory.getLogger(PaymentResource.class);

    private final MiddlewarePaymentService paymentService;

    public PaymentResource(MiddlewarePaymentService middlewareService) {
        this.paymentService = middlewareService;
    }

    @Override
    @PreAuthorize("paymentInitById(#paymentId)")
    public ResponseEntity<TransactionStatusTO> getPaymentStatusById(String paymentId) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentStatusById(paymentId));
        } catch (PaymentNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("paymentInitById(#paymentId)")
    public ResponseEntity<?> getPaymentById(String paymentId) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
        } catch (PaymentNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("paymentInit(#payment)")
    public ResponseEntity<SCAPaymentResponseTO> initiatePayment(PaymentTypeTO paymentType, Object payment)
    	throws NotFoundRestException, ForbiddenRestException, ConflictRestException{
    	try {
			return new ResponseEntity<SCAPaymentResponseTO>(paymentService.initiatePayment(payment, paymentType), HttpStatus.CREATED);
		} catch (AccountNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
		} catch (NoAccessMiddlewareException e) {
            throw new ForbiddenRestException(e.getMessage());
		} catch (PaymentWithIdMiddlewareException e) {
			throw new ConflictRestException(e.getMessage());
		}
    }

    @Override
    @PreAuthorize("paymentInitById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> getSCA(String paymentId, String authorisationId) {
        try {
        	return ResponseEntity.ok(paymentService.loadSCAForPaymentData(paymentId, authorisationId));
        } catch (PaymentNotFoundMiddlewareException | SCAOperationExpiredMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }
    
    @Override
    @PreAuthorize("paymentInitById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> selectMethod(String paymentId, 
    		String authorisationId,
    		String scaMethodId) throws ValidationRestException, ConflictRestException, NotFoundRestException
    {
    	try {
			return ResponseEntity.ok(paymentService.selectSCAMethodForPayment(paymentId, authorisationId, scaMethodId));
		} catch (PaymentNotFoundMiddlewareException | UserScaDataNotFoundMiddlewareException | SCAOperationNotFoundMiddlewareException e) {
            logger.error(e.getMessage());
			throw new NotFoundRestException(e.getMessage());
		} catch (SCAMethodNotSupportedMiddleException e) {
            logger.error(e.getMessage());
			throw new NotAcceptableRestException(e.getMessage());
		} catch (SCAOperationValidationMiddlewareException e) {
            logger.error(e.getMessage());
			throw new ValidationRestException(e.getMessage());
		}
    }

    @Override
    @PreAuthorize("paymentInitById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> authorizePayment(String paymentId,
    		String authorisationId, 
    		String authCode) throws GoneRestException,NotFoundRestException, ConflictRestException, ExpectationFailedRestException, NotAcceptableRestException
    {
        try {
        	return ResponseEntity.ok(paymentService.authorizePayment(paymentId, authorisationId, authCode));
        } catch (SCAOperationNotFoundMiddlewareException | PaymentNotFoundMiddlewareException e) {
			throw new NotFoundRestException(e.getMessage());
		} catch (SCAOperationValidationMiddlewareException e) {
			throw new ValidationRestException(e.getMessage());
		} catch (SCAOperationExpiredMiddlewareException e) {
			throw new GoneRestException(e.getMessage());
		} catch (SCAOperationUsedOrStolenMiddlewareException e) {
			throw new NotAcceptableRestException(e.getMessage());
		}
    }

    @Override
    @PreAuthorize("paymentInitById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> initiatePmtCancellation(String paymentId) {
        try {
        	return ResponseEntity.ok(paymentService.initiatePaymentCancellation(paymentId));
        } catch (PaymentNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        } catch (PaymentProcessingMiddlewareException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @Override
    @PreAuthorize("paymentInitById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> getCancelSCA(String paymentId, 
    		String cancellationId) throws ConflictRestException{
        try {
        	return ResponseEntity.ok(paymentService.loadSCAForCancelPaymentData(paymentId, cancellationId));
        } catch (PaymentNotFoundMiddlewareException | SCAOperationExpiredMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }
    
    @Override
    @PreAuthorize("paymentInitById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> selecCancelPaymentSCAtMethod(String paymentId, 
    	    String cancellationId, String scaMethodId) throws ValidationRestException, ConflictRestException, NotFoundRestException
    {
    	try {
			return ResponseEntity.ok(paymentService.selectSCAMethodForCancelPayment(paymentId, cancellationId, scaMethodId));
		} catch (PaymentNotFoundMiddlewareException | UserScaDataNotFoundMiddlewareException | SCAOperationNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
		} catch (SCAMethodNotSupportedMiddleException e) {
			throw new NotAcceptableRestException(e.getMessage());
		} catch (SCAOperationValidationMiddlewareException e) {
			throw new ValidationRestException(e.getMessage());
		}
    }
    
    @Override
    @PreAuthorize("paymentInitById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> authorizeCancelPayment(String paymentId,String cancellationId, String authCode) throws GoneRestException,NotFoundRestException, ConflictRestException, ExpectationFailedRestException, NotAcceptableRestException
    {
        try {
        	return ResponseEntity.ok(paymentService.authorizeCancelPayment(paymentId, cancellationId, authCode));
		} catch (SCAOperationNotFoundMiddlewareException | PaymentNotFoundMiddlewareException | SCAOperationExpiredMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
		} catch (SCAOperationValidationMiddlewareException e) {
			throw new ValidationRestException(e.getMessage());
		} catch (SCAOperationUsedOrStolenMiddlewareException e) {
			throw new NotAcceptableRestException(e.getMessage());
		}
    }
}
