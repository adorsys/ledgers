package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.FundsConfirmationRequestBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountAlreadyExistsException;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.TransactionNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.payment.ConsentKeyDataTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.*;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.impl.converter.*;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.exception.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.exception.ConsentNotFoundException;
import de.adorsys.ledgers.um.api.exception.InsufficientPermissionException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.exception.UserScaDataNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("PMD.TooManyMethods")
public class MiddlewareAccountManagementServiceImpl implements MiddlewareAccountManagementService {
    private static final Logger logger = LoggerFactory.getLogger(MiddlewareAccountManagementServiceImpl.class);
    private static final LocalDateTime BASE_TIME = LocalDateTime.MIN;

    private final DepositAccountService depositAccountService;
    private final AccountDetailsMapper accountDetailsMapper;
    private final PaymentConverter paymentConverter;
    private final UserService userService;
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    private final AisConsentBOMapper aisConsentMapper;
    private final BearerTokenMapper bearerTokenMapper;
    private final AccessTokenMapper accessTokenMapper;
    private final AccessTokenTO accessToken;
    private final SCAOperationService scaOperationService;
    private final SCAUtils scaUtils;
    private final AccessService accessService;
    private int defaultLoginTokenExpireInSeconds = 600; // 600 seconds.
    private final AmountMapper amountMapper;

