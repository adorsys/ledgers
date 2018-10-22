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

package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.PaymentResultBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DepositAccountPaymentServiceImpl implements DepositAccountPaymentService {

    public DepositAccountPaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentResultBO<TransactionStatusBO> getPaymentStatusById(String paymentId) throws PaymentNotFoundException {
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        TransactionStatus transactionStatus = payment
                                                      .map(Payment::getTransactionStatus)
                                                      .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        return new PaymentResultBO<>(TransactionStatusBO.valueOf(transactionStatus.name()));
    }
}
