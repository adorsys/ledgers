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

import de.adorsys.ledgers.deposit.api.domain.AccountReferenceBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;

import java.util.List;

public interface DepositAccountPaymentService {

    TransactionStatusBO getPaymentStatusById(String paymentId);

    PaymentBO getPaymentById(String paymentId);

    PaymentBO initiatePayment(PaymentBO paymentBO, TransactionStatusBO status);

    TransactionStatusBO executePayment(String paymentId, String userName);

    TransactionStatusBO cancelPayment(String paymentId);

    String readIbanByPaymentId(String paymentId);

    TransactionStatusBO updatePaymentStatus(String paymentId, TransactionStatusBO status);

    List<PaymentBO> getPaymentsByTypeStatusAndDebtor(PaymentTypeBO paymentType, TransactionStatusBO status, List<AccountReferenceBO> referenceList);
}
