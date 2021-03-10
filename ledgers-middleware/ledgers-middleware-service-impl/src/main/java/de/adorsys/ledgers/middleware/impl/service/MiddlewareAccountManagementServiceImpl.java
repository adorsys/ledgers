package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.FundsConfirmationRequestBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.middleware.api.domain.Constants;
import de.adorsys.ledgers.middleware.api.domain.account.*;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.payment.ConsentKeyDataTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.impl.converter.*;
import de.adorsys.ledgers.um.api.domain.UserBO;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.ACCOUNT_CREATION_VALIDATION_FAILURE;
import static de.adorsys.ledgers.util.DateTimeUtils.getTimeAtEndOfTheDay;

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
    private final SCAUtils scaUtils;
    private final AccessService accessService;
    private final AmountMapper amountMapper;
    private final PageMapper pageMapper;
    private final ScaResponseResolver scaResponseResolver;
    private final KeycloakTokenService tokenService;
    private final KeycloakDataService keycloakDataService;

    @Value("${ledgers.token.lifetime.seconds.sca:10800}")
    private int scaTokenLifeTime;
    @Value("${ledgers.token.lifetime.seconds.full:7776000}")
    private int fullTokenLifeTime;
    @Value("${ledgers.sca.final.weight:100}")
    private int finalWeight;

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

        checkPresentAccountsAndOwner(depositAccount.getIban(), user);//TODO Consider moving to Filter
        DepositAccountBO createdAccount = depositAccountService.createNewAccount(accountDetailsMapper.toDepositAccountBO(depositAccount), user.getLogin(), user.getBranch());
        accessService.updateAccountAccessNewAccount(createdAccount, user, finalWeight);
    }

    @Override
    public AccountDetailsTO getDepositAccountById(String accountId, LocalDateTime time, boolean withBalance) {
        DepositAccountDetailsBO accountDetailsBO = depositAccountService.getAccountDetailsById(accountId, time, true);
        return accountDetailsMapper.toAccountDetailsTO(accountDetailsBO);
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
        LocalDateTime dateTimeTo = getTimeAtEndOfTheDay(dateTo == null ? today : dateTo);

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
        LocalDateTime dateTimeTo = getTimeAtEndOfTheDay(dateTo == null ? today : dateTo);

        return pageMapper.toCustomPageImpl(depositAccountService.getTransactionsByDatesPaged(accountId, dateTimeFrom, dateTimeTo, PageRequest.of(pageable.getPage(), pageable.getSize()))
                                                   .map(paymentConverter::toTransactionTO));
    }

    @Override
    public boolean confirmFundsAvailability(FundsConfirmationRequestTO request) {
        FundsConfirmationRequestBO requestBO = accountDetailsMapper.toFundsConfirmationRequestBO(request);
        return depositAccountService.confirmationOfFunds(requestBO);
    }

    @Override
    public List<AccountDetailsTO> listDepositAccounts(String userId) {
        UserBO user = userService.findById(userId);
        return user.getAccountIds().stream()
                       .map(id -> depositAccountService.getAccountDetailsById(id, LocalDateTime.now(), true))
                       .map(accountDetailsMapper::toAccountDetailsTO)
                       .collect(Collectors.toList());
    }

    @Override
    public List<AccountDetailsTO> listDepositAccountsByBranch(String userId) {
        UserBO user = userService.findById(userId);
        List<DepositAccountDetailsBO> depositAccounts = depositAccountService.findDetailsByBranch(user.getBranch());
        return accountDetailsMapper.toAccountDetailsTOList(depositAccounts);
    }

    @Override
    public CustomPageImpl<AccountDetailsTO> listDepositAccountsByBranchPaged(String userId, String queryParam, CustomPageableImpl pageable) {
        UserBO user = userService.findById(userId);
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
    public String iban(String id) { //TODO Consider elimination!
        return depositAccountService.readIbanById(id);
    }

    // ======================= CONSENT ======================//
    @Override
    public SCAConsentResponseTO startAisConsent(ScaInfoTO scaInfoTO, String consentId, AisConsentTO aisConsent) {
        UserBO user = scaUtils.userBO(scaInfoTO.getUserLogin());

        boolean isScaRequired = user.hasSCA();
        String psuMessage = new ConsentKeyDataTO(aisConsent).template();
        BearerTokenTO token = isScaRequired
                                      ? tokenService.exchangeToken(scaInfoTO.getAccessToken(), scaTokenLifeTime, Constants.SCOPE_SCA)
                                      : tokenService.exchangeToken(scaInfoTO.getAccessToken(), fullTokenLifeTime, Constants.SCOPE_FULL_ACCESS);
        ScaStatusTO scaStatus = isScaRequired
                                        ? ScaStatusTO.PSUAUTHENTICATED
                                        : ScaStatusTO.EXEMPTED;
        int scaWeight = user.resolveMinimalWeightForIbanSet(aisConsentMapper.toAisConsentBO(aisConsent).getUniqueIbans());
        SCAConsentResponseTO response = new SCAConsentResponseTO(consentId);
        scaResponseResolver.updateScaResponseFields(user, response, null, psuMessage, token, scaStatus, scaWeight);
        userService.storeConsent(aisConsentMapper.toAisConsentBO(aisConsent));
        return response;
    }

    @Override
    public Set<String> getAccountsFromConsent(String consentId) {
        return userService.loadConsent(consentId).getUniqueIbans();
    }

    @Override
    public SCAConsentResponseTO grantPIISConsent(ScaInfoTO scaInfoTO, AisConsentTO aisConsent) {
        aisConsent.cleanupForPIIS();
        ConsentKeyDataTO consentKeyData = new ConsentKeyDataTO(aisConsent);
        BearerTokenTO consentToken = tokenService.exchangeToken(scaInfoTO.getAccessToken(), fullTokenLifeTime, Constants.SCOPE_PARTIAL_ACCESS);
        return new SCAConsentResponseTO(consentToken, aisConsent.getId(), consentKeyData.exemptedTemplate());
    }

    @Override
    public void depositCash(ScaInfoTO scaInfoTO, String accountId, AmountTO amount) {
        transactionService.depositCash(accountId, amountMapper.toAmountBO(amount), scaInfoTO.getUserLogin());
    }


    //TODO Create separate clean up service for this purposes - remove after! https://git.adorsys.de/adorsys/xs2a/psd2-dynamic-sandbox/-/issues/906
    @Override
    public void deleteTransactions(String userId, UserRoleTO userRole, String accountId) {
        log.info("User {} attempting delete postings for account: {}", userId, accountId);
        long start = System.nanoTime();
        depositAccountService.deleteTransactions(accountId);
        log.info("Deleting postings for account: {} Successful, in {} seconds", accountId, (double) (System.nanoTime() - start) / NANO_TO_SECOND);
    }

    @Override
    public void deleteAccount(String userId, UserRoleTO userRole, String accountId) {
        log.info("User {} attempting delete account: {}", userId, accountId);
        long start = System.nanoTime();
        depositAccountService.deleteAccount(accountId);
        log.info("Deleting account: {} Successful, in {} seconds", accountId, (double) (System.nanoTime() - start) / NANO_TO_SECOND);
    }

    @Override
    @SuppressWarnings("PMD.PrematureDeclaration")
    public void deleteUser(String userId, UserRoleTO userRole, String userToDeleteId) {
        log.info("User {} attempting delete user: {}", userId, userToDeleteId);
        long start = System.nanoTime();
        String login = userService.findById(userToDeleteId).getLogin();
        depositAccountService.deleteUser(userToDeleteId);
        keycloakDataService.deleteUser(login);
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

    @Override
    public void changeCreditLimit(String accountId, BigDecimal creditLimit) {
        depositAccountService.changeCreditLimit(accountId, creditLimit);
    }

    private void checkPresentAccountsAndOwner(String iban, UserBO user) { //TODO Consider moving to separate place RequestValidationFilter?
        List<DepositAccountBO> accountsPresentByIban = depositAccountService.getAccountsByIbanAndParamCurrency(iban, "");

        if (CollectionUtils.isNotEmpty(accountsPresentByIban) && !user.getLogin().equals(accountsPresentByIban.get(0).getName())) {
            throw MiddlewareModuleException.builder()
                          .errorCode(ACCOUNT_CREATION_VALIDATION_FAILURE)
                          .devMsg("The IBAN you're trying to create account for is already busy by another user")
                          .build();
        }
    }
}
