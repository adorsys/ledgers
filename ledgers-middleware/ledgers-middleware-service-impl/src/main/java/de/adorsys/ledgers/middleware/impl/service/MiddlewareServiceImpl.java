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

package de.adorsys.ledgers.middleware.impl.service;


import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.AuthCodeDataTO;
import de.adorsys.ledgers.middleware.api.exception.*;
import de.adorsys.ledgers.middleware.api.service.MiddlewareService;
import de.adorsys.ledgers.middleware.impl.converter.AuthCodeDataConverter;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.exception.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.exception.UserScaDataNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MiddlewareServiceImpl implements MiddlewareService {
    private static final Logger logger = LoggerFactory.getLogger(MiddlewareServiceImpl.class);

    private final DepositAccountPaymentService paymentService;
    private final SCAOperationService scaOperationService;
    private final DepositAccountService accountService;
    private final PaymentConverter paymentConverter;
    private final AuthCodeDataConverter authCodeDataConverter;

    public MiddlewareServiceImpl(DepositAccountPaymentService paymentService, SCAOperationService scaOperationService,
                                 DepositAccountService accountService, PaymentConverter paymentConverter, AuthCodeDataConverter authCodeDataConverter) {
        super();
        this.paymentService = paymentService;
        this.scaOperationService = scaOperationService;
        this.accountService = accountService;
        this.paymentConverter = paymentConverter;
        this.authCodeDataConverter = authCodeDataConverter;
    }

    @Override
    public TransactionStatusTO getPaymentStatusById(String paymentId) throws
            PaymentNotFoundMiddlewareException {
        try {
            TransactionStatusBO paymentStatus = paymentService.getPaymentStatusById(paymentId);
            return TransactionStatusTO.valueOf(paymentStatus.name());
        } catch (PaymentNotFoundException e) {
            logger.error("Payment with id=" + paymentId + " not found", e);
            throw new PaymentNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("PMD.IdenticalCatchBranches")
    public String generateAuthCode(AuthCodeDataTO authCodeData) throws AuthCodeGenerationMiddlewareException, SCAMethodNotSupportedMiddleException, UserNotFoundMiddlewareException, UserScaDataNotFoundMiddlewareException {
        try {
            AuthCodeDataBO authCodeDataBO = authCodeDataConverter.toAuthCodeDataBO(authCodeData);
            return scaOperationService.generateAuthCode(authCodeDataBO);
        } catch (AuthCodeGenerationException e) {
            logger.error(e.getMessage(), e);
            throw new AuthCodeGenerationMiddlewareException(e);
        } catch (SCAMethodNotSupportedException e) {
            logger.error(e.getMessage(), e);
            throw new SCAMethodNotSupportedMiddleException(e);
        } catch (UserNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new UserNotFoundMiddlewareException(e);
        } catch (UserScaDataNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new UserScaDataNotFoundMiddlewareException(e);
        }
    }

    @Override
    @SuppressWarnings("PMD.IdenticalCatchBranches")
    public boolean validateAuthCode(String opId, String opData, String authCode) throws
            SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException {
        try {
            return scaOperationService.validateAuthCode(opId, opData, authCode);
        } catch (SCAOperationNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new SCAOperationNotFoundMiddlewareException(e);
        } catch (SCAOperationValidationException e) {
            logger.error(e.getMessage(), e);
            throw new SCAOperationValidationMiddlewareException(e);
        } catch (SCAOperationExpiredException e) {
            logger.error(e.getMessage(), e);
            throw new SCAOperationExpiredMiddlewareException(e);
        } catch (SCAOperationUsedOrStolenException e) {
            logger.error(e.getMessage(), e);
            throw new SCAOperationUsedOrStolenMiddlewareException(e);
        }
    }

    @Override
    public <T> Object initiatePayment(T payment, PaymentTypeTO paymentType) throws AccountNotFoundMiddlewareException {
        @SuppressWarnings("unchecked")
        PaymentBO paymentBO = paymentConverter.toPaymentBO(payment, paymentType.getPaymentClass());
        try {
            accountService.getDepositAccountByIban(paymentBO.getDebtorAccount().getIban(), LocalDateTime.now(), false);

        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new AccountNotFoundMiddlewareException(e.getMessage());
        }
        PaymentBO paymentInitiationResult = paymentService.initiatePayment(paymentBO);
        return paymentConverter.toPaymentTO(paymentInitiationResult);
    }

    @Override
    public TransactionStatusTO executePayment(String paymentId) throws PaymentProcessingMiddlewareException {
        try {
            TransactionStatusBO executePayment = paymentService.executePayment(paymentId);
            return TransactionStatusTO.valueOf(executePayment.name());
        } catch (PaymentNotFoundException | PaymentProcessingException e) {
            throw new PaymentProcessingMiddlewareException(paymentId, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override //TODO Consider refactoring to avoid unchecked cast warnings
    public Object getPaymentById(PaymentTypeTO paymentType, PaymentProductTO paymentProduct, String paymentId) throws PaymentNotFoundMiddlewareException {
        try {
            PaymentBO paymentResult = paymentService.getPaymentById(paymentId);
            return paymentConverter.toPaymentTO(paymentResult);
        } catch (PaymentNotFoundException e) {
            logger.error("Payment with id={} not found", paymentId, e);
            throw new PaymentNotFoundMiddlewareException(e.getMessage(), e);
        }
    }
}
