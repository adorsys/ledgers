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

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;

import java.util.List;

public interface DepositAccountPaymentService {

    PaymentResultBO<TransactionStatusBO> getPaymentStatusById(String paymentId) throws PaymentNotFoundException;

    PaymentBO getPaymentById(PaymentTypeBO paymentType, PaymentProductBO paymentProduct, String paymentId) throws PaymentNotFoundException;

    PaymentBO initiatePayment(PaymentBO paymentBO);

    List<TransactionDetailsBO> executePayment(String paymentId, PaymentTypeBO paymentType, PaymentProductBO paymentProduct) throws PaymentNotFoundException, PaymentProcessingException;
}
