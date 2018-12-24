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

import static de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO.ACSC;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTargetBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentCancellationResponseTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentKeyDataTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.AuthCodeDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountMiddlewareUncheckedException;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AuthCodeGenerationMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.NoAccessMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAMethodNotSupportedMiddleException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationExpiredMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationUsedOrStolenMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationValidationMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserScaDataNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareService;
import de.adorsys.ledgers.middleware.impl.converter.AuthCodeDataConverter;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.exception.AuthCodeGenerationException;
import de.adorsys.ledgers.sca.exception.SCAMethodNotSupportedException;
import de.adorsys.ledgers.sca.exception.SCAOperationExpiredException;
import de.adorsys.ledgers.sca.exception.SCAOperationNotFoundException;
import de.adorsys.ledgers.sca.exception.SCAOperationUsedOrStolenException;
import de.adorsys.ledgers.sca.exception.SCAOperationValidationException;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.api.exception.InsufficientPermissionException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.exception.UserScaDataNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;

@Service
public class MiddlewareServiceImpl implements MiddlewareService {
	private static final Logger logger = LoggerFactory.getLogger(MiddlewareServiceImpl.class);
	private static final String NOT_THIS_USERS_ACCOUNT_MSG = "Account: %s doesn't belong to current User";
	private static final String UTF_8 = "UTF-8";

	private final DepositAccountPaymentService paymentService;
	private final SCAOperationService scaOperationService;
	private final DepositAccountService accountService;
	private final PaymentConverter paymentConverter;
	private final AuthCodeDataConverter authCodeDataConverter;
	private final MiddlewareUserManagementServiceImpl userManagementService;
	private final AccessTokenTO accessTokenTO;
	private final Principal principal;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	private final BearerTokenMapper bearerTokenMapper;
	private final UserService userService;

	public MiddlewareServiceImpl(DepositAccountPaymentService paymentService, SCAOperationService scaOperationService,
			DepositAccountService accountService, PaymentConverter paymentConverter,
			AuthCodeDataConverter authCodeDataConverter, MiddlewareUserManagementServiceImpl userManagementService,
			AccessTokenTO accessTokenTO, Principal principal, BearerTokenMapper bearerTokenMapper,
			UserService userService) {
		super();
		this.paymentService = paymentService;
		this.scaOperationService = scaOperationService;
		this.accountService = accountService;
		this.paymentConverter = paymentConverter;
		this.authCodeDataConverter = authCodeDataConverter;
		this.userManagementService = userManagementService;
		this.accessTokenTO = accessTokenTO;
		this.principal = principal;
		this.bearerTokenMapper = bearerTokenMapper;
		this.userService = userService;
	}

