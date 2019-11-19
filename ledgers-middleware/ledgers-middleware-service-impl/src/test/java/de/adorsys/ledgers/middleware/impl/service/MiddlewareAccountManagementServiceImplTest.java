package de.adorsys.ledgers.middleware.impl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.impl.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.impl.converter.AmountMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareAccountManagementServiceImplTest {
    private static final String WRONG_ID = "wrong id";
    private static final String ACCOUNT_ID = "id";
    private static final String IBAN = "DE91100000000123456789";
    private static final String CORRECT_USER_ID = "kjk345knkj45";

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
    private DepositAccountPaymentService paymentService;
    @Mock
    private SCAOperationService operationService;
    @Mock
    private PaymentConverter paymentConverter;
    @Mock
    private DepositAccountService accountService;
    @Mock
    private AccountDetailsMapper detailsMapper;
    @Mock
    private UserService userService;
    @Mock
    private AccessService accessService;
    @Mock
    AmountMapper amountMapper;
    @Mock
    private UserMapper userMapper;

    private static ObjectMapper mapper = getObjectMapper();

    @Test
    public void getAccountDetailsByAccountId() {
        when(accountService.getDepositAccountById(ACCOUNT_ID, TIME, true)).thenReturn(getDepositAccountDetailsBO());

        when(detailsMapper.toAccountDetailsTO(any())).thenReturn(getAccount(AccountDetailsTO.class));
        AccountDetailsTO details = middlewareService.getDepositAccountById(ACCOUNT_ID, TIME, false);

        assertThat(details).isNotNull();
        verify(accountService, times(1)).getDepositAccountById(ACCOUNT_ID, TIME, true);
    }

    @Test(expected = DepositModuleException.class)
    public void getAccountDetailsByAccountId_wrong_id() {
        when(accountService.getDepositAccountById(WRONG_ID, TIME, true)).thenThrow(DepositModuleException.class);

        middlewareService.getDepositAccountById(WRONG_ID, TIME, false);
        verify(accountService, times(1)).getDepositAccountById(WRONG_ID, TIME, true);
    }

    @Test
    public void getAccountDetailsByAccountId_Success() {
        DepositAccountDetailsBO depositAccountDetailsBO = getDepositAccountDetailsBO();
        when(accountService.getDepositAccountById(ACCOUNT_ID, TIME, true)).thenReturn(depositAccountDetailsBO);
        when(detailsMapper.toAccountDetailsTO(depositAccountDetailsBO)).thenReturn(getAccountDetailsTO());

        AccountDetailsTO accountDetails = middlewareService.getDepositAccountById(ACCOUNT_ID, TIME, false);
        assertThat(accountDetails).isNotNull();
        assertThat(accountDetails.getBalances()).isNotNull();
        assertThat(accountDetails.getBalances().size()).isEqualTo(2);
    }

    @Test(expected = DepositModuleException.class)
    public void getAccountDetailsByAccountId_Failure_depositAccount_Not_Found() {
        when(accountService.getDepositAccountById(ACCOUNT_ID, TIME, true)).thenThrow(DepositModuleException.class);
        middlewareService.getDepositAccountById(ACCOUNT_ID, TIME, false);
    }

    @Test
    public void getAllAccountDetailsByUserLogin() {

        String userLogin = "spe";

        AccountDetailsTO account = new AccountDetailsTO();
        DepositAccountDetailsBO accountBO = getDepositAccountDetailsBO();

        List<AccountAccessBO> accessBOList = getDataFromFile("account-access-bo-list.yml", new TypeReference<List<AccountAccessBO>>() {
        });
        String iban = accessBOList.get(0).getIban();

        UserBO userBO = new UserBO();
        userBO.getAccountAccesses().addAll(accessBOList);
        when(userService.findByLogin(userLogin)).thenReturn(userBO);

        when(accountService.getDepositAccountsByIban(Collections.singletonList(iban), LocalDateTime.MIN, false)).thenReturn(Collections.singletonList(accountBO));
        when(detailsMapper.toAccountDetailsTO(accountBO)).thenReturn(account);

        List<AccountDetailsTO> details = middlewareService.getAllAccountDetailsByUserLogin(userLogin);

        assertThat(details.size(), is(1));
        assertThat(details.get(0), is(account));

        verify(userService, times(1)).findByLogin(userLogin);
        verify(accountService, times(1)).getDepositAccountsByIban(Collections.singletonList(iban), LocalDateTime.MIN, false);
        verify(detailsMapper, times(1)).toAccountDetailsTO(accountBO);
    }

    private static <T> T getAccount(Class<T> aClass) {
        try {
            return mapper.readValue(PaymentConverter.class.getResourceAsStream("AccountDetails.yml"), aClass);
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

    @Test
    public void listDepositAccounts() {
        // accounts
        List<DepositAccountDetailsBO> accounts = new ArrayList<>();
        accounts.add(getDepositAccountDetailsBO());

        when(accessService.loadCurrentUser(CORRECT_USER_ID)).thenReturn(buildUserBO());
        when(userMapper.toUserTO(buildUserBO())).thenReturn(buildUserTO());
        when(accountService.getDepositAccountsByIban(anyList(), any(LocalDateTime.class), anyBoolean())).thenReturn(accounts);
        when(detailsMapper.toAccountDetailsTO(any(DepositAccountDetailsBO.class))).thenReturn(getAccountDetailsTO());

        List<AccountDetailsTO> accountsToBeTested = middlewareService.listDepositAccounts(CORRECT_USER_ID);

        assertThat(accountsToBeTested).isNotNull();
        assertThat(accountsToBeTested).isNotEmpty();

        verify(accessService, times(1)).loadCurrentUser(CORRECT_USER_ID);
        verify(accountService, times(1)).getDepositAccountsByIban(anyList(), any(LocalDateTime.class), anyBoolean());
        verify(detailsMapper, times(1)).toAccountDetailsTO(any(DepositAccountDetailsBO.class)); // only one element in the list
    }

    @Test
    public void listDepositAccountsByBranch() {
        // user
        UserBO user = getDataFromFile("user.yml", new TypeReference<UserBO>() {
        });
        // accounts
        List<DepositAccountDetailsBO> accounts = new ArrayList<>();
        accounts.add(getDepositAccountDetailsBO());

        when(accessService.loadCurrentUser(CORRECT_USER_ID)).thenReturn(user);
        when(accountService.findByBranch(anyString())).thenReturn(accounts);
        when(detailsMapper.toAccountDetailsTO(any(DepositAccountDetailsBO.class))).thenReturn(getAccountDetailsTO());

        List<AccountDetailsTO> accountsToBeTested = middlewareService.listDepositAccountsByBranch(CORRECT_USER_ID);

        assertThat(accountsToBeTested).isNotNull();
        assertThat(accountsToBeTested).isNotEmpty();

        verify(accessService, times(1)).loadCurrentUser(CORRECT_USER_ID);
        verify(accountService, times(1)).findByBranch(anyString());
        verify(detailsMapper, times(1)).toAccountDetailsTO(any(DepositAccountDetailsBO.class)); // only one element in the list
    }

    @Test
    public void getTransactionById() {
        when(accountService.getTransactionById(anyString(), anyString())).thenReturn(readYml(TransactionDetailsBO.class, "TransactionBO.yml"));
        when(paymentConverter.toTransactionTO(any())).thenReturn(readYml(TransactionTO.class, "TransactionTO.yml"));

        TransactionTO result = middlewareService.getTransactionById("ACCOUNT_ID", "POSTING_ID");
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(TransactionTO.class, "TransactionTO.yml"));
    }

    @Test(expected = DepositModuleException.class)
    public void getTransactionById_Failure() {
        when(accountService.getTransactionById(anyString(), anyString())).thenThrow(DepositModuleException.class);

        middlewareService.getTransactionById("ACCOUNT_ID", "POSTING_ID");
    }

    @Test
    public void getAccountDetailsByIban() {
        DepositAccountDetailsBO accountBO = getDepositAccountDetailsBO();
        AccountDetailsTO accountDetailsTO = getAccount(AccountDetailsTO.class);

        when(accountService.getDepositAccountByIban(any(), any(), anyBoolean())).thenReturn(accountBO);
        when(detailsMapper.toAccountDetailsTO(accountBO)).thenReturn(accountDetailsTO);
        AccountDetailsTO details = middlewareService.getDepositAccountByIban(IBAN, TIME, false);

        assertThat(details).isNotNull();
        assertThat(details, is(accountDetailsTO));

        verify(accountService, times(1)).getDepositAccountByIban(IBAN, TIME, false);
        verify(detailsMapper, times(1)).toAccountDetailsTO(accountBO);
    }

    @Test(expected = DepositModuleException.class)
    public void getAccountDetailsByIbanDepositAccountNotFoundException() {
        when(accountService.getDepositAccountByIban(IBAN, TIME, false)).thenThrow(DepositModuleException.class);

        middlewareService.getDepositAccountByIban(IBAN, TIME, false);
        verify(accountService, times(1)).getDepositAccountByIban(IBAN, TIME, false);
    }

    @Test
    public void shouldReturnUserAccountAccess() {
        when(userService.findById(CORRECT_USER_ID)).thenReturn(buildUserBO());
        when(userMapper.toUserTO(buildUserBO())).thenReturn(buildUserTO());

        List<AccountAccessTO> accountAccesses = middlewareService.getAccountAccesses(CORRECT_USER_ID);

        assertThat(accountAccesses).isNotEmpty();
        assertThat(accountAccesses, hasSize(1));

        AccountAccessTO accountAccess = getFirstAccountAccess(accountAccesses);

        assertEquals(IBAN, accountAccess.getIban());
        assertEquals(AccessTypeTO.OWNER, accountAccess.getAccessType());
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
            return mapper.readValue(PaymentConverter.class.getResourceAsStream(fileName), aClass);
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

    @Test
    public void depositCashDelegatesToDepositAccountService() {
        doNothing().when(accountService).depositCash(eq(ACCOUNT_ID), any(), any());
        middlewareService.depositCash(buildScaInfoTO(), ACCOUNT_ID, new AmountTO());
        verify(accountService, times(1)).depositCash(eq(ACCOUNT_ID), any(), any());
    }

    @Test(expected = DepositModuleException.class)
    public void depositCashWrapsNotFoundException() {
        doThrow(DepositModuleException.class).when(accountService).depositCash(any(), any(), any());
        middlewareService.depositCash(buildScaInfoTO(), ACCOUNT_ID, new AmountTO());
    }

    private static UserBO buildUserBO() {
        UserBO user = new UserBO();
        user.setId(CORRECT_USER_ID);
        user.setAccountAccesses(buildAccountAccessesBO());
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
}
