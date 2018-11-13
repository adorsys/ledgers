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

package de.adorsys.ledgers.middleware.service;

import de.adorsys.ledgers.middleware.service.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.service.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.service.domain.sca.SCAMethodTO;
import de.adorsys.ledgers.middleware.service.exception.*;

import java.time.LocalDate;
import java.util.List;

public interface MiddlewareService {

    //================= PAYMENT INITIATION ========================//

    /**
     * PROC:01 Initiates a payment. Called by the channel layer.
     * <p>
     * This call sets the status RCVD
     *
     * @param payment
     * @param paymentType
     * @return
     */
    <T> Object initiatePayment(T payment, PaymentTypeTO paymentType) throws AccountNotFoundMiddlewareException;


    // ================= SCA =======================================//

    /**
     * PROC: 02a
     * <p>
     * Called after the payment initiation to have a list of SCA methods.
     *
     * @param userLogin
     * @return
     * @throws UserNotFoundMiddlewareException
     */
    List<SCAMethodTO> getSCAMethods(String userLogin) throws UserNotFoundMiddlewareException;

    /**
     * PROC: 02b
     * <p>
     * After the PSU selects the SCA method, this is called to generate and send the auth code.
     *
     * @param userLogin       user login
     * @param scaMethod       sca method
     * @param opData          operation data
     * @param validitySeconds time to live in seconds
     * @param userMessage     what would be show to user
     * @return opId id of operation created on the request
     * @throws AuthCodeGenerationMiddlewareException if something happens during auth code generation
     */
    String generateAuthCode(String userLogin, SCAMethodTO scaMethod, String opData, String userMessage, int validitySeconds) throws AuthCodeGenerationMiddlewareException, SCAMethodNotSupportedMiddleException;

    /**
     * PROC: 02c
     * <p>
     * This is called when the user enters the received code.
     *
     * @param opId
     * @param opData
     * @param authCode
     * @return
     * @throws SCAOperationNotFoundMiddlewareException
     * @throws SCAOperationValidationMiddlewareException
     * @throws SCAOperationExpiredMiddlewareException
     * @throws SCAOperationUsedOrStolenMiddlewareException
     */
    boolean validateAuthCode(String opId, String opData, String authCode) throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException;

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
     */
    TransactionStatusTO executePayment(String paymentId) throws PaymentProcessingMiddlewareException;


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
     * @param paymentType
     * @param paymentProduct
     * @param paymentId
     * @return
     * @throws PaymentNotFoundMiddlewareException
     */
    <T> T getPaymentById(PaymentTypeTO paymentType, PaymentProductTO paymentProduct, String paymentId) throws PaymentNotFoundMiddlewareException;


    //============================ Account Details ==============================//

    AccountDetailsTO getAccountDetailsByAccountId(String accountId) throws AccountNotFoundMiddlewareException;

    List<AccountBalanceTO> getBalances(String accountId) throws AccountNotFoundMiddlewareException;

    List<AccountDetailsTO> getAllAccountDetailsByUserLogin(String userLogin) throws UserNotFoundMiddlewareException;

    void updateScaMethods(List<SCAMethodTO> scaMethods, String userLogin) throws UserNotFoundMiddlewareException;

    TransactionTO getTransactionById(String accountId, String transactionId) throws AccountNotFoundMiddlewareException, TransactionNotFoundMiddlewareException;

    List<TransactionTO> getTransactionsByDates(String accountId, LocalDate dateFrom, LocalDate dateTo) throws AccountNotFoundMiddlewareException;
}
