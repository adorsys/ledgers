package de.adorsys.ledgers.middleware.impl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.TransactionNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.impl.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.impl.converter.AmountMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
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
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareAccountManagementServiceImplTest {
    private static final String WRONG_ID = "wrong id";
	private static final String ACCOUNT_ID = "id";
    private static final String IBAN = "DE91100000000123456789";

    private static final LocalDateTime TIME = LocalDateTime.MIN;
    
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
    @Mock
    private AccessTokenTO accessToken;
    
    static ObjectMapper mapper = getObjectMapper();
    
    @Test
    public void getAccountDetailsByAccountId() throws DepositAccountNotFoundException, AccountNotFoundMiddlewareException, IOException, LedgerAccountNotFoundException {
        when(accountService.getDepositAccountById(ACCOUNT_ID, TIME, true)).thenReturn(getDepositAccountDetailsBO());

        when(detailsMapper.toAccountDetailsTO(any())).thenReturn(getAccount(AccountDetailsTO.class));
        AccountDetailsTO details = middlewareService.getDepositAccountById(ACCOUNT_ID, TIME, false);

        assertThat(details).isNotNull();
        verify(accountService, times(1)).getDepositAccountById(ACCOUNT_ID, TIME, true);
    }

	@Test(expected = AccountNotFoundMiddlewareException.class)
    public void getAccountDetailsByAccountId_wrong_id() throws AccountNotFoundMiddlewareException, DepositAccountNotFoundException {
    	when(accountService.getDepositAccountById(WRONG_ID, TIME, true)).thenThrow(new DepositAccountNotFoundException());

        middlewareService.getDepositAccountById(WRONG_ID, TIME,false);
        verify(accountService, times(1)).getDepositAccountById(WRONG_ID, TIME, true);
    }

    @Test
    public void getAccountDetailsByAccountId_Success() throws DepositAccountNotFoundException, AccountNotFoundMiddlewareException {
    	DepositAccountDetailsBO depositAccountDetailsBO = getDepositAccountDetailsBO();
        when(accountService.getDepositAccountById(ACCOUNT_ID, TIME, true)).thenReturn(depositAccountDetailsBO);
        when(detailsMapper.toAccountDetailsTO(depositAccountDetailsBO)).thenReturn(getAccountDetailsTO());
        
        AccountDetailsTO accountDetails = middlewareService.getDepositAccountById(ACCOUNT_ID, TIME,false);
        assertThat(accountDetails).isNotNull();
        assertThat(accountDetails.getBalances()).isNotNull();
        assertThat(accountDetails.getBalances().size()).isEqualTo(2);
    }

    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getAccountDetailsByAccountId_Failure_depositAccount_Not_Found() throws DepositAccountNotFoundException, AccountNotFoundMiddlewareException {
        when(accountService.getDepositAccountById(ACCOUNT_ID, TIME, true)).thenThrow(new DepositAccountNotFoundException());
        middlewareService.getDepositAccountById(ACCOUNT_ID, TIME,false);
    }

    @Test
    public void getAllAccountDetailsByUserLogin() throws UserNotFoundMiddlewareException, UserNotFoundException, DepositAccountNotFoundException, AccountNotFoundMiddlewareException {

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
    public void listDepositAccounts () throws DepositAccountNotFoundException {
        // users
        UserBO user = getDataFromFile("user.yml", new TypeReference<UserBO>() {});
        UserTO userTO = getDataFromFile("user.yml", new TypeReference<UserTO>() {});
        // accounts
        List<DepositAccountDetailsBO> accounts = new ArrayList<>();
        accounts.add(getDepositAccountDetailsBO());
        List<AccountDetailsTO> accountsTO = new ArrayList<>();
        accountsTO.add(getAccountDetailsTO());

        when(accessService.loadCurrentUser()).thenReturn(user);
        when(accountService.getDepositAccountsByIban(anyList(), any(LocalDateTime.class), anyBoolean())).thenReturn(accounts);
        when(detailsMapper.toAccountDetailsTO(any(DepositAccountDetailsBO.class))).thenReturn(getAccountDetailsTO());

        List<AccountDetailsTO> accountsToBeTested = middlewareService.listDepositAccounts();

        assertThat(accountsToBeTested).isNotNull();
        assertThat(accountsToBeTested).isNotEmpty();

        verify(accessService, times(1)).loadCurrentUser();
        verify(accountService, times(1)).getDepositAccountsByIban(anyList(),any(LocalDateTime.class),anyBoolean());
        verify(detailsMapper, times(1)).toAccountDetailsTO(any(DepositAccountDetailsBO.class)); // only one element in the list
    }

    @Test
    public void listDepositAccountsByBranch() {
        // user
        UserBO user = getDataFromFile("user.yml", new TypeReference<UserBO>() {});
        // accounts
        List<DepositAccountDetailsBO> accounts = new ArrayList<>();
        accounts.add(getDepositAccountDetailsBO());
        List<AccountDetailsTO> accountsTO = new ArrayList<>();
        accountsTO.add(getAccountDetailsTO());

        when(accessService.loadCurrentUser()).thenReturn(user);
        when(accountService.findByBranch(anyString())).thenReturn(accounts);
        when(detailsMapper.toAccountDetailsTO(any(DepositAccountDetailsBO.class))).thenReturn(getAccountDetailsTO());

        List<AccountDetailsTO> accountsToBeTested = middlewareService.listDepositAccountsByBranch();

        assertThat(accountsToBeTested).isNotNull();
        assertThat(accountsToBeTested).isNotEmpty();

        verify(accessService, times(1)).loadCurrentUser();
        verify(accountService, times(1)).findByBranch(anyString());
        verify(detailsMapper, times(1)).toAccountDetailsTO(any(DepositAccountDetailsBO.class)); // only one element in the list
    }

	@Test
    public void getTransactionById() throws TransactionNotFoundMiddlewareException, AccountNotFoundMiddlewareException, DepositAccountNotFoundException, TransactionNotFoundException {
        when(accountService.getTransactionById(anyString(), anyString())).thenReturn(readYml(TransactionDetailsBO.class, "TransactionBO.yml"));
        when(paymentConverter.toTransactionTO(any())).thenReturn(readYml(TransactionTO.class, "TransactionTO.yml"));

        TransactionTO result = middlewareService.getTransactionById("ACCOUNT_ID", "POSTING_ID");
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(TransactionTO.class, "TransactionTO.yml"));
    }

    @Test(expected = TransactionNotFoundMiddlewareException.class)
    public void getTransactionById_Failure() throws TransactionNotFoundMiddlewareException, TransactionNotFoundException {
        when(accountService.getTransactionById(anyString(), anyString())).thenThrow(new TransactionNotFoundException("ACCOUNT_ID", "POSTING_ID"));

        middlewareService.getTransactionById("ACCOUNT_ID", "POSTING_ID");
    }

    @Test
    public void getAccountDetailsByIban() throws DepositAccountNotFoundException, AccountNotFoundMiddlewareException {
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

    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getAccountDetailsByIbanDepositAccountNotFoundException() throws DepositAccountNotFoundException, AccountNotFoundMiddlewareException {
        when(accountService.getDepositAccountByIban(IBAN, TIME, false)).thenThrow(new DepositAccountNotFoundException());

        middlewareService.getDepositAccountByIban(IBAN, TIME, false);
        verify(accountService, times(1)).getDepositAccountByIban(IBAN, TIME, false);
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
    public void depositCashDelegatesToDepositAccountService() throws Exception {
        doNothing().when(accountService).depositCash(eq(ACCOUNT_ID), any(), any());
        middlewareService.depositCash(ACCOUNT_ID, new AmountTO());
        verify(accountService, times(1)).depositCash(eq(ACCOUNT_ID), any(), any());
    }

    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void depositCashWrapsNotFoundException() throws Exception {
        doThrow(DepositAccountNotFoundException.class).when(accountService).depositCash(any(), any(), any());
        middlewareService.depositCash(ACCOUNT_ID, new AmountTO());
    }
}
