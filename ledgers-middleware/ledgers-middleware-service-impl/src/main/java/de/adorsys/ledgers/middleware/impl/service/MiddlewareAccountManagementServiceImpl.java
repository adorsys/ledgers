package de.adorsys.ledgers.middleware.impl.service;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.FundsConfirmationRequestBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.TransactionNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.ConsentKeyDataTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountMiddlewareUncheckedException;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithPrefixGoneMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithSuffixExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AisConsentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAMethodNotSupportedMiddleException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationExpiredMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationUsedOrStolenMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationValidationMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserScaDataNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.impl.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.impl.converter.AisConsentBOMapper;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.exception.SCAMethodNotSupportedException;
import de.adorsys.ledgers.sca.exception.SCAOperationExpiredException;
import de.adorsys.ledgers.sca.exception.SCAOperationNotFoundException;
import de.adorsys.ledgers.sca.exception.SCAOperationUsedOrStolenException;
import de.adorsys.ledgers.sca.exception.SCAOperationValidationException;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.ConsentNotFoundException;
import de.adorsys.ledgers.um.api.exception.InsufficientPermissionException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.exception.UserScaDataNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;

@Service
public class MiddlewareAccountManagementServiceImpl implements MiddlewareAccountManagementService {
    private static final Logger logger = LoggerFactory.getLogger(MiddlewareAccountManagementServiceImpl.class);
    private static final LocalDateTime BASE_TIME = LocalDateTime.MIN; //TODO @fpo why we use minimal possible time value?

    private final DepositAccountService depositAccountService;
    private final AccountDetailsMapper accountDetailsMapper;
    private final PaymentConverter paymentConverter;
    private final UserService userService;
    private final UserMapper userMapper;
    private final AisConsentBOMapper aisConsentMapper;
    private final BearerTokenMapper bearerTokenMapper;
    
    private final AccessTokenTO accessToken;
    private final Principal principal;
	private final SCAOperationService scaOperationService;
	private final SCAUtils scaUtils;
	private final AccessService accessService;

	public MiddlewareAccountManagementServiceImpl(DepositAccountService depositAccountService,
			AccountDetailsMapper accountDetailsMapper, PaymentConverter paymentConverter, UserService userService,
			UserMapper userMapper, AisConsentBOMapper aisConsentMapper, BearerTokenMapper bearerTokenMapper,
			AccessTokenTO accessToken, Principal principal, SCAOperationService scaOperationService, SCAUtils scaUtils,
			AccessService accessService) {
		super();
		this.depositAccountService = depositAccountService;
		this.accountDetailsMapper = accountDetailsMapper;
		this.paymentConverter = paymentConverter;
		this.userService = userService;
		this.userMapper = userMapper;
		this.aisConsentMapper = aisConsentMapper;
		this.bearerTokenMapper = bearerTokenMapper;
		this.accessToken = accessToken;
		this.principal = principal;
		this.scaOperationService = scaOperationService;
		this.scaUtils = scaUtils;
		this.accessService = accessService;
	}

	@Override
    public void createDepositAccount(AccountDetailsTO depositAccount)
            throws UserNotFoundMiddlewareException {
        createDepositAccount(depositAccount, Collections.emptyList());
    }

