package de.adorsys.ledgers.middleware.impl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.middleware.api.domain.account.*;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.impl.converter.*;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.domain.ScaValidationBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.AuthorizationService;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiddlewareAccountManagementServiceImplTest {
    private static final String WRONG_ID = "wrong id";
    private static final String ACCOUNT_ID = "id";
    private static final String IBAN = "DE91100000000123456789";
    private static final String CORRECT_USER_ID = "kjk345knkj45";
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final LocalDateTime TIME = LocalDateTime.MIN;
    private static final String USER_LOGIN = "userLogin";
    private static final String USER_ID = "kjk345knkj45";
    private static final String SCA_ID = "scaId";
    private static final String SCA_METHOD_ID = "scaMethodId";
    private static final String AUTH_CODE = "123456";
    private static final String AUTHORISATION_ID = "authorisationId";

    @InjectMocks
    private MiddlewareAccountManagementServiceImpl middlewareService;
    @Mock
    private PaymentConverter paymentConverter;
    @Mock
    private DepositAccountService depositAccountService;
    @Mock
    DepositAccountTransactionService transactionService;
    @Mock
    private AccountDetailsMapper accountDetailsMapper;
    @Mock
    private UserService userService;
    @Mock
    private AccessService accessService;
    @Mock
    private AmountMapper amountMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PageMapper pageMapper;
    @Mock
    private ScaInfoMapper scaInfoMapper;
    @Mock
    private SCAUtils scaUtils;
    @Mock
    private BearerTokenMapper bearerTokenMapper;
    @Mock
    private AisConsentBOMapper aisConsentMapper;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private SCAOperationService scaOperationService;
    @Mock
    private ScaResponseResolver scaResponseResolver;

    private static final ObjectMapper MAPPER = getObjectMapper();

    @Test
    void getAccountsByIbanAndCurrency() {
        // Given
        when(depositAccountService.getAccountsByIbanAndParamCurrency(any(), any())).thenAnswer(i -> Collections.singletonList(getDepositAccountBO()));
        when(accountDetailsMapper.toAccountDetailsList(any())).thenReturn(Collections.singletonList(getAccountDetailsTO()));

        // When
        List<AccountDetailsTO> result = middlewareService.getAccountsByIbanAndCurrency(IBAN, "EUR");

        // Then
        assertNotNull(result);
        assertThat(result.get(0)).isEqualTo(getAccountDetailsTO());
        verify(depositAccountService, times(1)).getAccountsByIbanAndParamCurrency(IBAN, "EUR");
        verify(accountDetailsMapper, times(1)).toAccountDetailsList(Collections.singletonList(getDepositAccountBO()));
    }

    @Test
    void createDepositAccount() {
        // Given
        when(userService.findById(any())).thenReturn(buildUserBO());
        when(accountDetailsMapper.toDepositAccountBO(any())).thenReturn(getDepositAccountBO());
        when(depositAccountService.createNewAccount(any(), any(), any())).thenReturn(getDepositAccountBO());
        when(depositAccountService.getAccountsByIbanAndParamCurrency(any(), any())).thenAnswer(i -> Collections.singletonList(getDepositAccountBO()));

        // When
        middlewareService.createDepositAccount(CORRECT_USER_ID, buildScaInfoTO(), getAccountDetailsTO());

        // Then
        verify(depositAccountService, times(1)).createNewAccount(getDepositAccountBO(), USER_LOGIN, null);
        verify(accountDetailsMapper, times(1)).toDepositAccountBO(getAccountDetailsTO());
        verify(userService, times(1)).findById(CORRECT_USER_ID);
    }

    @Test
    void createDepositAccount_currencyNull() {
        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareService.createDepositAccount(CORRECT_USER_ID, buildScaInfoTO(), new AccountDetailsTO()));
    }

    @Test
    void createDepositAccount_accountValidationFailure() {
        // Given
        when(userService.findById(any())).thenReturn(buildUserBO());
        when(depositAccountService.getAccountsByIbanAndParamCurrency(any(), any())).thenAnswer(i -> Collections.singletonList(new DepositAccountBO()));

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareService.createDepositAccount(CORRECT_USER_ID, buildScaInfoTO(), getAccountDetailsTO()));
    }

    @Test
    void getDepositAccountById_Success() {
        // Given
        DepositAccountDetailsBO depositAccountDetailsBO = getDepositAccountDetailsBO();
        when(depositAccountService.getAccountDetailsById(ACCOUNT_ID, TIME, true)).thenReturn(depositAccountDetailsBO);
        when(accountDetailsMapper.toAccountDetailsTO(depositAccountDetailsBO)).thenReturn(getAccountDetailsTO());

        // When
        AccountDetailsTO accountDetails = middlewareService.getDepositAccountById(ACCOUNT_ID, TIME, false);

        // Then
        assertNotNull(accountDetails);
        assertNotNull(accountDetails.getBalances());
        assertThat(accountDetails.getBalances().size()).isEqualTo(2);
    }

    @Test
    void getAccountDetailsByAccountId_wrong_id() {
        // Given
        when(depositAccountService.getAccountDetailsById(WRONG_ID, TIME, true)).thenThrow(DepositModuleException.class);

        // Then
        assertThrows(DepositModuleException.class, () -> middlewareService.getDepositAccountById(WRONG_ID, TIME, false));
    }

    @Test
    void getAccountDetailsByAccountId_Failure_depositAccount_Not_Found() {
        // Given
        when(depositAccountService.getAccountDetailsById(ACCOUNT_ID, TIME, true)).thenThrow(DepositModuleException.class);

        // Then
        assertThrows(DepositModuleException.class, () -> middlewareService.getDepositAccountById(ACCOUNT_ID, TIME, false));
    }

    @Test
    void getDepositAccountByIban() {
        // Given
        DepositAccountDetailsBO accountBO = getDepositAccountDetailsBO();
        AccountDetailsTO accountDetailsTO = getAccount(AccountDetailsTO.class);
        when(depositAccountService.getDetailsByIban(any(), any(), anyBoolean())).thenReturn(accountBO);
        when(accountDetailsMapper.toAccountDetailsTO(accountBO)).thenReturn(accountDetailsTO);

        // When
        AccountDetailsTO details = middlewareService.getDepositAccountByIban(IBAN, TIME, false);

        // Then
        assertNotNull(details);
        assertEquals(accountDetailsTO, details);
        verify(depositAccountService, times(1)).getDetailsByIban(IBAN, TIME, false);
        verify(accountDetailsMapper, times(1)).toAccountDetailsTO(accountBO);
    }

    @Test
    void getDepositAccountByIban_depositAccountNotFoundException() {
        // Given
        when(depositAccountService.getDetailsByIban(IBAN, TIME, false)).thenThrow(DepositModuleException.class);

        // When
        assertThrows(DepositModuleException.class, () -> middlewareService.getDepositAccountByIban(IBAN, TIME, false));
    }

    @Test
    void getAllAccountDetailsByUserLogin() {
        // Given
        String userLogin = "spe";
        AccountDetailsTO account = new AccountDetailsTO();
        List<AccountAccessBO> accessBOList = getDataFromFile("account-access-bo-list.yml", new TypeReference<List<AccountAccessBO>>() {
        });
        UserBO userBO = new UserBO();
        userBO.getAccountAccesses().addAll(accessBOList);
        when(userService.findByLogin(userLogin)).thenReturn(userBO);
        when(accountDetailsMapper.toAccountDetailsList(any())).thenReturn(Collections.singletonList(account));

        // When
        List<AccountDetailsTO> details = middlewareService.getAllAccountDetailsByUserLogin(userLogin);

        // Then
        assertEquals(1, details.size());
        assertEquals(account, details.get(0));
        verify(userService, times(1)).findByLogin(userLogin);
        verify(depositAccountService, times(1)).getAccountByIbanAndCurrency(any(), any());
        verify(accountDetailsMapper, times(1)).toAccountDetailsList(any());
    }

    @Test
    void getTransactionById() {
        // Given
        when(depositAccountService.getTransactionById(anyString(), anyString())).thenReturn(readYml(TransactionDetailsBO.class, "TransactionBO.yml"));
        when(paymentConverter.toTransactionTO(any())).thenReturn(readYml(TransactionTO.class, "TransactionTO.yml"));

        // When
        TransactionTO result = middlewareService.getTransactionById("ACCOUNT_ID", "POSTING_ID");

        // Then
        assertNotNull(result);
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(TransactionTO.class, "TransactionTO.yml"));
    }

    @Test
    void getTransactionById_Failure() {
        // Given
        when(depositAccountService.getTransactionById(anyString(), anyString())).thenThrow(DepositModuleException.class);

        // Then
        assertThrows(DepositModuleException.class, () -> middlewareService.getTransactionById("ACCOUNT_ID", "POSTING_ID"));
    }

    @Test
    void getTransactionsByDates() {
        // Given
        when(accessService.getTimeAtEndOfTheDay(any())).thenReturn(LocalDateTime.now().minusDays(1));
        when(accessService.getTimeAtEndOfTheDay(any())).thenReturn(LocalDateTime.now());
        when(depositAccountService.getTransactionsByDates(any(), any(), any())).thenAnswer(i -> Collections.singletonList(readYml(TransactionDetailsBO.class, "TransactionBO.yml")));
        when(paymentConverter.toTransactionTOList(any())).thenReturn(Collections.singletonList(readYml(TransactionTO.class, "TransactionTO.yml")));

        // When
        List<TransactionTO> result = middlewareService.getTransactionsByDates("ACCOUNT_ID", LocalDate.now().minusDays(1), LocalDate.now());

        // Then
        assertNotNull(result);
        assertThat(result.get(0)).isEqualToComparingFieldByFieldRecursively(readYml(TransactionTO.class, "TransactionTO.yml"));
    }

    @Test
    void getTransactionsByDatesPaged() {
        // Given
        when(accessService.getTimeAtEndOfTheDay(any())).thenReturn(LocalDateTime.now().minusDays(1));
        when(accessService.getTimeAtEndOfTheDay(any())).thenReturn(LocalDateTime.now());
        when(depositAccountService.getTransactionsByDatesPaged(any(), any(), any(), any())).thenReturn(Page.empty());
        when(pageMapper.toCustomPageImpl(any())).thenReturn(new CustomPageImpl<>());

        // When
        CustomPageImpl<TransactionTO> result = middlewareService.getTransactionsByDatesPaged("ACCOUNT_ID", LocalDate.now().minusDays(1), LocalDate.now(), getCustomPageableImpl());

        // Then
        assertNotNull(result);
        verify(pageMapper, times(1)).toCustomPageImpl(Page.empty());
    }

    @Test
    void confirmFundsAvailability() {
        // Given
        when(accountDetailsMapper.toFundsConfirmationRequestBO(any())).thenReturn(getFundsConfirmationRequestBO());
        when(depositAccountService.confirmationOfFunds(any())).thenReturn(true);

        // When
        boolean result = middlewareService.confirmFundsAvailability(getFundsConfirmationRequestTO());

        // Then
        assertThat(result).isTrue();
        verify(accountDetailsMapper, times(1)).toFundsConfirmationRequestBO(getFundsConfirmationRequestTO());
        verify(depositAccountService, times(1)).confirmationOfFunds(getFundsConfirmationRequestBO());
    }

    @Test
    void createAccount() {
        // Given
        when(depositAccountService.findByAccountNumberPrefix(any())).thenReturn(Collections.singletonList(getDepositAccountBO()));
        when(userMapper.toAccountAccessListTO(any())).thenReturn(Collections.EMPTY_LIST);
        when(userService.findById(any())).thenReturn(buildUserBO());
        when(accessService.filterOwnedAccounts(any())).thenReturn(Collections.EMPTY_LIST);

        // When
        middlewareService.createDepositAccount(buildScaInfoTO(), "06", "07", getAccountDetailsTO());

        // Then
        verify(depositAccountService, times(1)).findByAccountNumberPrefix("06");
        verify(userService, times(2)).findById(CORRECT_USER_ID);
    }

    // TODO: to be fixed: https://git.adorsys.de/adorsys/xs2a/ledgers/-/issues/237
    @Test
    void createDepositAccount_emptyAccount() {
        // Given
        when(depositAccountService.findByAccountNumberPrefix(any())).thenReturn(Collections.EMPTY_LIST);

        // Then
        assertThrows(NullPointerException.class, () -> middlewareService.createDepositAccount(buildScaInfoTO(), "accountNumberPrefix", "accountNumberSuffix", getAccountDetailsTO()));
    }

    @Test
    void createDepositAccount_accountCreationFailure() {
        // Given
        when(depositAccountService.findByAccountNumberPrefix(any())).thenReturn(Collections.singletonList(getDepositAccountBO()));
        when(userMapper.toAccountAccessListTO(any())).thenReturn(buildAccountAccessesTO());
        when(userService.findById(any())).thenReturn(buildUserBO());

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareService.createDepositAccount(buildScaInfoTO(), "accountNumberPrefix", "accountNumberSuffix", getAccountDetailsTO()));
    }

    @Test
    void createDepositAccount_accountSuffixAndPrefixExists() {
        // Given
        when(depositAccountService.findByAccountNumberPrefix(any())).thenReturn(Collections.singletonList(getDepositAccountBO()));
        when(userMapper.toAccountAccessListTO(any())).thenReturn(Collections.EMPTY_LIST);
        when(userService.findById(any())).thenReturn(buildUserBO());
        when(accessService.filterOwnedAccounts(any())).thenReturn(Collections.singletonList("0102"));

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareService.createDepositAccount(buildScaInfoTO(), "01", "02", getAccountDetailsTO()));
    }

    @Test
    void createDepositAccount_userNotOwner() {
        // Given
        when(depositAccountService.findByAccountNumberPrefix(any())).thenReturn(Collections.singletonList(getDepositAccountBO()));
        when(userMapper.toAccountAccessListTO(any())).thenReturn(Collections.EMPTY_LIST);
        when(userService.findById(any())).thenReturn(buildUserBO());
        when(accessService.filterOwnedAccounts(any())).thenReturn(Collections.singletonList(IBAN));

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareService.createDepositAccount(buildScaInfoTO(), "accountNumberPrefix", "accountNumberSuffix", getAccountDetailsTO()));
    }

    @Test
    void listDepositAccounts() {
        // Given
        when(accessService.loadCurrentUser(CORRECT_USER_ID)).thenReturn(buildUserBO());
        when(userMapper.toUserTO(buildUserBO())).thenReturn(buildUserTO());
        when(depositAccountService.getAccountDetailsByIbanAndCurrency(anyString(), any(), any(LocalDateTime.class), anyBoolean())).thenReturn(getDepositAccountDetailsBO());
        when(accountDetailsMapper.toAccountDetailsTO(any(DepositAccountDetailsBO.class))).thenReturn(getAccountDetailsTO());

        // When
        List<AccountDetailsTO> accountsToBeTested = middlewareService.listDepositAccounts(CORRECT_USER_ID);

        // Then
        assertNotNull(accountsToBeTested);
        assertThat(accountsToBeTested).isNotEmpty();
        verify(accessService, times(1)).loadCurrentUser(CORRECT_USER_ID);
        verify(depositAccountService, times(1)).getAccountDetailsByIbanAndCurrency(anyString(), any(), any(LocalDateTime.class), anyBoolean());
        verify(accountDetailsMapper, times(1)).toAccountDetailsTO(any(DepositAccountDetailsBO.class)); // only one element in the list
    }

    @Test
    void listDepositAccountsByBranch() {
        // user
        UserBO user = getDataFromFile("user.yml", new TypeReference<UserBO>() {
        });
        // accounts
        List<DepositAccountDetailsBO> accounts = new ArrayList<>();
        accounts.add(getDepositAccountDetailsBO());

        // Given
        when(accessService.loadCurrentUser(CORRECT_USER_ID)).thenReturn(user);
        when(depositAccountService.findDetailsByBranch(anyString())).thenReturn(accounts);
        when(accountDetailsMapper.toAccountDetailsTO(any(DepositAccountDetailsBO.class))).thenReturn(getAccountDetailsTO());

        // When
        List<AccountDetailsTO> accountsToBeTested = middlewareService.listDepositAccountsByBranch(CORRECT_USER_ID);

        // Then
        assertNotNull(accountsToBeTested);
        assertThat(accountsToBeTested).isNotEmpty();
        verify(accessService, times(1)).loadCurrentUser(CORRECT_USER_ID);
        verify(depositAccountService, times(1)).findDetailsByBranch(anyString());
        verify(accountDetailsMapper, times(1)).toAccountDetailsTO(any(DepositAccountDetailsBO.class)); // only one element in the list
    }

    @Test
    void listDepositAccountsByBranchPaged() {
        // Given
        when(accessService.loadCurrentUser(any())).thenReturn(buildUserBO());
        when(depositAccountService.findDetailsByBranchPaged(any(), any(), any())).thenReturn(getPage());
        when(accountDetailsMapper.toAccountDetailsTO(any(DepositAccountDetailsBO.class))).thenReturn(getAccountDetailsTO());
        when(pageMapper.toCustomPageImpl(any())).thenReturn(getPageImpl());

        // When
        CustomPageImpl<AccountDetailsTO> result = middlewareService.listDepositAccountsByBranchPaged("ACCOUNT_ID", "queryParam", getCustomPageableImpl());

        // Then
        assertNotNull(result);
        verify(accountDetailsMapper, times(1)).toAccountDetailsTO(getDepositAccountDetailsBO());
    }

    @Test
    void iban() {
        // Given
        when(depositAccountService.readIbanById(any())).thenReturn(IBAN);

        // When
        String iban = middlewareService.iban("id");

        // Then
        assertNotNull(iban);
        assertThat(iban).isNotEmpty();
        verify(depositAccountService, times(1)).readIbanById("id");
    }

    @Test
    void startSCA() {
        // Given
        when(scaInfoMapper.toScaInfoBO(any())).thenReturn(buildScaInfoBO());
        when(scaUtils.userBO(any())).thenReturn(buildUserBO());
        when(scaUtils.user((UserBO) any())).thenReturn(buildUserTO());
        when(scaUtils.authorisationId(any())).thenReturn("id");
        when(scaUtils.hasSCA(any())).thenReturn(false);
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(getBearerTokenTO());
        when(aisConsentMapper.toAisConsentBO(any())).thenReturn(getAisConsentBO());

        // When
        SCAConsentResponseTO result = middlewareService.startSCA(buildScaInfoTO(), "consentId", getAisConsentTO());

        // Then
        assertNotNull(result);
        verify(scaInfoMapper, times(1)).toScaInfoBO(buildScaInfoTO());
        verify(aisConsentMapper, times(1)).toAisConsentBO(getAisConsentTO());
    }

    @Test
    void startSCA_scaNotRequired() {
        // Given
        when(scaInfoMapper.toScaInfoBO(any())).thenReturn(buildScaInfoBO());
        when(scaUtils.userBO(any())).thenReturn(buildUserBO());
        when(scaUtils.user((UserBO) any())).thenReturn(buildUserTO());
        when(scaUtils.authorisationId(any())).thenReturn("id");
        when(scaUtils.hasSCA(any())).thenReturn(true);
        when(aisConsentMapper.toAisConsentBO(any())).thenReturn(getAisConsentBO());
        when(userService.storeConsent(any())).thenReturn(getAisConsentBO());
        when(accessService.resolveMinimalScaWeightForConsent(any(), any())).thenReturn(10);
        when(scaOperationService.createAuthCode(any(), any())).thenReturn(getSCAOperationBO());

        // When
        SCAConsentResponseTO result = middlewareService.startSCA(buildScaInfoTO(), "consentId", getAisConsentTO());

        // Then
        assertNotNull(result);
        verify(scaInfoMapper, times(1)).toScaInfoBO(buildScaInfoTO());
        verify(aisConsentMapper, times(2)).toAisConsentBO(getAisConsentTO());
    }

    @Test
    void loadSCAForAisConsent() {
        // Given
        when(scaUtils.userBO(any())).thenReturn(buildUserBO());
        when(userService.loadConsent(any())).thenReturn(getAisConsentBO());
        when(aisConsentMapper.toAisConsentTO(any())).thenReturn(getAisConsentTO());
        when(scaUtils.loadAuthCode(any())).thenReturn(getSCAOperationBO());
        when(accessService.resolveMinimalScaWeightForConsent(any(), any())).thenReturn(10);
        when(userMapper.toUserTO(any())).thenReturn(buildUserTO());

        // When
        SCAConsentResponseTO result = middlewareService.loadSCAForAisConsent(CORRECT_USER_ID, "consentId", "authorisationId");

        // Then
        assertNotNull(result);
        verify(userMapper, times(1)).toUserTO(buildUserBO());
    }

    @Test
    void selectSCAMethodForAisConsent() {
        // Given
        when(scaUtils.userBO(any())).thenReturn(buildUserBO());
        when(userService.loadConsent(any())).thenReturn(getAisConsentBO());
        when(aisConsentMapper.toAisConsentTO(any())).thenReturn(getAisConsentTO());
        when(accessService.resolveMinimalScaWeightForConsent(any(), any())).thenReturn(10);
        when(userMapper.toUserTO(any())).thenReturn(buildUserTO());

        // When
        SCAConsentResponseTO result = middlewareService.selectSCAMethodForAisConsent(CORRECT_USER_ID, "consentId", "authorisationId", "scaMethodId");

        // Then
        assertNotNull(result);
        verify(userMapper, times(1)).toUserTO(buildUserBO());
    }

    @Test
    void authorizeConsent() {
        // Given
        when(userService.loadConsent(any())).thenReturn(getAisConsentBO());
        when(aisConsentMapper.toAisConsentTO(any())).thenReturn(getAisConsentTO());
        when(scaUtils.userBO(any())).thenReturn(buildUserBO());
        when(scaUtils.user((UserBO) any())).thenReturn(buildUserTO());
        when(accessService.resolveMinimalScaWeightForConsent(any(), any())).thenReturn(10);
        when(scaOperationService.validateAuthCode(any(), any(), any(), any(), anyInt())).thenReturn(getScaValidationBO(true));
        when(scaUtils.loadAuthCode(any())).thenReturn(getSCAOperationBO());
        when(scaOperationService.authenticationCompleted(any(), any())).thenReturn(false);
        when(authorizationService.consentToken(any(), any())).thenReturn(getBearerTokenBO());
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(getBearerTokenTO());

        // When
        SCAConsentResponseTO result = middlewareService.authorizeConsent(buildScaInfoTO(), "consentId");

        // Then
        assertNotNull(result);
    }

    @Test
    void authorizeConsent_wrongAuthCode() {
        // Given
        when(userService.loadConsent(any())).thenReturn(getAisConsentBO());
        when(aisConsentMapper.toAisConsentTO(any())).thenReturn(getAisConsentTO());
        when(scaUtils.userBO(any())).thenReturn(buildUserBO());
        when(accessService.resolveMinimalScaWeightForConsent(any(), any())).thenReturn(10);
        when(scaOperationService.validateAuthCode(any(), any(), any(), any(), anyInt())).thenThrow(ScaModuleException.class);

        // Then
        assertThrows(ScaModuleException.class, () -> middlewareService.authorizeConsent(buildScaInfoTO(), "consentId"));
    }

    @Test
    void grantAisConsent() {
        // Given
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(getBearerTokenTO());

        // When
        SCAConsentResponseTO result = middlewareService.grantAisConsent(buildScaInfoTO(), getAisConsentTO());

        // Then
        assertNotNull(result);
    }

    @Test
    void depositCashDelegatesToDepositAccountService() {
        // Given
        when(depositAccountService.getAccountDetailsById(anyString(), any(), anyBoolean())).thenReturn(getAccountDetails(false));
        doNothing().when(transactionService).depositCash(eq(ACCOUNT_ID), any(), any());

        // When
        middlewareService.depositCash(buildScaInfoTO(), ACCOUNT_ID, new AmountTO());

        // Then
        verify(transactionService, times(1)).depositCash(eq(ACCOUNT_ID), any(), any());
    }

    @Test
    void depositCashWrapsNotFoundException() {
        // Given
        when(depositAccountService.getAccountDetailsById(anyString(), any(), anyBoolean())).thenReturn(getAccountDetails(true));

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareService.depositCash(buildScaInfoTO(), ACCOUNT_ID, new AmountTO()));
    }

    @Test
    void shouldReturnUserAccountAccess() {
        // Given
        when(userService.findById(CORRECT_USER_ID)).thenReturn(buildUserBO());
        when(userMapper.toUserTO(buildUserBO())).thenReturn(buildUserTO());

        // When
        List<AccountAccessTO> accountAccesses = middlewareService.getAccountAccesses(CORRECT_USER_ID);

        // Then
        assertThat(accountAccesses).isNotEmpty();
        assertEquals(1, accountAccesses.size());

        AccountAccessTO accountAccess = getFirstAccountAccess(accountAccesses);

        assertEquals(IBAN, accountAccess.getIban());
        assertEquals(AccessTypeTO.OWNER, accountAccess.getAccessType());
    }

    @Test
    void deleteTransactions_userRoleCustomer() {
        // When
        middlewareService.deleteTransactions(CORRECT_USER_ID, UserRoleTO.CUSTOMER, ACCOUNT_ID);
    }

    @Test
    void deleteTransactions_userRoleStaff() {
        // Given
        when(userService.findById(any())).thenReturn(getUserBO());
        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareService.deleteTransactions(CORRECT_USER_ID, UserRoleTO.STAFF, null));
    }

    @Test
    void getAccountReport() {
        // Given
        when(userMapper.toUserTOList(any())).thenReturn(Collections.singletonList(buildUserTO()));
        when(userService.findUsersByIban(any())).thenReturn(Collections.singletonList(buildUserBO()));
        when(depositAccountService.getAccountDetailsById(any(), any(LocalDateTime.class), anyBoolean())).thenReturn(getDepositAccountDetailsBO());
        when(accountDetailsMapper.toAccountDetailsTO(any(DepositAccountDetailsBO.class))).thenReturn(getAccountDetailsTO());

        // When
        AccountReportTO result = middlewareService.getAccountReport(ACCOUNT_ID);

        // Then
        assertNotNull(result);
        verify(userMapper, times(1)).toUserTOList(Collections.singletonList(buildUserBO()));
        verify(accountDetailsMapper, times(1)).toAccountDetailsTO(getDepositAccountDetailsBO());

    }

    @Test
    void getAccountsByOptionalBranchPaged() {
        when(depositAccountService.findByBranchIdsAndMultipleParams(anyList(),anyString(), anyBoolean(), any())).thenReturn(getPageWithAccount());
        when(accountDetailsMapper.toAccountDetailsTO(any(DepositAccountBO.class))).thenReturn(getAccountDetailsTO());
        when(pageMapper.toCustomPageImpl(any())).thenReturn(new CustomPageImpl<>(1, 1, 1, 1, 1L, false, true, false, true, Collections.singletonList(getAccountDetailsTO())));
        CustomPageImpl<AccountDetailsTO> result = middlewareService.getAccountsByBranchAndMultipleParams("countryCode", "branchId", USER_ID, IBAN, false, new CustomPageableImpl(0, 1));
        assertEquals(result.getContent(), Collections.singletonList(getAccountDetailsTO()));
    }

    private CustomPageImpl<Object> getPageImpl() {
        return new CustomPageImpl<>();
    }

    private Page<DepositAccountDetailsBO> getPage() {
        return new PageImpl<>(Collections.singletonList(getDepositAccountDetailsBO()));
    }

    private Page<DepositAccountBO> getPageWithAccount() {
        return new PageImpl<>(Collections.singletonList(getDepositAccountDetailsBO().getAccount()));
    }

    private CustomPageableImpl getCustomPageableImpl() {
        return new CustomPageableImpl(1, 5);
    }

    private UserBO getUserBO() {
        UserBO user = buildUserBO();
        user.getAccountAccesses().get(0).setAccountId(ACCOUNT_ID);
        return user;
    }

    private ScaValidationBO getScaValidationBO(boolean validAuthCode) {
        return new ScaValidationBO("authConfirmationCode", validAuthCode, ScaStatusBO.FINALISED, 0);
    }

    private SCAOperationBO getSCAOperationBO() {
        return new SCAOperationBO();
    }

    private AisConsentBO getAisConsentBO() {
        return new AisConsentBO("id", "userId", "tppId", 4, new AisAccountAccessInfoBO(), LocalDate.now().plusDays(5), false);
    }

    private AisConsentTO getAisConsentTO() {
        return new AisConsentTO("id", "userId", "tppId", 4, new AisAccountAccessInfoTO(), LocalDate.now().plusDays(5), false);
    }

    private BearerTokenBO getBearerTokenBO() {
        BearerTokenBO token = new BearerTokenBO();
        token.setAccess_token("access_token");
        token.setAccessTokenObject(new AccessTokenBO());
        token.setExpires_in(30);
        token.setRefresh_token("refresh_token");
        token.setToken_type("Bearer");
        return token;
    }

    private BearerTokenTO getBearerTokenTO() {
        return new BearerTokenTO("access_token", "Bearer", 30, "refresh_token", new AccessTokenTO());
    }

    private static <T> T getAccount(Class<T> aClass) {
        try {
            return MAPPER.readValue(PaymentConverter.class.getResourceAsStream("AccountDetails.yml"), aClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }

    private DepositAccountDetailsBO getDepositAccountDetailsBO() {
        return readYml(DepositAccountDetailsBO.class, "DepositAccountDetailsBO.yml");
    }

    private AccountDetailsTO getAccountDetailsTO() {
        return readYml(AccountDetailsTO.class, "AccountDetailsTO.yml");
    }

    private AccountAccessTO getFirstAccountAccess(List<AccountAccessTO> accountAccesses) {
        return accountAccesses.get(0);
    }

    //    todo: replace by javatar-commons version 0.7
    private <T> T getDataFromFile(String fileName, TypeReference<T> typeReference) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        InputStream inputStream = getClass().getResourceAsStream(fileName);
        try {
            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            throw new IllegalStateException("File not found");
        }
    }

    private static <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return MAPPER.readValue(PaymentConverter.class.getResourceAsStream(fileName), aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    private DepositAccountDetailsBO getAccountDetails(boolean isBlocked) {
        DepositAccountBO account = new DepositAccountBO();
        account.setBlocked(isBlocked);
        return new DepositAccountDetailsBO(account, Collections.emptyList());
    }

    private static UserBO buildUserBO() {
        UserBO user = new UserBO();
        user.setId(CORRECT_USER_ID);
        user.setAccountAccesses(buildAccountAccessesBO());
        user.setLogin(USER_LOGIN);
        return user;
    }

    private static List<AccountAccessBO> buildAccountAccessesBO() {
        return Collections.singletonList(new AccountAccessBO(IBAN, AccessTypeBO.OWNER));
    }

    private static UserTO buildUserTO() {
        UserTO user = new UserTO();
        user.setId(CORRECT_USER_ID);
        user.setAccountAccesses(buildAccountAccessesTO());
        return user;
    }

    private static List<AccountAccessTO> buildAccountAccessesTO() {
        AccountAccessTO access = new AccountAccessTO();
        access.setIban(IBAN);
        access.setAccessType(AccessTypeTO.OWNER);
        return Collections.singletonList(access);
    }

    private static ScaInfoTO buildScaInfoTO() {
        ScaInfoTO info = new ScaInfoTO();
        info.setUserId(USER_ID);
        info.setAuthorisationId(AUTHORISATION_ID);
        info.setScaId(SCA_ID);
        info.setUserRole(UserRoleTO.CUSTOMER);
        info.setAuthCode(AUTH_CODE);
        info.setScaMethodId(SCA_METHOD_ID);
        info.setUserLogin(USER_LOGIN);
        return info;
    }

    private static ScaInfoBO buildScaInfoBO() {
        ScaInfoBO info = new ScaInfoBO();
        info.setUserId(USER_ID);
        info.setAuthorisationId(AUTHORISATION_ID);
        info.setScaId(SCA_ID);
        info.setUserRole(UserRoleBO.CUSTOMER);
        info.setAuthCode(AUTH_CODE);
        info.setScaMethodId(SCA_METHOD_ID);
        info.setUserLogin(USER_LOGIN);
        return info;
    }

    private DepositAccountBO getDepositAccountBO() {
        return new DepositAccountBO("id", IBAN, "bban", "pan", "maskedPan", "msisdn", EUR, USER_LOGIN, "product", AccountTypeBO.CASH, "bic", "linkedAccounts", AccountUsageBO.PRIV, "details", false, false, "branch");
    }

    private FundsConfirmationRequestBO getFundsConfirmationRequestBO() {
        return new FundsConfirmationRequestBO("psuId", new AccountReferenceBO(), new AmountBO(), "cardNumber", "payee");
    }

    private FundsConfirmationRequestTO getFundsConfirmationRequestTO() {
        return new FundsConfirmationRequestTO("psuId", new AccountReferenceTO(), new AmountTO(), "cardNumber", "payee");
    }
}
