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


import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentResultBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.converter.AccountConverter;
import de.adorsys.ledgers.middleware.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentResultTO;
import de.adorsys.ledgers.middleware.service.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.service.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.PaymentNotFoundMiddlewareException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MiddlewareServiceImpl implements MiddlewareService {
    private static final Logger logger = LoggerFactory.getLogger(MiddlewareServiceImpl.class);

    private final DepositAccountPaymentService paymentService;

    private final DepositAccountService accountService;

    private final PaymentConverter paymentConverter;

    private final AccountConverter accountConverter;

    public MiddlewareServiceImpl(DepositAccountPaymentService paymentService, DepositAccountService depositAccountService, PaymentConverter paymentConverter, AccountConverter accountConverter) {
        this.paymentService = paymentService;
        this.accountService = depositAccountService;
        this.paymentConverter = paymentConverter;
        this.accountConverter = accountConverter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PaymentResultTO<TransactionStatusTO> getPaymentStatusById(String paymentId) throws PaymentNotFoundMiddlewareException {
        try {
            PaymentResultBO<TransactionStatusBO> paymentStatus = paymentService.getPaymentStatusById(paymentId);
            return paymentConverter.toPaymentResultTO(paymentStatus);
        } catch (PaymentNotFoundException e) {
            logger.error("Payment with id=" + paymentId + " not found", e);
            throw new PaymentNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public AccountDetailsTO getAccountDetailsByAccountId(String accountId) throws AccountNotFoundMiddlewareException {
        try {
            DepositAccountBO account = accountService.getDepositAccountById(accountId);
            return accountConverter.toAccountDetailsTO(account, null); //TODO add real balances call and mapping
        } catch (DepositAccountNotFoundException e) {
            logger.error("Deposit Account with id=" + accountId + "not found", e);
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }
}
