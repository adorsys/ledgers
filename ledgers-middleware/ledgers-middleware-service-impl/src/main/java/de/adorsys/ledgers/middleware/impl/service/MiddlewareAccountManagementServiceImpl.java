package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.FundsConfirmationRequestBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.middleware.api.domain.Constants;
import de.adorsys.ledgers.middleware.api.domain.account.*;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.payment.ConsentKeyDataTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.impl.converter.*;
import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.STAFF;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.*;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException.blockedSupplier;
import static java.lang.String.format;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("PMD.TooManyMethods")
public class MiddlewareAccountManagementServiceImpl implements MiddlewareAccountManagementService {
    private static final int NANO_TO_SECOND = 1000000000;

    private final UserMapper userMapper;
    private final DepositAccountService depositAccountService;
    private final DepositAccountTransactionService transactionService;
    private final AccountDetailsMapper accountDetailsMapper;
    private final PaymentConverter paymentConverter;
    private final UserService userService;
    private final AisConsentBOMapper aisConsentMapper;
    private final BearerTokenMapper bearerTokenMapper;
    private final SCAUtils scaUtils;
    private final AccessService accessService;
    private final AmountMapper amountMapper;
    private final PageMapper pageMapper;
    private final ScaResponseResolver scaResponseResolver;
    private final KeycloakTokenService tokenService;

    @Value("${ledgers.token.lifetime.seconds.sca:10800}")
    private int scaTokenLifeTime;
    @Value("${ledgers.token.lifetime.seconds.full:7776000}")
    private int fullTokenLifeTime;

    @Override
    public List<AccountDetailsTO> getAccountsByIbanAndCurrency(String iban, String currency) {
        return accountDetailsMapper.toAccountDetailsList(depositAccountService.getAccountsByIbanAndParamCurrency(iban, currency));
    }

    @Override
    public void createDepositAccount(String userId, ScaInfoTO scaInfoTO, AccountDetailsTO depositAccount) {
        if (depositAccount.getCurrency() == null) {
            throw MiddlewareModuleException.builder()
                          .errorCode(ACCOUNT_CREATION_VALIDATION_FAILURE)
                          .devMsg("Can not create new account without currency set! Please set currency to continue.")
                          .build();
        }
        UserBO user = userService.findById(userId);

        checkPresentAccountsAndOwner(depositAccount.getIban(), user);
        DepositAccountBO accountToCreate = accountDetailsMapper.toDepositAccountBO(depositAccount);
        DepositAccountBO createdAccount = depositAccountService.createNewAccount(accountToCreate, user.getLogin(), user.getBranch());
        accessService.updateAccountAccessNewAccount(createdAccount, user);
    }

    @Override
    public AccountDetailsTO getDepositAccountById(String accountId, LocalDateTime time, boolean withBalance) {
        DepositAccountDetailsBO accountDetailsBO = depositAccountService.getAccountDetailsById(accountId, time, true);
        return accountDetailsMapper.toAccountDetailsTO(accountDetailsBO);
    }

    @Override
    @Deprecated
    public AccountDetailsTO getDepositAccountByIban(String iban, LocalDateTime time, boolean withBalance) {
        return accountDetailsMapper.toAccountDetailsTO(depositAccountService.getDetailsByIban(iban, time, withBalance));
    }

    @Override
    public List<AccountDetailsTO> getAllAccountDetailsByUserLogin(String userLogin) {
        log.info("Retrieving accounts by user login {}", userLogin);
        UserBO userBO = userService.findByLogin(userLogin);
        List<AccountAccessBO> accountAccess = userBO.getAccountAccesses();
        log.info("{} accounts accesses were found", accountAccess.size());
        List<DepositAccountBO> details = accountAccess.stream()
                                                 .filter(a -> a.getAccessType() == AccessTypeBO.OWNER)
                                                 .map(a -> depositAccountService.getAccountByIbanAndCurrency(a.getIban(), a.getCurrency()))
                                                 .collect(Collectors.toList());

        log.info("{} were accounts were filtered as OWN", details.size());
        return accountDetailsMapper.toAccountDetailsList(details);
    }

    @Override
    public TransactionTO getTransactionById(String accountId, String transactionId) {
        TransactionDetailsBO transaction = depositAccountService.getTransactionById(accountId, transactionId);
        return paymentConverter.toTransactionTO(transaction);
    }

