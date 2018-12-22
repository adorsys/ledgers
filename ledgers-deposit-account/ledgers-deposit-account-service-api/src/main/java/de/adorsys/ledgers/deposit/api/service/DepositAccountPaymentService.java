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

package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;

public interface DepositAccountPaymentService {

    TransactionStatusBO getPaymentStatusById(String paymentId) throws PaymentNotFoundException;

    PaymentBO getPaymentById(String paymentId) throws PaymentNotFoundException;

    PaymentBO initiatePayment(PaymentBO paymentBO, TransactionStatusBO status);

    TransactionStatusBO executePayment(String paymentId, String userName) throws PaymentNotFoundException, PaymentProcessingException;

    void cancelPayment(String paymentId) throws PaymentNotFoundException;

    String readIbanByPaymentId(String paymentId);

    void updatePaymentStatusToAuthorised(String paymentId) throws PaymentNotFoundException;
}
