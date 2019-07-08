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
import de.adorsys.ledgers.middleware.api.exception.*;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.*;
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
@RequestMapping(PaymentRestAPI.BASE_PATH)
public class PaymentResource implements PaymentRestAPI {
    private final MiddlewarePaymentService paymentService;
    private final ScaInfoHolder scaInfoHolder;

    @Override
    @PreAuthorize("paymentInfoById(#paymentId)")
    public ResponseEntity<TransactionStatusTO> getPaymentStatusById(String paymentId) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentStatusById(paymentId));
        } catch (PaymentNotFoundMiddlewareException e) {
            log.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("paymentInfoById(#paymentId)")
    public ResponseEntity<?> getPaymentById(String paymentId) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
        } catch (PaymentNotFoundMiddlewareException e) {
            log.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("paymentInit(#payment)")
    public ResponseEntity<SCAPaymentResponseTO> initiatePayment(PaymentTypeTO paymentType, Object payment)
    	throws NotFoundRestException, ForbiddenRestException, ConflictRestException{
    	try {
			return new ResponseEntity<>(paymentService.initiatePayment(scaInfoHolder.getScaInfo(), payment, paymentType), HttpStatus.CREATED);
		} catch (AccountNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
		} catch (NoAccessMiddlewareException e) {
            throw new ForbiddenRestException(e.getMessage());
		} catch (PaymentWithIdMiddlewareException e) {
			throw new ConflictRestException(e.getMessage());
		}
    }

    @Override
    @PreAuthorize("paymentInfoById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> getSCA(String paymentId, String authorisationId) {
        try {
        	return ResponseEntity.ok(paymentService.loadSCAForPaymentData(scaInfoHolder.getScaInfo(), paymentId));
        } catch (PaymentNotFoundMiddlewareException | SCAOperationExpiredMiddlewareException e) {
            log.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }
    
    @Override
    @PreAuthorize("paymentInfoById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> selectMethod(String paymentId, String authorisationId, String scaMethodId) {
    	try {
			return ResponseEntity.ok(paymentService.selectSCAMethodForPayment(scaInfoHolder.getScaInfoWithScaMethodId(scaMethodId), paymentId));
		} catch (PaymentNotFoundMiddlewareException | UserScaDataNotFoundMiddlewareException | SCAOperationNotFoundMiddlewareException e) {
            log.error(e.getMessage());
			throw new NotFoundRestException(e.getMessage());
		} catch (SCAMethodNotSupportedMiddleException e) {
            log.error(e.getMessage());
			throw new NotAcceptableRestException(e.getMessage());
		} catch (SCAOperationValidationMiddlewareException e) {
            log.error(e.getMessage());
			throw new ValidationRestException(e.getMessage());
		}
    }

    @Override
    @PreAuthorize("paymentInfoById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> authorizePayment(String paymentId, String authorisationId, String authCode) {
        try {
        	return ResponseEntity.ok(paymentService.authorizePayment(scaInfoHolder.getScaInfoWithAuthCode(authCode), paymentId));
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
        	return ResponseEntity.ok(paymentService.initiatePaymentCancellation(scaInfoHolder.getScaInfo(), paymentId));
        } catch (PaymentNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        } catch (PaymentProcessingMiddlewareException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @Override
    @PreAuthorize("paymentInfoById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> getCancelSCA(String paymentId, 
    		String cancellationId) throws ConflictRestException{
        try {
        	return ResponseEntity.ok(paymentService.loadSCAForCancelPaymentData(scaInfoHolder.getScaInfo(), paymentId, cancellationId));
        } catch (PaymentNotFoundMiddlewareException | SCAOperationExpiredMiddlewareException e) {
            log.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }
    
    @Override
    @PreAuthorize("paymentInfoById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> selecCancelPaymentSCAtMethod(String paymentId, 
    	    String cancellationId, String scaMethodId) {
    	try {
			return ResponseEntity.ok(paymentService.selectSCAMethodForCancelPayment(scaInfoHolder.getScaInfoWithScaMethodId(scaMethodId), paymentId, cancellationId));
		} catch (PaymentNotFoundMiddlewareException | UserScaDataNotFoundMiddlewareException | SCAOperationNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
		} catch (SCAMethodNotSupportedMiddleException e) {
			throw new NotAcceptableRestException(e.getMessage());
		} catch (SCAOperationValidationMiddlewareException e) {
			throw new ValidationRestException(e.getMessage());
		}
    }
    
    @Override
    @PreAuthorize("paymentInfoById(#paymentId)")
    public ResponseEntity<SCAPaymentResponseTO> authorizeCancelPayment(String paymentId,String cancellationId, String authCode) {
        try {
        	return ResponseEntity.ok(paymentService.authorizeCancelPayment(scaInfoHolder.getScaInfoWithAuthCode(authCode), paymentId, cancellationId));
		} catch (SCAOperationNotFoundMiddlewareException | PaymentNotFoundMiddlewareException | SCAOperationExpiredMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
		} catch (SCAOperationValidationMiddlewareException e) {
			throw new ValidationRestException(e.getMessage());
		} catch (SCAOperationUsedOrStolenMiddlewareException e) {
			throw new NotAcceptableRestException(e.getMessage());
		}
    }
}
