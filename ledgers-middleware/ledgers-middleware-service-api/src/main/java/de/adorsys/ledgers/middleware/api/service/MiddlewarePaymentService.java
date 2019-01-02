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
     * @param payment
     * @param paymentType
     * @return
	 * @throws AccountNotFoundMiddlewareException
	 * @throws NoAccessMiddlewareException
	 */
    <T> SCAPaymentResponseTO initiatePayment(T payment, PaymentTypeTO paymentType) throws AccountNotFoundMiddlewareException, NoAccessMiddlewareException;


    // ================= SCA =======================================//

    /**
     * PROC: 02b
     * <p>
     * After the PSU selects the SCA method, this is called to generate and send the auth code.
     *
     * @param authCodeData Data that needed for auth code generation
     * @return opId id of operation created on the request
     * @throws AuthCodeGenerationMiddlewareException  if something happens during auth code generation
     * @throws SCAMethodNotSupportedMiddleException   if user sca method doesn't support by ledgers
     * @throws UserNotFoundMiddlewareException        if user not found by id
     * @throws UserScaDataNotFoundMiddlewareException if sca user data not found by id
    String generateAuthCode(AuthCodeDataTO authCodeData) throws AuthCodeGenerationMiddlewareException, SCAMethodNotSupportedMiddleException, UserNotFoundMiddlewareException, UserScaDataNotFoundMiddlewareException;
     */

    /**
     * PROC: 02c
     * <p>
     * This is called when the user enters the received code.
     * 
     * @param paymentId
     * @param authorisationId
     * @param authCode
     * @return
     * @throws SCAOperationNotFoundMiddlewareException
     * @throws SCAOperationValidationMiddlewareException
     * @throws SCAOperationExpiredMiddlewareException
     * @throws SCAOperationUsedOrStolenMiddlewareException
     * @throws PaymentNotFoundMiddlewareException 
     * @throws AuthorisationNotFoundMiddlewareException
     */
	SCAPaymentResponseTO authorizePayment(String paymentId, String authorisationId, String authCode)
			throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
			SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, 
			PaymentNotFoundMiddlewareException;

    //============================ Payment Execution ==============================//

    /**
     * PROC: 03
     * <p>
     * Is called by the channel layer after successfull SCA or no SCA.
     * - When SCA is not needed
     * - After a successfull SCA
     * payment status will be set to ACSP.
     *
     * @param paymentId
     * @return
     * @throws PaymentProcessingMiddlewareException
    TransactionStatusTO executePayment(String paymentId) throws PaymentProcessingMiddlewareException;
     */

    //============================ Payment Status ==============================//

    /**
     * PROC: 04
     * <p>
     * Read the status of a payment. Can be called repetitively after initiation of a payment.
     *
     * @param paymentId
     * @return
     * @throws PaymentNotFoundMiddlewareException
     */
    TransactionStatusTO getPaymentStatusById(String paymentId) throws PaymentNotFoundMiddlewareException;


    //============================ Payment Status ==============================//

    /**
     * Reads and return a payment.
     * 
     * @deprecated: no need for specifying payment product and payment type.
     *
     * @param paymentType
     * @param paymentProduct
     * @param paymentId
     * @return
     * @throws PaymentNotFoundMiddlewareException
    <T> T getPaymentById(PaymentTypeTO paymentType, PaymentProductTO paymentProduct, String paymentId) throws PaymentNotFoundMiddlewareException;
     */

    /**
     * Reads and return a payment.
     *
     * @param paymentType
     * @param paymentProduct
     * @param paymentId
     * @return
     * @throws PaymentNotFoundMiddlewareException
     */
    Object getPaymentById(String paymentId) throws PaymentNotFoundMiddlewareException;
    
    /**
     * Checks the possibility of payment cancellation
     *
     * @param paymentId
     * @return
     * @throws PaymentNotFoundMiddlewareException
     * @throws PaymentProcessingMiddlewareException
     */
    SCAPaymentResponseTO initiatePaymentCancellation(String paymentId) throws PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException;

    /**
     * Cancels payment if possible
     *
     * @param paymentId payment identifier
     * @throws PaymentNotFoundMiddlewareException
     * @throws PaymentProcessingMiddlewareException
    void cancelPayment(String paymentId) throws PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException;
    PaymentKeyDataTO getPaymentKeyDataById(String paymentId) throws PaymentNotFoundMiddlewareException;
     */

	String iban(String paymentId);


	SCAPaymentResponseTO loadSCAForPaymentData(String paymentId, String authorisationId) throws PaymentNotFoundMiddlewareException, SCAOperationExpiredMiddlewareException;

	SCAPaymentResponseTO selectSCAMethodForPayment(String paymentId, String authorisationId, String scaMethodId) throws PaymentNotFoundMiddlewareException, SCAMethodNotSupportedMiddleException, UserScaDataNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException;

	SCAPaymentResponseTO loadSCAForCancelPaymentData(String paymentId, String cancellationId) throws PaymentNotFoundMiddlewareException, SCAOperationExpiredMiddlewareException;

	SCAPaymentResponseTO selectSCAMethodForCancelPayment(String paymentId, String cancellationId, String scaMethodId) throws PaymentNotFoundMiddlewareException, SCAMethodNotSupportedMiddleException, UserScaDataNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException;

	SCAPaymentResponseTO authorizeCancelPayment(String paymentId, String cancellationId, String authCode) throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, PaymentNotFoundMiddlewareException;
	
}
