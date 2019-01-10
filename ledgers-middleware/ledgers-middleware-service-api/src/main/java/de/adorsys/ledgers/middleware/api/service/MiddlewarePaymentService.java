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

package de.adorsys.ledgers.middleware.api.service;

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

public interface MiddlewarePaymentService {

    //================= PAYMENT INITIATION ========================//

    /**
     * PROC:01 Initiates a payment. Called by the channel layer.
     * <p>
     * This call sets the status RCVD
     *
     * @param payment : the payment object
     * @param paymentType : the payment type
     * @return : the sca response object.
	 * @throws AccountNotFoundMiddlewareException : account non existant.
	 * @throws NoAccessMiddlewareException : missing permissions
     * @throws PaymentWithIdMiddlewareException : payment with given id exist. 
	 */
    <T> SCAPaymentResponseTO initiatePayment(T payment, PaymentTypeTO paymentType) throws AccountNotFoundMiddlewareException, NoAccessMiddlewareException, PaymentWithIdMiddlewareException;


    // ================= SCA =======================================//
    /**
     * PROC: 02c
     * <p>
     * This is called when the user enters the received code.
     * 
     * @param paymentId : the payment id
     * @param authorisationId : the authorization id
     * @param authCode : the auth code.
     * @return : auth response.
     * @throws SCAOperationNotFoundMiddlewareException : not found
     * @throws SCAOperationValidationMiddlewareException : not valid
     * @throws SCAOperationExpiredMiddlewareException : expired
     * @throws SCAOperationUsedOrStolenMiddlewareException : malicious
     * @throws PaymentNotFoundMiddlewareException  : payment not found.
     */
	SCAPaymentResponseTO authorizePayment(String paymentId, String authorisationId, String authCode)
			throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
			SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, 
			PaymentNotFoundMiddlewareException;

    //============================ Payment Execution ==============================//

    //============================ Payment Status ==============================//

    /**
     * PROC: 04
     * <p>
     * Read the status of a payment. Can be called repetitively after initiation of a payment.
     *
     * @param paymentId : the payment id
     * @return : the transaction status
     * @throws PaymentNotFoundMiddlewareException : payment with id not found.
     */
    TransactionStatusTO getPaymentStatusById(String paymentId) throws PaymentNotFoundMiddlewareException;


    //============================ Payment Status ==============================//

    /**
     * Reads and return a payment.
     *
     * @param paymentId : the payment id
     * @return the payment
     * @throws PaymentNotFoundMiddlewareException
     */
    Object getPaymentById(String paymentId) throws PaymentNotFoundMiddlewareException;
    
    /**
     * Checks the possibility of payment cancellation
     *
     * @param paymentId : the payment id
     * @return : the auth response object.
     * @throws PaymentNotFoundMiddlewareException : not found
     * @throws PaymentProcessingMiddlewareException : processing
     */
    SCAPaymentResponseTO initiatePaymentCancellation(String paymentId) throws PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException;

	String iban(String paymentId);


	SCAPaymentResponseTO loadSCAForPaymentData(String paymentId, String authorisationId) throws PaymentNotFoundMiddlewareException, SCAOperationExpiredMiddlewareException;

	SCAPaymentResponseTO selectSCAMethodForPayment(String paymentId, String authorisationId, String scaMethodId) throws PaymentNotFoundMiddlewareException, SCAMethodNotSupportedMiddleException, UserScaDataNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException;

	SCAPaymentResponseTO loadSCAForCancelPaymentData(String paymentId, String cancellationId) throws PaymentNotFoundMiddlewareException, SCAOperationExpiredMiddlewareException;

	SCAPaymentResponseTO selectSCAMethodForCancelPayment(String paymentId, String cancellationId, String scaMethodId) throws PaymentNotFoundMiddlewareException, SCAMethodNotSupportedMiddleException, UserScaDataNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException;

	SCAPaymentResponseTO authorizeCancelPayment(String paymentId, String cancellationId, String authCode) throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, PaymentNotFoundMiddlewareException;
	
}