    @Override
    public List<TransactionTO> getTransactionsByDates(String accountId, LocalDate dateFrom, LocalDate dateTo) {
        log.info("Start retrieving transactions for {}", accountId);
        long start = System.nanoTime();
        LocalDate today = LocalDate.now();
        LocalDateTime dateTimeFrom = dateFrom == null
                                             ? today.atStartOfDay()
                                             : dateFrom.atStartOfDay();
        LocalDateTime dateTimeTo = dateTo == null
                                           ? accessService.getTimeAtEndOfTheDay(today)
                                           : accessService.getTimeAtEndOfTheDay(dateTo);

        List<TransactionDetailsBO> transactions = depositAccountService.getTransactionsByDates(accountId, dateTimeFrom, dateTimeTo);
        log.info("Retrieved {} transactions in {} secs", transactions.size(), (double) (System.nanoTime() - start) / NANO_TO_SECOND);
        return paymentConverter.toTransactionTOList(transactions);
    }

    @Override
    public CustomPageImpl<TransactionTO> getTransactionsByDatesPaged(String accountId, LocalDate dateFrom, LocalDate dateTo, CustomPageableImpl pageable) {
        LocalDate today = LocalDate.now();
        LocalDateTime dateTimeFrom = dateFrom == null
                                             ? today.atStartOfDay()
                                             : dateFrom.atStartOfDay();
        LocalDateTime dateTimeTo = dateTo == null
                                           ? accessService.getTimeAtEndOfTheDay(today)
                                           : accessService.getTimeAtEndOfTheDay(dateTo);

        return pageMapper.toCustomPageImpl(depositAccountService.getTransactionsByDatesPaged(accountId, dateTimeFrom, dateTimeTo, PageRequest.of(pageable.getPage(), pageable.getSize()))
                                                   .map(paymentConverter::toTransactionTO));
    }

    @Override
    public boolean confirmFundsAvailability(FundsConfirmationRequestTO request) {
        FundsConfirmationRequestBO requestBO = accountDetailsMapper.toFundsConfirmationRequestBO(request);
        return depositAccountService.confirmationOfFunds(requestBO);
    }

    @Override
    public void createDepositAccount(ScaInfoTO scaInfoTO, String accountNumberPrefix, String accountNumberSuffix, AccountDetailsTO accDetails) {
        String accNbr = accountNumberPrefix + accountNumberSuffix;
        // if the list is not empty, we mus make sure that account belong to the current user.s
        List<DepositAccountBO> accounts = depositAccountService.findByAccountNumberPrefix(accountNumberPrefix);
        validateInput(scaInfoTO.getUserId(), accounts, accountNumberPrefix, accountNumberSuffix);
        accDetails.setIban(accNbr);
        createDepositAccount(scaInfoTO.getUserId(), scaInfoTO, accDetails);
    }