    @Value("${sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;


    public MiddlewareAccountManagementServiceImpl(DepositAccountService depositAccountService,
                                                  AccountDetailsMapper accountDetailsMapper, PaymentConverter paymentConverter, UserService userService,
                                                  AisConsentBOMapper aisConsentMapper, BearerTokenMapper bearerTokenMapper,
                                                  AccessTokenMapper accessTokenMapper, AccessTokenTO accessToken, SCAOperationService scaOperationService,
                                                  SCAUtils scaUtils, AccessService accessService, AmountMapper amountMapper) {
        this.depositAccountService = depositAccountService;
        this.accountDetailsMapper = accountDetailsMapper;
        this.paymentConverter = paymentConverter;
        this.userService = userService;
        this.aisConsentMapper = aisConsentMapper;
        this.bearerTokenMapper = bearerTokenMapper;
        this.accessTokenMapper = accessTokenMapper;
        this.accessToken = accessToken;
        this.scaOperationService = scaOperationService;
        this.scaUtils = scaUtils;
        this.accessService = accessService;
        this.amountMapper = amountMapper;
    }

    @Override
    public void createDepositAccount(String userID, AccountDetailsTO depositAccount) throws UserNotFoundMiddlewareException, AccountNotFoundMiddlewareException {
        try {
            UserBO user = userService.findById(userID);
            DepositAccountBO accountToCreate = accountDetailsMapper.toDepositAccountBO(depositAccount);
            DepositAccountBO createdAccount = depositAccountService.createDepositAccountForBranch(accountToCreate, userID, user.getBranch());

            AccountAccessBO accountAccess = accessService.createAccountAccess(createdAccount.getIban(), AccessTypeBO.OWNER);
            accessService.updateAccountAccess(user, accountAccess);

            //Check if the account is created by Branch and if so add access to this account to Branch
            if (!user.getLogin().equals(accessToken.getLogin())) {
                UserBO branch = userService.findByLogin(accessToken.getLogin());
                accessService.updateAccountAccess(branch, accountAccess);
            }
        } catch (UserNotFoundException e) {
            logger.error(e.getMessage());
            throw new UserNotFoundMiddlewareException();
        } catch (DepositAccountNotFoundException e) {
            throw new AccountNotFoundMiddlewareException();
        } catch (DepositAccountAlreadyExistsException e) {
            throw new DepositAccountAlreadyExistsMiddlewareException(e.getMessage());
        }
    }

    @Override
    public AccountDetailsTO getDepositAccountById(String accountId, LocalDateTime time, boolean withBalance) throws AccountNotFoundMiddlewareException {
        try {
            DepositAccountDetailsBO accountDetailsBO = depositAccountService.getDepositAccountById(accountId, time, true);
            return accountDetailsMapper.toAccountDetailsTO(accountDetailsBO);
        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
            throw new UserNotFoundMiddlewareException(e.getMessage());
        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage());
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
            throws AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException, AccountNotFoundMiddlewareException {
        String accNbr = accountNumberPrefix + accountNumberSuffix;
        // if the list is not empty, we mus make sure that account belong to the current user.s
        List<DepositAccountBO> accounts = depositAccountService.findByAccountNumberPrefix(accountNumberPrefix);
        validateInput(accounts, accountNumberPrefix, accountNumberSuffix);
        accDetails.setIban(accNbr);
        try {
            createDepositAccount(accessToken.getSub(), accDetails);
        } catch (UserNotFoundMiddlewareException e) {
            throw new AccountMiddlewareUncheckedException(String.format("Can not find user with id %s and login %s", accessToken.getSub(), accessToken.getLogin()));
        }
    }

    // Validate that
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private void validateInput(List<DepositAccountBO> accounts, String accountNumberPrefix, String accountNumberSuffix) throws AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException {
        // This prefix is still free
        if (accounts.isEmpty()) {
            return;
        }

        // XOR The user is the owner of this prefix
        List<AccountAccessTO> accountAccesses = null;
        try {
            accountAccesses = userMapper.toAccountAccessListTO(userService.findById(accessToken.getSub()).getAccountAccesses());
        } catch (UserNotFoundException e) {
            logger.error("Could not get User by id: {}", accessToken.getSub());
        }

        // Empty if user is not owner of this prefix.
        if (accountAccesses == null || accountAccesses.isEmpty()) {
            // User can not own any of those accounts.
            throw new AccountWithPrefixGoneMiddlewareException(String.format("Account prefix %s is gone.", accountNumberPrefix));
        }

        List<String> ownedAccounts = accessService.filterOwnedAccounts(accountAccesses);

        // user already has account with this prefix and suffix
        String accNbr = accountNumberPrefix + accountNumberSuffix;
        if (ownedAccounts.contains(accNbr)) {
            throw new AccountWithSuffixExistsMiddlewareException(String.format("Account with suffix %S and prefix %s already exist", accountNumberPrefix, accountNumberSuffix));
        }

        // All accounts with this prefix must be owned by this user.
        for (DepositAccountBO a : accounts) {
            if (ownedAccounts.contains(a.getIban())) {
                throw new AccountWithSuffixExistsMiddlewareException(String.format("User not owner of account with iban %s that also holds the requested prefix %s", a.getIban(), accountNumberPrefix));
            }
        }
    }

    @Override
    public List<AccountDetailsTO> listDepositAccounts() {
        UserBO user = accessService.loadCurrentUser();
        UserTO userTO = userMapper.toUserTO(user);
        List<AccountAccessTO> accountAccesses = userTO.getAccountAccesses();
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
    public List<AccountDetailsTO> listDepositAccountsByBranch() {
        UserBO user = accessService.loadCurrentUser();

        List<DepositAccountDetailsBO> depositAccounts = depositAccountService.findByBranch(user.getBranch());

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
        if (ScaStatusTO.EXEMPTED.equals(response.getScaStatus())) {
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
                                                             String scaMethodId) throws SCAMethodNotSupportedMiddleException,
                                                                                                UserScaDataNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
                                                                                                SCAOperationNotFoundMiddlewareException, AisConsentNotFoundMiddlewareException {
        UserBO userBO = scaUtils.userBO();
        UserTO userTO = scaUtils.user(userBO);
        AisConsentBO consent = consent(consentId);
        AisConsentTO aisConsentTO = aisConsentMapper.toAisConsentTO(consent);
        ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsentTO);
        String template = consentKeyData.template();
        int scaWeight = accessService.resolveMinimalScaWeightForConsent(consent.getAccess(), userBO.getAccountAccesses());

        AuthCodeDataBO a = new AuthCodeDataBO(userBO.getLogin(), scaMethodId,
                consentId, template, template,
                defaultLoginTokenExpireInSeconds, OpTypeBO.CONSENT, authorisationId, scaWeight);
        try {
            SCAOperationBO scaOperationBO = scaOperationService.generateAuthCode(a, userBO, ScaStatusBO.SCAMETHODSELECTED);
            return toScaConsentResponse(userTO, consent, consentKeyData.template(), scaOperationBO);
        } catch (SCAMethodNotSupportedException e) {
            logger.error(e.getMessage());
            throw new SCAMethodNotSupportedMiddleException(e);
        } catch (UserScaDataNotFoundException e) {
            logger.error(e.getMessage());
            throw new UserScaDataNotFoundMiddlewareException(e);
        } catch (SCAOperationValidationException e) {
            throw new SCAOperationValidationMiddlewareException(e.getMessage(), e);
        } catch (SCAOperationNotFoundException e) {
            throw new SCAOperationNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public SCAConsentResponseTO authorizeConsent(String consentId, String authorisationId, String authCode)
            throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
                           SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, AisConsentNotFoundMiddlewareException {
        AisConsentBO consent = consent(consentId);
        AisConsentTO aisConsentTO = aisConsentMapper.toAisConsentTO(consent);
        ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsentTO);
        try {
            UserBO userBO = scaUtils.userBO();
            int scaWeight = accessService.resolveMinimalScaWeightForConsent(consent.getAccess(), userBO.getAccountAccesses());
            boolean validAuthCode = scaOperationService.validateAuthCode(authorisationId, consentId,
                    consentKeyData.template(), authCode, scaWeight);
            if (!validAuthCode) {
                throw new SCAOperationValidationMiddlewareException("Wrong auth code");
            }
            UserTO userTO = scaUtils.user(userBO);
            SCAOperationBO scaOperationBO = scaUtils.loadAuthCode(authorisationId);
            SCAConsentResponseTO response = toScaConsentResponse(userTO, consent, consentKeyData.template(), scaOperationBO);
            if (scaOperationService.authenticationCompleted(consentId, OpTypeBO.CONSENT)) {
                BearerTokenBO consentToken = userService.consentToken(accessTokenMapper.toAccessTokenBO(accessToken), consent);
                response.setBearerToken(bearerTokenMapper.toBearerTokenTO(consentToken));
            } else if (multilevelScaEnable) {
                response.setPartiallyAuthorised(true);
            }
            return response;
        } catch (SCAOperationNotFoundException e) {
            logger.error(e.getMessage());
            throw new SCAOperationNotFoundMiddlewareException(e);
        } catch (SCAOperationValidationException e) {
            logger.error(e.getMessage());
            throw new SCAOperationValidationMiddlewareException(e);
        } catch (SCAOperationExpiredException e) {
            logger.error(e.getMessage());
            throw new SCAOperationExpiredMiddlewareException(e);
        } catch (SCAOperationUsedOrStolenException e) {
            logger.error(e.getMessage());
            throw new SCAOperationUsedOrStolenMiddlewareException(e);
        } catch (InsufficientPermissionException e) {
            throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
        }
    }

    @Override
    public SCAConsentResponseTO grantAisConsent(AisConsentTO aisConsent) throws InsufficientPermissionMiddlewareException {
        try {
            AisConsentTO piisConsentTO = cleanupForPIIS(aisConsent);
            ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(piisConsentTO);
            AisConsentBO consentBO = aisConsentMapper.toAisConsentBO(piisConsentTO);

            BearerTokenBO consentToken = userService.consentToken(accessTokenMapper.toAccessTokenBO(accessToken), consentBO);
            SCAConsentResponseTO response = new SCAConsentResponseTO();
            response.setBearerToken(bearerTokenMapper.toBearerTokenTO(consentToken));
            response.setAuthorisationId(scaUtils.authorisationId());
            response.setConsentId(aisConsent.getId());
            response.setPsuMessage(consentKeyData.exemptedTemplate());
            response.setScaStatus(ScaStatusTO.EXEMPTED);
            response.setStatusDate(LocalDateTime.now());
            return response;
        } catch (InsufficientPermissionException e) {
            throw new InsufficientPermissionMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public void depositCash(String accountId, AmountTO amount) throws AccountNotFoundMiddlewareException {
        try {
            depositAccountService.depositCash(accountId, amountMapper.toAmountBO(amount), accessToken.getLogin());
        } catch (DepositAccountNotFoundException e) {
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    /*
     * We reuse an ais consent and trim everything we do not need.
     */

    private AisConsentTO cleanupForPIIS(AisConsentTO aisConsentTo) {
        // Cautiously empty all fields.
        aisConsentTo.getAccess().setAllPsd2(null);
        aisConsentTo.getAccess().setAvailableAccounts(null);
        aisConsentTo.getAccess().setAccounts(Collections.emptyList());
        aisConsentTo.getAccess().setTransactions(Collections.emptyList());
        return aisConsentTo;
    }

    /*
     * Returns a bearer token matching the consent if user has enougth permission
     * to execute the operation.
     */

    private BearerTokenBO checkAisConsent(AisConsentTO aisConsent) throws InsufficientPermissionMiddlewareException {
        AisConsentBO consentBO = aisConsentMapper.toAisConsentBO(aisConsent);
        try {
            return userService.consentToken(accessTokenMapper.toAccessTokenBO(accessToken), consentBO);
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

    private boolean scaRequired(UserBO user) {
        return scaUtils.hasSCA(user);
    }

    private SCAConsentResponseTO prepareSCA(UserBO user, AisConsentTO aisConsent, ConsentKeyDataTO consentKeyData) {
        String consentKeyDataTemplate = consentKeyData.template();
        UserTO userTo = scaUtils.user(user);
        String authorisationId = scaUtils.authorisationId();
        if (!scaRequired(user)) {
            SCAConsentResponseTO response = new SCAConsentResponseTO();
            response.setAuthorisationId(authorisationId);
            response.setConsentId(aisConsent.getId());
            response.setPsuMessage(consentKeyData.exemptedTemplate());
            response.setScaStatus(ScaStatusTO.EXEMPTED);
            response.setStatusDate(LocalDateTime.now());
            return response;
        } else {
            // start SCA
            SCAOperationBO scaOperationBO;
            AisConsentBO consentBO = aisConsentMapper.toAisConsentBO(aisConsent);
            consentBO = userService.storeConsent(consentBO);

            int scaWeight = accessService.resolveMinimalScaWeightForConsent(consentBO.getAccess(), user.getAccountAccesses());

            AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(),
                    aisConsent.getId(), aisConsent.getId(),
                    consentKeyDataTemplate, consentKeyDataTemplate,
                    defaultLoginTokenExpireInSeconds, OpTypeBO.CONSENT, authorisationId, scaWeight);
            // FPO no auto generation of SCA AutCode. Process shall always be triggered from outside
            // The system. Even if a user ha only one sca method.
            scaOperationBO = scaOperationService.createAuthCode(authCodeData, ScaStatusBO.PSUAUTHENTICATED);
            return toScaConsentResponse(userTo, consentBO, consentKeyDataTemplate, scaOperationBO);
        }
    }

    private SCAConsentResponseTO toScaConsentResponse(UserTO user, AisConsentBO consent, String messageTemplate, SCAOperationBO operation) {
        SCAConsentResponseTO response = new SCAConsentResponseTO(); //TODO - matter of refactoring
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
