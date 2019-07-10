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
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
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
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("PMD.TooManyMethods")
public class MiddlewareAccountManagementServiceImpl implements MiddlewareAccountManagementService {
    private static final LocalDateTime BASE_TIME = LocalDateTime.MIN;

    private final UserMapper userMapper;
    private final DepositAccountService depositAccountService;
    private final AccountDetailsMapper accountDetailsMapper;
    private final PaymentConverter paymentConverter;
    private final UserService userService;
    private final AisConsentBOMapper aisConsentMapper;
    private final BearerTokenMapper bearerTokenMapper;
    private final SCAOperationService scaOperationService;
    private final SCAUtils scaUtils;
    private final AccessService accessService;
    private int defaultLoginTokenExpireInSeconds = 600; // 600 seconds.
    private final AmountMapper amountMapper;
    private final ScaInfoMapper scaInfoMapper;

    @Value("${sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

    @Override
    public void createDepositAccount(String userId, ScaInfoTO scaInfoTO, AccountDetailsTO depositAccount) throws AccountNotFoundMiddlewareException {
        try {
            UserBO user = userService.findById(userId);
            DepositAccountBO accountToCreate = accountDetailsMapper.toDepositAccountBO(depositAccount);
            DepositAccountBO createdAccount = depositAccountService.createDepositAccountForBranch(accountToCreate, user.getId(), user.getBranch());

            AccountAccessBO accountAccess = accessService.createAccountAccess(createdAccount.getIban(), AccessTypeBO.OWNER);
            accessService.updateAccountAccess(user, accountAccess);

            //Check if the account is created by Branch and if so add access to this account to Branch
            if (!user.getLogin().equals(scaInfoTO.getUserLogin())) {
                UserBO userBO = userService.findByLogin(scaInfoTO.getUserLogin());
                accessService.updateAccountAccess(userBO, accountAccess);
            }
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
            log.error(e.getMessage());
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
            log.error(e.getMessage());
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public List<AccountDetailsTO> getAllAccountDetailsByUserLogin(String userLogin) throws AccountNotFoundMiddlewareException {
        log.info("Retrieving accounts by user login {}", userLogin);
        try {
            UserBO userBO = userService.findByLogin(userLogin);
            List<AccountAccessBO> accountAccess = userBO.getAccountAccesses();
            log.info("{} accounts were retrieved", accountAccess.size());

            List<String> ibans = accountAccess.stream()
                                         .filter(a -> a.getAccessType() == AccessTypeBO.OWNER)
                                         .map(AccountAccessBO::getIban)
                                         .collect(Collectors.toList());
            log.info("{} were accounts were filtered as OWN", ibans.size());

            List<DepositAccountDetailsBO> depositAccounts = depositAccountService.getDepositAccountsByIban(ibans, BASE_TIME, false);
            log.info("{} deposit accounts were found", depositAccounts.size());

            return depositAccounts.stream()
                           .map(accountDetailsMapper::toAccountDetailsTO)
                           .collect(Collectors.toList());
        } catch (DepositAccountNotFoundException e) {
            log.error(e.getMessage());
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
    public void createDepositAccount(ScaInfoTO scaInfoTO, String accountNumberPrefix, String accountNumberSuffix, AccountDetailsTO accDetails)
            throws AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException, AccountNotFoundMiddlewareException {
        String accNbr = accountNumberPrefix + accountNumberSuffix;
        // if the list is not empty, we mus make sure that account belong to the current user.s
        List<DepositAccountBO> accounts = depositAccountService.findByAccountNumberPrefix(accountNumberPrefix);
        validateInput(scaInfoTO.getUserId(), accounts, accountNumberPrefix, accountNumberSuffix);
        accDetails.setIban(accNbr);
        createDepositAccount(scaInfoTO.getUserId(), scaInfoTO, accDetails);
    }

    // Validate that
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private void validateInput(String userId, List<DepositAccountBO> accounts, String accountNumberPrefix, String accountNumberSuffix) throws AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException {
        // This prefix is still free
        if (accounts.isEmpty()) {
            return;
        }

        // XOR The user is the owner of this prefix
        List<AccountAccessTO> accountAccesses = userMapper.toAccountAccessListTO(userService.findById(userId).getAccountAccesses());

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
    public List<AccountDetailsTO> listDepositAccounts(String userId) {
        UserBO user = accessService.loadCurrentUser(userId);
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
    public List<AccountDetailsTO> listDepositAccountsByBranch(String userId) {
        UserBO user = accessService.loadCurrentUser(userId);

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
    public SCAConsentResponseTO startSCA(ScaInfoTO scaInfoTO, String consentId, AisConsentTO aisConsent) {
        BearerTokenBO bearerToken = checkAisConsent(scaInfoMapper.toScaInfoBO(scaInfoTO), aisConsent);
        ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsent);
        SCAConsentResponseTO response = prepareSCA(scaInfoTO, scaUtils.userBO(scaInfoTO.getUserId()), aisConsent, consentKeyData);
        if (ScaStatusTO.EXEMPTED.equals(response.getScaStatus())) {
            response.setBearerToken(bearerTokenMapper.toBearerTokenTO(bearerToken));
        }
        return response;
    }

    @Override
    public SCAConsentResponseTO loadSCAForAisConsent(String userId, String consentId, String authorisationId)
            throws SCAOperationExpiredMiddlewareException {
        UserTO user = userMapper.toUserTO(scaUtils.userBO(userId));
        AisConsentBO consent = userService.loadConsent(consentId);
        AisConsentTO aisConsentTO = aisConsentMapper.toAisConsentTO(consent);
        ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsentTO);
        SCAOperationBO scaOperationBO = scaUtils.loadAuthCode(authorisationId);
        return toScaConsentResponse(user, consent, consentKeyData.template(), scaOperationBO);
    }

    @Override
    @SuppressWarnings("PMD.IdenticalCatchBranches")
    public SCAConsentResponseTO selectSCAMethodForAisConsent(String userId, String consentId, String authorisationId,
                                                             String scaMethodId) throws SCAMethodNotSupportedMiddleException,
                                                                                                SCAOperationValidationMiddlewareException,
                                                                                                SCAOperationNotFoundMiddlewareException {
        UserBO userBO = scaUtils.userBO(userId);
        UserTO userTO = scaUtils.user(userBO);
        AisConsentBO consent = userService.loadConsent(consentId);
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
            log.error(e.getMessage());
            throw new SCAMethodNotSupportedMiddleException(e);
        } catch (SCAOperationValidationException e) {
            throw new SCAOperationValidationMiddlewareException(e.getMessage(), e);
        } catch (SCAOperationNotFoundException e) {
            throw new SCAOperationNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public SCAConsentResponseTO authorizeConsent(ScaInfoTO scaInfoTO, String consentId)
            throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
                           SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException {
        AisConsentBO consent = userService.loadConsent(consentId);
        AisConsentTO aisConsentTO = aisConsentMapper.toAisConsentTO(consent);
        ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsentTO);
        try {
            UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
            int scaWeight = accessService.resolveMinimalScaWeightForConsent(consent.getAccess(), userBO.getAccountAccesses());
            boolean validAuthCode = scaOperationService.validateAuthCode(scaInfoTO.getAuthorisationId(), consentId,
                    consentKeyData.template(), scaInfoTO.getAuthCode(), scaWeight);
            if (!validAuthCode) {
                throw new SCAOperationValidationMiddlewareException("Wrong auth code");
            }
            UserTO userTO = scaUtils.user(userBO);
            SCAOperationBO scaOperationBO = scaUtils.loadAuthCode(scaInfoTO.getAuthorisationId());
            SCAConsentResponseTO response = toScaConsentResponse(userTO, consent, consentKeyData.template(), scaOperationBO);
            if (scaOperationService.authenticationCompleted(consentId, OpTypeBO.CONSENT)) {
                BearerTokenBO consentToken = userService.consentToken(scaInfoMapper.toScaInfoBO(scaInfoTO), consent);
                response.setBearerToken(bearerTokenMapper.toBearerTokenTO(consentToken));
            } else if (multilevelScaEnable) {
                response.setPartiallyAuthorised(true);
            }
            return response;
        } catch (SCAOperationNotFoundException e) {
            log.error(e.getMessage());
            throw new SCAOperationNotFoundMiddlewareException(e);
        } catch (SCAOperationValidationException e) {
            log.error(e.getMessage());
            throw new SCAOperationValidationMiddlewareException(e);
        } catch (SCAOperationExpiredException e) {
            log.error(e.getMessage());
            throw new SCAOperationExpiredMiddlewareException(e);
        } catch (SCAOperationUsedOrStolenException e) {
            log.error(e.getMessage());
            throw new SCAOperationUsedOrStolenMiddlewareException(e);
        }
    }

    @Override
    public SCAConsentResponseTO grantAisConsent(ScaInfoTO scaInfoTO, AisConsentTO aisConsent) {
        AisConsentTO piisConsentTO = cleanupForPIIS(aisConsent);
        ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(piisConsentTO);
        AisConsentBO consentBO = aisConsentMapper.toAisConsentBO(piisConsentTO);

        BearerTokenBO consentToken = userService.consentToken(scaInfoMapper.toScaInfoBO(scaInfoTO), consentBO);
        SCAConsentResponseTO response = new SCAConsentResponseTO();
        response.setBearerToken(bearerTokenMapper.toBearerTokenTO(consentToken));
        response.setAuthorisationId(scaUtils.authorisationId(scaInfoTO));
        response.setConsentId(aisConsent.getId());
        response.setPsuMessage(consentKeyData.exemptedTemplate());
        response.setScaStatus(ScaStatusTO.EXEMPTED);
        response.setStatusDate(LocalDateTime.now());
        return response;
    }

    @Override
    public void depositCash(ScaInfoTO scaInfoTO, String accountId, AmountTO amount) throws AccountNotFoundMiddlewareException {
        try {
            depositAccountService.depositCash(accountId, amountMapper.toAmountBO(amount), scaInfoTO.getUserLogin());
        } catch (DepositAccountNotFoundException e) {
            throw new AccountNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public List<AccountAccessTO> getAccountAccesses(String userId) {
        UserBO user = userService.findById(userId);
        UserTO userTO = userMapper.toUserTO(user);
        return userTO.getAccountAccesses();
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

    private BearerTokenBO checkAisConsent(ScaInfoBO scaInfoBO, AisConsentTO aisConsent) {
        AisConsentBO consentBO = aisConsentMapper.toAisConsentBO(aisConsent);
        return userService.consentToken(scaInfoBO, consentBO);

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

    private SCAConsentResponseTO prepareSCA(ScaInfoTO scaInfoTO, UserBO user, AisConsentTO aisConsent, ConsentKeyDataTO consentKeyData) {
        String consentKeyDataTemplate = consentKeyData.template();
        UserTO userTo = scaUtils.user(user);
        String authorisationId = scaUtils.authorisationId(scaInfoTO);
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
}