    // Validate that
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private void validateInput(String userId, List<DepositAccountBO> accounts, String accountNumberPrefix, String accountNumberSuffix) {
        // This prefix is still free
        if (accounts.isEmpty()) {
            return;
        }

        // XOR The user is the owner of this prefix
        List<AccountAccessTO> accountAccesses = userMapper.toAccountAccessListTO(userService.findById(userId).getAccountAccesses());

        // Empty if user is not owner of this prefix.
        if (CollectionUtils.isNotEmpty(accountAccesses)) {
            // User can not own any of those accounts.
            throw MiddlewareModuleException.builder()
                          .errorCode(ACCOUNT_CREATION_VALIDATION_FAILURE)
                          .devMsg(format("Account prefix %s is gone.", accountNumberPrefix))
                          .build();
        }

        List<String> ownedAccounts = accessService.filterOwnedAccounts(accountAccesses);

        // user already has account with this prefix and suffix
        String accNbr = accountNumberPrefix + accountNumberSuffix;
        if (ownedAccounts.contains(accNbr)) {
            throw MiddlewareModuleException.builder()
                          .errorCode(ACCOUNT_CREATION_VALIDATION_FAILURE)
                          .devMsg(format("Account with suffix %S and prefix %s already exist", accountNumberPrefix, accountNumberSuffix))
                          .build();
        }

        // All accounts with this prefix must be owned by this user.
        for (DepositAccountBO a : accounts) {
            if (ownedAccounts.contains(a.getIban())) {
                throw MiddlewareModuleException.builder()
                              .errorCode(ACCOUNT_CREATION_VALIDATION_FAILURE)
                              .devMsg(format("User not owner of account with iban %s that also holds the requested prefix %s", a.getIban(), accountNumberPrefix))
                              .build();
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
        return accountAccesses.stream()
                       .map(a -> depositAccountService.getAccountDetailsByIbanAndCurrency(a.getIban(), a.getCurrency(), LocalDateTime.now(), true))
                       .map(accountDetailsMapper::toAccountDetailsTO)
                       .collect(Collectors.toList());
    }

    @Override
    public List<AccountDetailsTO> listDepositAccountsByBranch(String userId) {
        UserBO user = accessService.loadCurrentUser(userId);

        List<DepositAccountDetailsBO> depositAccounts = depositAccountService.findDetailsByBranch(user.getBranch());

        return depositAccounts.stream()
                       .map(accountDetailsMapper::toAccountDetailsTO)
                       .collect(Collectors.toList());

    }

    @Override
    public CustomPageImpl<AccountDetailsTO> listDepositAccountsByBranchPaged(String userId, String queryParam, CustomPageableImpl pageable) {
        UserBO user = accessService.loadCurrentUser(userId);
        return pageMapper.toCustomPageImpl(depositAccountService.findDetailsByBranchPaged(user.getBranch(), queryParam, PageRequest.of(pageable.getPage(), pageable.getSize()))
                                                   .map(accountDetailsMapper::toAccountDetailsTO));
    }

    @Override
    public CustomPageImpl<AccountDetailsExtendedTO> getAccountsByBranchAndMultipleParams(String countryCode, String branchId, String branchLogin, String iban, Boolean blocked, CustomPageableImpl pageable) {
        Map<String, String> branchIds = userService.findBranchIdsByMultipleParameters(countryCode, branchId, branchLogin);
        Page<AccountDetailsExtendedTO> page = depositAccountService.findByBranchIdsAndMultipleParams(branchIds.keySet(), iban, blocked, PageRequest.of(pageable.getPage(), pageable.getSize()))
                                                      .map(d -> accountDetailsMapper.toAccountDetailsExtendedTO(d, branchIds.get(d.getBranch())));
        return pageMapper.toCustomPageImpl(page);
    }


    @Override
    public String iban(String id) {
        return depositAccountService.readIbanById(id);
    }

    // ======================= CONSENT ======================//
    @Override
    public SCAConsentResponseTO startAisConsent(ScaInfoTO scaInfoTO, String consentId, AisConsentTO aisConsent) {
        UserBO user = scaUtils.userBO(scaInfoTO.getUserLogin());
        if (!aisConsent.getAccess().getListedAccounts().stream().allMatch(user::hasAccessToAccount)) {
            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg("You can not Grant Access to Accounts you do not have access to!")
                          .build();
        }

        boolean isScaRequired = user.hasSCA();
        String psuMessage = new ConsentKeyDataTO(aisConsent).template();
        BearerTokenTO token = isScaRequired
                                      ? tokenService.exchangeToken(scaInfoTO.getAccessToken(), scaTokenLifeTime, Constants.SCOPE_SCA)
                                      : tokenService.exchangeToken(scaInfoTO.getAccessToken(), fullTokenLifeTime, Constants.SCOPE_FULL_ACCESS);
        ScaStatusTO scaStatus = isScaRequired
                                        ? ScaStatusTO.PSUAUTHENTICATED
                                        : ScaStatusTO.EXEMPTED;
        int scaWeight = accessService.resolveMinimalScaWeightForConsent(aisConsentMapper.toAisConsentBO(aisConsent).getAccess(), user.getAccountAccesses());
        SCAConsentResponseTO response = new SCAConsentResponseTO(consentId);
        scaResponseResolver.updateScaResponseFields(user, response, null, psuMessage, token, scaStatus, scaWeight);
        return response;
    }

    @Override
    public SCAConsentResponseTO grantPIISConsent(ScaInfoTO scaInfoTO, AisConsentTO aisConsent) {
        AisConsentTO piisConsentTO = cleanupForPIIS(aisConsent);
        ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(piisConsentTO);
        //AisConsentBO consentBO = aisConsentMapper.toAisConsentBO(piisConsentTO);

        BearerTokenBO consentToken = null/*authorizationService.consentToken(scaInfoMapper.toScaInfoBO(scaInfoTO), consentBO)*/;// TODO FIXME FOR KEYCLOAK!!! DMI
        SCAConsentResponseTO response = new SCAConsentResponseTO();
        response.setBearerToken(bearerTokenMapper.toBearerTokenTO(consentToken));
        response.setConsentId(aisConsent.getId());
        response.setPsuMessage(consentKeyData.exemptedTemplate());
        response.setScaStatus(ScaStatusTO.EXEMPTED);
        response.setStatusDate(LocalDateTime.now());
        return response;
    }

    @Override
    public void depositCash(ScaInfoTO scaInfoTO, String accountId, AmountTO amount) {
        DepositAccountDetailsBO account = depositAccountService.getAccountDetailsById(accountId, LocalDateTime.now(), false);
        if (!account.isEnabled()) {
            throw blockedSupplier(PAYMENT_PROCESSING_FAILURE, account.getAccount().getIban(), account.getAccount().isBlocked()).get();
        }
        transactionService.depositCash(accountId, amountMapper.toAmountBO(amount), scaInfoTO.getUserLogin());
    }

    @Override
    public List<AccountAccessTO> getAccountAccesses(String userId) {
        UserBO user = userService.findById(userId);
        UserTO userTO = userMapper.toUserTO(user);
        return userTO.getAccountAccesses();
    }

    @Override
    public void deleteTransactions(String userId, UserRoleTO userRole, String accountId) {
        log.info("User {} attempting delete postings for account: {}", userId, accountId);
        long start = checkPermissionAndStartCount(userId, userRole, accountId);
        depositAccountService.deleteTransactions(accountId);
        log.info("Deleting postings for account: {} Successful, in {} seconds", accountId, (double) (System.nanoTime() - start) / NANO_TO_SECOND);
    }

    @Override
    public void deleteAccount(String userId, UserRoleTO userRole, String accountId) {
        log.info("User {} attempting delete account: {}", userId, accountId);
        long start = checkPermissionAndStartCount(userId, userRole, accountId);
        depositAccountService.deleteAccount(accountId);
        log.info("Deleting account: {} Successful, in {} seconds", accountId, (double) (System.nanoTime() - start) / NANO_TO_SECOND);
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    private long checkPermissionAndStartCount(String userId, UserRoleTO userRole, String accountId) {
        long start = System.nanoTime();
        if (userRole == STAFF && !hasAccess(userId, accountId)) {
            throw MiddlewareModuleException.builder()
                          .devMsg("You dont have permission to modify this account")
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .build();
        }
        log.info("Permission checked for account {} -> OK", accountId);
        return start;
    }

    private boolean hasAccess(String userId, String accountId) {
        return userService.findById(userId).getAccountAccesses().stream()
                       .anyMatch(a -> a.getAccountId().equals(accountId));
    }

    @Override
    @SuppressWarnings("PMD.PrematureDeclaration")
    public void deleteUser(String userId, UserRoleTO userRole, String userToDeleteId) {
        log.info("User {} attempting delete user: {}", userId, userToDeleteId);
        long start = System.nanoTime();
        if (userRole == STAFF && !userService.findById(userToDeleteId).getBranch().equals(userId)) {
            throw MiddlewareModuleException.builder()
                          .devMsg("You dont have permission to modify this user")
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .build();
        }
        depositAccountService.deleteUser(userToDeleteId);
        log.info("Deleting user: {} Successful, in {} seconds", userToDeleteId, (double) (System.nanoTime() - start) / NANO_TO_SECOND);
    }

    @Override
    public AccountReportTO getAccountReport(String accountId) {
        long start = System.nanoTime();
        AccountDetailsTO details = getDepositAccountById(accountId, LocalDateTime.now(), true);
        log.info("Loaded details with balances in {} seconds", TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
        start = System.nanoTime();
        List<UserTO> users = userMapper.toUserTOList(userService.findUsersByIban(details.getIban()));
        log.info("Loaded users in {} seconds", TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
        return new AccountReportTO(details, users);
    }

    @Override
    public boolean changeStatus(String accountId, boolean isSystemBlock) {
        DepositAccountBO account = depositAccountService.getAccountById(accountId);

        boolean lockStatusToSet = isSystemBlock ? !account.isSystemBlocked() : !account.isBlocked();
        depositAccountService.changeAccountsBlockedStatus(Collections.singleton(accountId), isSystemBlock, lockStatusToSet);

        return lockStatusToSet;
    }

    private void checkPresentAccountsAndOwner(String iban, UserBO user) {
        if (!user.isEnabled()) {
            throw MiddlewareModuleException.builder()
                          .errorCode(USER_IS_BLOCKED)
                          .devMsg("User is blocked, cannot create new account.")
                          .build();
        }

        List<DepositAccountBO> accountsPresentByIban = depositAccountService.getAccountsByIbanAndParamCurrency(iban, "");

        if (CollectionUtils.isNotEmpty(accountsPresentByIban) && !user.getLogin().equals(accountsPresentByIban.get(0).getName())) {
            throw MiddlewareModuleException.builder()
                          .errorCode(ACCOUNT_CREATION_VALIDATION_FAILURE)
                          .devMsg("The IBAN you're trying to create account for is already busy by another user")
                          .build();
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
}
