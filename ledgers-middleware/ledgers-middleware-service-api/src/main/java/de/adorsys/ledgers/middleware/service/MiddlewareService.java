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

import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentResultTO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.service.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.service.domain.sca.SCAMethodTO;
import de.adorsys.ledgers.middleware.service.exception.*;

import java.util.List;

public interface MiddlewareService {

    PaymentResultTO<TransactionStatusTO> getPaymentStatusById(String paymentId) throws PaymentNotFoundMiddlewareException;

    /**
     * @param userLogin       user login
     * @param scaMethod       sca method
     * @param opData          operation data
     * @param validitySeconds time to live in seconds
     * @param userMessage     what would be show to user
     * @return opId id of operation created on the request
     * @throws AuthCodeGenerationMiddlewareException if something happens during auth code generation
     */
    String generateAuthCode(String userLogin, SCAMethodTO scaMethod, String opData, String userMessage, int validitySeconds) throws AuthCodeGenerationMiddlewareException, SCAMethodNotSupportedMiddleException;

    boolean validateAuthCode(String opId, String opData, String authCode) throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException;

    AccountDetailsTO getAccountDetailsByAccountId(String accountId) throws AccountNotFoundMiddlewareException;

    <T> T getPaymentById(PaymentTypeTO paymentType, PaymentProductTO paymentProduct, String paymentId) throws PaymentNotFoundMiddlewareException;

    <T> Object initiatePayment(T payment, PaymentTypeTO paymentType);

    <T> List<TransactionTO> executePayment(String paymentId, PaymentTypeTO paymentType, PaymentProductTO paymentProduct) throws PaymentProcessingMiddlewareException;

    List<SCAMethodTO> getSCAMethods(String userLogin) throws UserNotFoundMiddlewareException;
}