	@Override
	public TransactionStatusTO getPaymentStatusById(String paymentId) throws PaymentNotFoundMiddlewareException {
		try {
			TransactionStatusBO paymentStatus = paymentService.getPaymentStatusById(paymentId);
			return TransactionStatusTO.valueOf(paymentStatus.name());
		} catch (PaymentNotFoundException e) {
			logger.error("Payment with id=" + paymentId + " not found", e);
			throw new PaymentNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public PaymentCancellationResponseTO initiatePaymentCancellation(String psuId, String paymentId)
			throws UserNotFoundMiddlewareException, PaymentNotFoundMiddlewareException,
			PaymentProcessingMiddlewareException {
		UserTO user = userManagementService.findById(psuId);
		try {
			PaymentBO payment = paymentService.getPaymentById(paymentId);
			Optional<AccountAccessTO> userAccountRelatedToPayment = user.getAccountAccesses().stream()
					.filter(a -> a.getIban().equals(payment.getDebtorAccount().getIban())).findFirst();
			userAccountRelatedToPayment.orElseThrow(() -> new PaymentNotFoundException(paymentId));

			TransactionStatusTO status = TransactionStatusTO.valueOf(payment.getTransactionStatus().name());
			if (status == ACSC) {
				throw new PaymentProcessingMiddlewareException(String.format(
						"Request for payment cancellation is forbidden as the payment with id:%s has status:%s",
						paymentId, status));
			}
			boolean scaRequired = !user.getScaUserData().isEmpty();
			return new PaymentCancellationResponseTO(scaRequired, status);
		} catch (PaymentNotFoundException e) {
			logger.error(String.format("Payment with id= %s, not found", paymentId), e);
			throw new PaymentNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public void cancelPayment(String paymentId)
			throws PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException {
		try {
			paymentService.cancelPayment(paymentId);
		} catch (PaymentNotFoundException e) {
			logger.error(String.format("Payment with id= %s, not found", paymentId), e);
			throw new PaymentNotFoundMiddlewareException(e.getMessage(), e);
		} catch (PaymentProcessingException e) {
			logger.error(e.getMessage(), e);
			throw new PaymentProcessingMiddlewareException(e.getMessage());
		}
	}

	@Override
	@SuppressWarnings("PMD.IdenticalCatchBranches")
	public String generateAuthCode(AuthCodeDataTO authCodeData)
			throws AuthCodeGenerationMiddlewareException, SCAMethodNotSupportedMiddleException,
			UserNotFoundMiddlewareException, UserScaDataNotFoundMiddlewareException {
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
	public boolean validateAuthCode(String opId, String opData, String authCode) throws // NOPMD
	SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
			SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException,
			PaymentNotFoundMiddlewareException {
		try {
			boolean isValidated = scaOperationService.validateAuthCode(opId, opData, authCode);
			if (opData.contains("Payment")) { // TODO @fpo this is implemented as is without any info please clarify
				paymentService.updatePaymentStatusToAuthorised(opId);
			}
			return isValidated;
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
		} catch (PaymentNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new PaymentNotFoundMiddlewareException(e.getMessage());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Object initiatePayment(T payment, PaymentTypeTO paymentType)
			throws AccountNotFoundMiddlewareException, NoAccessMiddlewareException {
		PaymentBO paymentBO = paymentConverter.toPaymentBO(payment, paymentType.getPaymentClass());
		UserTO user = null;
		try {
			user = userManagementService.findByUserLogin(accessTokenTO.getActor());
			boolean isUsersAccount = user.getAccountAccesses().stream()
					.anyMatch(a -> a.getIban().equals(paymentBO.getDebtorAccount().getIban()));
			if (!isUsersAccount) {
				throw new NoAccessMiddlewareException(
						String.format(NOT_THIS_USERS_ACCOUNT_MSG, paymentBO.getDebtorAccount().getIban()));
			}
			accountService.getDepositAccountByIban(paymentBO.getDebtorAccount().getIban(), LocalDateTime.now(), false);

		} catch (DepositAccountNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new AccountNotFoundMiddlewareException(e.getMessage());
		} catch (UserNotFoundMiddlewareException e) {
			logger.error(e.getMessage());
			throw new NoAccessMiddlewareException(e.getMessage());
		}
		TransactionStatusBO status = !user.getScaUserData().isEmpty() ? TransactionStatusBO.ACCP
				: TransactionStatusBO.ACTC;
		PaymentBO paymentInitiationResult = paymentService.initiatePayment(paymentBO, status);
		return paymentConverter.toPaymentTO(paymentInitiationResult);
	}

	@Override
	public TransactionStatusTO executePayment(String paymentId) throws PaymentProcessingMiddlewareException {
		try {
			// TODO fill in AUTH CHECK with USER ROLE CHECK @dmiex
			TransactionStatusBO executePayment = paymentService.executePayment(paymentId, principal.getName());
			return TransactionStatusTO.valueOf(executePayment.name());
		} catch (PaymentNotFoundException | PaymentProcessingException e) {
			throw new PaymentProcessingMiddlewareException(paymentId, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getPaymentById(PaymentTypeTO paymentType, PaymentProductTO paymentProduct, String paymentId)
			throws PaymentNotFoundMiddlewareException {
		try {
			PaymentBO paymentResult = paymentService.getPaymentById(paymentId);
			return paymentConverter.toPaymentTO(paymentResult);
		} catch (PaymentNotFoundException e) {
			logger.error(String.format("Payment with id= %s, not found", paymentId), e);
			throw new PaymentNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	public static final DecimalFormat decimalFormat = new DecimalFormat("###,###.##");

	private String formatAmount(BigDecimal amount) {
		return decimalFormat.format(amount);
	}

	@Override
	public PaymentKeyDataTO getPaymentKeyDataById(String paymentId) throws PaymentNotFoundMiddlewareException {
		try {
			PaymentBO r = paymentService.getPaymentById(paymentId);
			return getPaymentKeyDataById(r);
		} catch (PaymentNotFoundException e) {
			logger.error(String.format("Payment with id= %s, not found", paymentId), e);
			throw new PaymentNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public String iban(String paymentId) {
		return paymentService.readIbanByPaymentId(paymentId);
	}

	@Override
	public BearerTokenTO authorizePayment(String paymentId, String opId, String authCode)
			throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
			SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException {
		PaymentBO r;
		try {
			r = paymentService.getPaymentById(paymentId);
			PaymentKeyDataTO paymentKeyData = getPaymentKeyDataById(r);
			boolean isValidated = scaOperationService.validateAuthCode(opId, paymentKeyData.template(), authCode);
			if(!isValidated) {
				// TODO number of failed attempt.
				throw new SCAOperationValidationMiddlewareException("Wrong auth code");
			}
			paymentService.updatePaymentStatusToAuthorised(paymentId);
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
		} catch (PaymentNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
		}

		String iban = r.getDebtorAccount().getIban();
		// Returned token can be used to access status.
		try {
			AisConsentBO aisConsent = new AisConsentBO();
			AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
			aisConsent.setAccess(access);
			List<String> asList = Arrays.asList(iban);
			access.setAccounts(asList);
			access.setTransactions(asList);
			access.setBalances(asList);
			aisConsent.setFrequencyPerDay(0);
			aisConsent.setRecurringIndicator(true);
			aisConsent.setUserId(accessTokenTO.getActor());
			return bearerTokenMapper.toBearerTokenTO(userService.grant(aisConsent));
		} catch (InsufficientPermissionException e) {
			throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
		}
	}

	private PaymentKeyDataTO getPaymentKeyDataById(PaymentBO r) {
		try {
			PaymentKeyDataTO p = new PaymentKeyDataTO();
			p.setPaymentId(r.getPaymentId());
			if (r.getTargets().size() == 1) {// Single, Periodic, Future Dated
				PaymentTargetBO t = r.getTargets().iterator().next();
				p.setCreditorIban(t.getCreditorAccount().getIban());
				p.setCreditorName(t.getCreditorName());
				p.setCurrency(t.getInstructedAmount().getCurrency().getCurrencyCode());
				p.setAmount(formatAmount(t.getInstructedAmount().getAmount()));
			} else {
				List<PaymentTargetBO> targets = r.getTargets();
				// Bulk
				p.setPaymentsSize("" + targets.size());
				p.setCreditorName("Many Receipients");
				// Hash of all receiving Iban
				MessageDigest md = MessageDigest.getInstance("MD5");
				BigDecimal amt = BigDecimal.ZERO;
				for (PaymentTargetBO t : targets) {
					if (p.getCurrency() != null
							&& p.getCurrency().equals(t.getInstructedAmount().getCurrency().getCurrencyCode())) {
						throw new AccountMiddlewareUncheckedException(
								String.format("Currency mismatched in bulk payment with id %s", r.getPaymentId()));
					}
					p.setCurrency(t.getInstructedAmount().getCurrency().getCurrencyCode());
					md.update(t.getCreditorAccount().getIban().getBytes(UTF_8));
					amt.add(t.getInstructedAmount().getAmount());
				}
				p.setAmount(formatAmount(amt));
				p.setCreditorIban(DatatypeConverter.printHexBinary(md.digest()));
			}

			if (r.getRequestedExecutionDate() != null) {
				p.setRequestedExecutionDate(r.getRequestedExecutionDate().format(formatter));
			}
			if (PaymentTypeBO.PERIODIC.equals(r.getPaymentType())) {
				p.setDayOfExecution("" + r.getDayOfExecution());
				p.setExecutionRule(r.getExecutionRule());
				p.setFrequency("" + r.getFrequency());
			}
			return p;
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
		}
	}

}