    @Override
    public void createDepositAccount(AccountDetailsTO depositAccount, List<AccountAccessTO> accountAccesss)
            throws UserNotFoundMiddlewareException {
        try {
            Map<String, UserBO> persistBuffer = new HashMap<>();

            DepositAccountBO depositAccountBO = depositAccountService.createDepositAccount(accountDetailsMapper.toDepositAccountBO(depositAccount), principal.getName());
            if (accountAccesss != null) {
            	accessService.addAccess(accountAccesss, depositAccountBO, persistBuffer);
            }
        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundMiddlewareException(e.getMessage(), e);
        }
    }
    @Override
    public AccountDetailsTO getDepositAccountById(String accountId, LocalDateTime time, boolean withBalance) throws
            AccountNotFoundMiddlewareException {
        try {
            DepositAccountDetailsBO accountDetailsBO = depositAccountService.getDepositAccountById(accountId, time, true);
            return accountDetailsMapper.toAccountDetailsTO(accountDetailsBO);
        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public AccountDetailsTO getDepositAccountByIban(String iban, LocalDateTime time, boolean withBalance) throws
            AccountNotFoundMiddlewareException {
        try {
            DepositAccountDetailsBO depositAccountBO = depositAccountService.getDepositAccountByIban(iban, time, withBalance);
            return accountDetailsMapper.toAccountDetailsTO(depositAccountBO);
        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public List<AccountDetailsTO> getAllAccountDetailsByUserLogin(String userLogin) throws
            UserNotFoundMiddlewareException, AccountNotFoundMiddlewareException {
        logger.info("Retrieving accounts by user login {}", userLogin);
        try {
            UserBO userBO = userService.findByLogin(userLogin);
            List<AccountAccessBO> accountAccess = userBO.getAccountAccesses();
            logger.info("{} accounts were retrieved", accountAccess.size());

            List<String> ibans = accountAccess.stream()
                                         .filter(a -> a.getAccessType() == AccessTypeBO.OWNER)
                                         .map(AccountAccessBO::getIban)
                                         .collect(Collectors.toList());
            logger.info("{} were accounts were filtered as OWN", ibans.size());

            List<DepositAccountDetailsBO> depositAccounts = depositAccountService.getDepositAccountsByIban(ibans, BASE_TIME, false);
            logger.info("{} deposit accounts were found", depositAccounts.size());

            return depositAccounts.stream()
                           .map(accountDetailsMapper::toAccountDetailsTO)
                           .collect(Collectors.toList());
        } catch (UserNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new UserNotFoundMiddlewareException(e.getMessage());
        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public TransactionTO getTransactionById(String accountId, String transactionId) throws
            TransactionNotFoundMiddlewareException {
        try {
            TransactionDetailsBO transaction = depositAccountService.getTransactionById(accountId, transactionId);
            return paymentConverter.toTransactionTO(transaction);
        } catch (TransactionNotFoundException e) {
            throw new TransactionNotFoundMiddlewareException(e.getMessage(), e);
        }

    }

    @Override
    public List<TransactionTO> getTransactionsByDates(String accountId, LocalDate dateFrom, LocalDate dateTo) throws
            AccountNotFoundMiddlewareException {
        LocalDate today = LocalDate.now();
        LocalDateTime dateTimeFrom = dateFrom == null
                                             ? today.atStartOfDay()
                                             : dateFrom.atStartOfDay();
        LocalDateTime dateTimeTo = dateTo == null
                                           ? accessService.getTimeAtEndOfTheDay(today)
                                           : accessService.getTimeAtEndOfTheDay(dateTo);
        try {
            List<TransactionDetailsBO> transactions = depositAccountService.getTransactionsByDates(accountId, dateTimeFrom, dateTimeTo);
            return paymentConverter.toTransactionTOList(transactions);
        } catch (DepositAccountNotFoundException e) {
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public boolean confirmFundsAvailability(FundsConfirmationRequestTO request) throws
            AccountNotFoundMiddlewareException {
        try {
            FundsConfirmationRequestBO requestBO = accountDetailsMapper.toFundsConfirmationRequestBO(request);
            return depositAccountService.confirmationOfFunds(requestBO);
        } catch (DepositAccountNotFoundException e) {
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

	@Override
	public void createDepositAccount(String accountNumberPrefix, String accountNumberSuffix, AccountDetailsTO accDetails)
			throws AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException {
		
		String accNbr = accountNumberPrefix+accountNumberSuffix;

		// if the list is not empty, we mus make sure that account belong to the current user.s
		List<DepositAccountBO> accounts = depositAccountService.findByAccountNumberPrefix(accountNumberPrefix);

		validateInput(accounts, accountNumberPrefix, accountNumberSuffix);

		accDetails.setIban(accNbr);
		
		List<AccountAccessTO> accountAccesses = new ArrayList<>();
		// if caller is a customer
		if(accessToken.getRole()==UserRoleTO.CUSTOMER) {
			// then make him owner of the account.
			UserTO userTO = new UserTO();
			userTO.setId(accessToken.getSub());
			userTO.setLogin(accessToken.getActor());
	        accountAccesses.add(accessService.createAccountAccess(accNbr, userTO));
		}
		try {
			createDepositAccount(accDetails, accountAccesses);
		} catch (UserNotFoundMiddlewareException e) {
			throw new AccountMiddlewareUncheckedException(String.format("Can not find user with id %s and login", accessToken.getSub(), accessToken.getActor()));
		}
		
	}

	// Validate that
	private void validateInput(List<DepositAccountBO> accounts, String accountNumberPrefix, String accountNumberSuffix) throws AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException {
		// This prefix is still free
		if(accounts.isEmpty()) { 
			return;
		}
		
		// XOR The user is the owner of this prefix
		List<AccountAccessTO> accountAccesses = accessToken.getAccountAccesses();
		
		// EMpty if user is not owner of this prefix.
		if(accountAccesses.isEmpty()) {
			// User can not own any of those accounts.
			throw new AccountWithPrefixGoneMiddlewareException(String.format("Account prefix %s is gone.", accountNumberPrefix));
		}
		
		List<String> ownedAccounts = accessService.filterOwnedAccounts(accountAccesses);
		
		// user already has account with this prefix and suffix
		String accNbr = accountNumberPrefix+accountNumberSuffix;
		if(ownedAccounts.contains(accNbr)) {
			throw new AccountWithSuffixExistsMiddlewareException(String.format("Account with suffix %S and prefix %s already exist", accountNumberPrefix, accountNumberSuffix));
		}
		
		// All accounts with this prefix must be owned by this user.
		for (DepositAccountBO a : accounts) {
			if(ownedAccounts.contains(a.getIban())) {
				throw new AccountWithSuffixExistsMiddlewareException(String.format("User not owner of account with iban %s that also holds the requested prefix %s", a.getIban(),accountNumberPrefix));
			}
		}
	}

	@Override
	public void grantAccessToDepositAccount(AccountAccessTO accountAccess)
			throws AccountNotFoundMiddlewareException, InsufficientPermissionMiddlewareException {
        UserBO userBo = accessService.loadCurrentUser();
        List<AccountAccessTO> accountAccesses = accessToken.getAccountAccesses();

        // Check that current user owns the account.
        List<String> ownedAccounts = accessService.filterOwnedAccounts(accountAccesses);

        if (!ownedAccounts.contains(accountAccess.getIban())) {
            throw new InsufficientPermissionMiddlewareException(userBo.getId(), userBo.getLogin(), accountAccess.getIban());
        }

        accessService.addAccess(userBo, userMapper.toAccountAccessBO(accountAccess), new HashMap<>());
    }

    @Override
    public List<AccountDetailsTO> listOfDepositAccounts() {
    	UserBO user = accessService.loadCurrentUser();
    	UserTO userTO = userMapper.toUserTO(user);
    	List<AccountAccessTO> accountAccesses = userTO.getAccountAccesses();
//        List<AccountAccessTO> accountAccesses = accessToken.getAccountAccesses();
        if (accountAccesses == null || accountAccesses.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> ibans = accountAccesses.stream()
                                     .map(AccountAccessTO::getIban)
                                     .collect(Collectors.toList());
        List<DepositAccountDetailsBO> depositAccounts;
        try {
            depositAccounts = depositAccountService.getDepositAccountsByIban(ibans, LocalDateTime.now(), true);
        } catch (DepositAccountNotFoundException e) {
            throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
        }
        return depositAccounts.stream()
                       .map(accountDetailsMapper::toAccountDetailsTO)
                       .collect(Collectors.toList());
    }

    @Override
    public String iban(String id) {
        return depositAccountService.readIbanById(id);
    }

    // ======================= CONSENT ======================//

    /*
     * Starts the SCA process. Might directly produce the consent token if
     * sca is not needed.
     * 
     * (non-Javadoc)
     * @see de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService#startSCA(java.lang.String, de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO)
     */
	@Override
	public SCAConsentResponseTO startSCA(String consentId, AisConsentTO aisConsent) throws InsufficientPermissionMiddlewareException {
		BearerTokenBO bearerToken = checkAisConsent(aisConsent);
		ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsent);
		SCAConsentResponseTO response = prepareSCA(scaUtils.userBO(), aisConsent, consentKeyData);
		if(ScaStatusTO.EXEMPTED.equals(response.getScaStatus())) {
			response.setBearerToken(bearerTokenMapper.toBearerTokenTO(bearerToken));
		}
		return response;
	}

	@Override
	public SCAConsentResponseTO loadSCAForAisConsent(String consentId, String authorisationId)
			throws SCAOperationExpiredMiddlewareException, AisConsentNotFoundMiddlewareException {
		UserTO user = scaUtils.user();
		AisConsentBO consent = consent(consentId);
		AisConsentTO aisConsentTO = aisConsentMapper.toAisConsentTO(consent);
		ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsentTO);
		SCAOperationBO scaOperationBO = scaUtils.loadAuthCode(authorisationId);
		return toScaConsentResponse(user, consent, consentKeyData.template(), scaOperationBO);
	}

	@Override
	@SuppressWarnings("PMD.IdenticalCatchBranches")
	public SCAConsentResponseTO selectSCAMethodForAisConsent(String consentId, String authorisationId,
			String scaMethodId) throws PaymentNotFoundMiddlewareException, SCAMethodNotSupportedMiddleException,
			UserScaDataNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
			SCAOperationNotFoundMiddlewareException, AisConsentNotFoundMiddlewareException {
		UserBO userBO = scaUtils.userBO();
		UserTO userTO = scaUtils.user(userBO);
		AisConsentBO consent = consent(consentId);
		AisConsentTO aisConsentTO = aisConsentMapper.toAisConsentTO(consent);
		ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsentTO);
		AuthCodeDataBO a = authCodeData(consentId, userBO, consentKeyData.template(), authorisationId, scaMethodId);
		try {
			SCAOperationBO scaOperationBO = scaOperationService.generateAuthCode(a, userBO, ScaStatusBO.SCAMETHODSELECTED);
			return toScaConsentResponse(userTO, consent, consentKeyData.template(), scaOperationBO);
		} catch (SCAMethodNotSupportedException e) {
			logger.error(e.getMessage(), e);
			throw new SCAMethodNotSupportedMiddleException(e);
		} catch (UserScaDataNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new UserScaDataNotFoundMiddlewareException(e);
		} catch (SCAOperationValidationException e) {
			throw new SCAOperationValidationMiddlewareException(e.getMessage(), e);
		} catch (SCAOperationNotFoundException e) {
			throw new SCAOperationNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	@SuppressWarnings({"PMD.IdenticalCatchBranches", "PMD.CyclomaticComplexity"})
	public SCAConsentResponseTO authorizeConsent(String consentId, String authorisationId, String authCode)
			throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
			SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, AisConsentNotFoundMiddlewareException {
		AisConsentBO consent = consent(consentId);
		AisConsentTO aisConsentTO = aisConsentMapper.toAisConsentTO(consent);
		ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsentTO);
		try {
			boolean validAuthCode = scaOperationService.validateAuthCode(authorisationId, consentId,
					consentKeyData.template(), authCode);
			if (!validAuthCode) {
				throw new SCAOperationValidationMiddlewareException("Wrong auth code");
			}
			UserBO userBO = scaUtils.userBO();
			UserTO userTO = scaUtils.user(userBO);
			SCAOperationBO scaOperationBO = scaUtils.loadAuthCode(authorisationId);
			SCAConsentResponseTO response = toScaConsentResponse(userTO, consent, consentKeyData.template(), scaOperationBO);
			if(scaOperationService.authenticationCompleted(consentId, OpTypeBO.CONSENT)) {
				BearerTokenTO bearerToken = bearerTokenMapper.toBearerTokenTO(userService.grant(userBO.getId(), consent));
				response.setBearerToken(bearerToken);
			}
			return response;
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
		} catch (InsufficientPermissionException e) {
			throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
		}
	}

	@Override
	public BearerTokenTO grantAisConsent(AisConsentTO aisConsent) throws InsufficientPermissionMiddlewareException {
		try {
			UserBO userBO = scaUtils.userBO();
			return bearerTokenMapper.toBearerTokenTO(userService.grant(userBO.getId(), aisConsentMapper.toAisConsentBO(aisConsent)));
		} catch (InsufficientPermissionException e) {
			throw new InsufficientPermissionMiddlewareException(e.getMessage(), e);
		}
	}

	
	/*
	 * Returns a bearer token matching the consent if user has enougth permission
	 * to execute the operation.
	 */
	private BearerTokenBO checkAisConsent(AisConsentTO aisConsent) throws InsufficientPermissionMiddlewareException {
		AisConsentBO consentBO = aisConsentMapper.toAisConsentBO(aisConsent);
		try {
			UserBO userBO = scaUtils.userBO();
			return userService.grant(userBO.getId(),consentBO);
		} catch (InsufficientPermissionException e) {
			throw new InsufficientPermissionMiddlewareException("Not enougth permission for requested consent.", e);
		}
	}

	/*
	 * The SCA requirement shall be added as property of a deposit account permission.
	 * 
	 * For now we will assume there is no sca requirement, when the user having access
	 * to the account does not habe any sca data configured.
	 */
	@SuppressWarnings("PMD.UnusedFormalParameter")
	private boolean scaRequired(AisConsentTO aisConsent, UserBO user, OpTypeBO opType) {
		return scaUtils.hasSCA(user);
	}

	private SCAConsentResponseTO prepareSCA(UserBO user, AisConsentTO aisConsent, ConsentKeyDataTO consentKeyData) {
		if (!scaRequired(aisConsent, user, OpTypeBO.CONSENT)) {
			SCAConsentResponseTO response = new SCAConsentResponseTO();
			response.setConsentId(aisConsent.getId());
			response.setScaStatus(ScaStatusTO.EXEMPTED);
			return response;
		} else {
			// start SCA
			SCAOperationBO scaOperationBO;
			UserTO userTo =scaUtils.user(user);
			String consentKeyDataTemplate = consentKeyData.template();
			AuthCodeDataBO authCodeData = authCodeData(aisConsent.getId(), user, consentKeyDataTemplate, null, null);
			AisConsentBO consentBO = aisConsentMapper.toAisConsentBO(aisConsent);
			consentBO = userService.storeConsent(consentBO);
			if (userTo.getScaUserData().size() == 1) {
				ScaUserDataTO chosenScaMethod = userTo.getScaUserData().iterator().next();
				authCodeData.setScaUserDataId(chosenScaMethod.getId());
				try {
					scaOperationBO = scaOperationService.generateAuthCode(authCodeData, user, ScaStatusBO.SCAMETHODSELECTED);
				} catch (SCAMethodNotSupportedException | UserScaDataNotFoundException | SCAOperationValidationException
						| SCAOperationNotFoundException e) {
					throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
				}
			} else {
				scaOperationBO = scaOperationService.createAuthCode(authCodeData, ScaStatusBO.PSUAUTHENTICATED);
			}
			return toScaConsentResponse(userTo, consentBO, consentKeyDataTemplate, scaOperationBO);
		}
	}

	private AuthCodeDataBO authCodeData(String consentId, UserBO user, String consentKeyDataTemplate, String authorisationId, String scaMethodId) {
		AuthCodeDataBO authCodeData = new AuthCodeDataBO();
		authCodeData.setOpData(consentKeyDataTemplate);
		authCodeData.setOpId(consentId);
		authCodeData.setOpType(OpTypeBO.CONSENT);
		authCodeData.setUserLogin(user.getLogin());
		authCodeData.setUserMessage(consentKeyDataTemplate);
		authCodeData.setValiditySeconds(1800);
		authCodeData.setAuthorisationId(authorisationId);
		authCodeData.setScaUserDataId(scaMethodId);
		return authCodeData;
	}

	private SCAConsentResponseTO toScaConsentResponse(UserTO user,
			AisConsentBO consent, String messageTemplate, SCAOperationBO operation) {
		SCAConsentResponseTO response = new SCAConsentResponseTO();
		response.setAuthorisationId(operation.getId());
		response.setChosenScaMethod(scaUtils.getScaMethod(user, operation.getScaMethodId()));
		response.setChallengeData(null);
		response.setExpiresInSeconds(operation.getValiditySeconds());
		response.setConsentId(consent.getId());
		response.setPsuMessage(messageTemplate);
		response.setScaMethods(user.getScaUserData());
		response.setStatusDate(operation.getStatusTime());
		response.setScaStatus(ScaStatusTO.valueOf(operation.getScaStatus().name()));
		return response;
	}
	private AisConsentBO consent(String consentId) throws AisConsentNotFoundMiddlewareException {
		try {
			return userService.loadConsent(consentId);
		} catch (ConsentNotFoundException e) {
			throw new AisConsentNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	
}
