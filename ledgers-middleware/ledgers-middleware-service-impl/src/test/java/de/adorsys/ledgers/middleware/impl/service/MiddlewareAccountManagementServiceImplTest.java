package de.adorsys.ledgers.middleware.impl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.impl.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;

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
    private UserMapper userMapper;
    
    static ObjectMapper mapper = getObjectMapper();
    
    @Test
    public void getAccountDetailsByAccountId() throws DepositAccountNotFoundException, AccountNotFoundMiddlewareException, IOException, LedgerAccountNotFoundException {
        when(accountService.getDepositAccountById(ACCOUNT_ID, TIME, true)).thenReturn(getDepositAccountDetailsBO());

        when(detailsMapper.toAccountDetailsTO(any())).thenReturn(getAccount(AccountDetailsTO.class));
        AccountDetailsTO details = middlewareService.getAccountDetailsByAccountId(ACCOUNT_ID, TIME);

        assertThat(details).isNotNull();
        verify(accountService, times(1)).getDepositAccountById(ACCOUNT_ID, TIME, true);
    }

	@Test(expected = AccountNotFoundMiddlewareException.class)
    public void getAccountDetailsByAccountId_wrong_id() throws AccountNotFoundMiddlewareException, DepositAccountNotFoundException {
    	when(accountService.getDepositAccountById(WRONG_ID, TIME, true)).thenThrow(new DepositAccountNotFoundException());

        middlewareService.getAccountDetailsByAccountId(WRONG_ID, TIME);
        verify(accountService, times(1)).getDepositAccountById(WRONG_ID, TIME, true);
    }

    @Test
    public void getAccountDetailsByAccountId_Success() throws AccountNotFoundMiddlewareException, LedgerAccountNotFoundException, IOException, DepositAccountNotFoundException {
    	DepositAccountDetailsBO depositAccountDetailsBO = getDepositAccountDetailsBO();
        when(accountService.getDepositAccountById(ACCOUNT_ID, TIME, true)).thenReturn(depositAccountDetailsBO);
        when(detailsMapper.toAccountDetailsTO(depositAccountDetailsBO)).thenReturn(getAccountDetailsTO());
        
        AccountDetailsTO accountDetails = middlewareService.getAccountDetailsByAccountId(ACCOUNT_ID, TIME);
        assertThat(accountDetails).isNotNull();
        assertThat(accountDetails.getBalances()).isNotNull();
        assertThat(accountDetails.getBalances().size()).isEqualTo(2);
    }

    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getAccountDetailsByAccountId_Failure_depositAccount_Not_Found() throws AccountNotFoundMiddlewareException, DepositAccountNotFoundException {
        when(accountService.getDepositAccountById(ACCOUNT_ID, TIME, true)).thenThrow(new DepositAccountNotFoundException());
        middlewareService.getAccountDetailsByAccountId(ACCOUNT_ID, TIME);
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
        
        when(accountService.getDepositAccountsByIBAN(Collections.singletonList(iban), LocalDateTime.MIN, false)).thenReturn(Collections.singletonList(accountBO));
        when(detailsMapper.toAccountDetailsTO(accountBO)).thenReturn(account);

        List<AccountDetailsTO> details = middlewareService.getAllAccountDetailsByUserLogin(userLogin);

        assertThat(details.size(), is(1));
        assertThat(details.get(0), is(account));

        verify(userService, times(1)).findByLogin(userLogin);
        verify(accountService, times(1)).getDepositAccountsByIBAN(Collections.singletonList(iban), LocalDateTime.MIN, false);
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
    public void getTransactionById() throws TransactionNotFoundMiddlewareException, AccountNotFoundMiddlewareException, DepositAccountNotFoundException, TransactionNotFoundException {
        when(accountService.getTransactionById(anyString(), anyString())).thenReturn(readYml(TransactionDetailsBO.class, "TransactionBO.yml"));
        when(paymentConverter.toTransactionTO(any())).thenReturn(readYml(TransactionTO.class, "TransactionTO.yml"));

        TransactionTO result = middlewareService.getTransactionById("ACCOUNT_ID", "POSTING_ID");
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(TransactionTO.class, "TransactionTO.yml"));
    }

    @Test(expected = TransactionNotFoundMiddlewareException.class)
    public void getTransactionById_Failure() throws TransactionNotFoundMiddlewareException, AccountNotFoundMiddlewareException, DepositAccountNotFoundException, TransactionNotFoundException {
        when(accountService.getTransactionById(anyString(), anyString())).thenThrow(new TransactionNotFoundException("ACCOUNT_ID", "POSTING_ID"));

        middlewareService.getTransactionById("ACCOUNT_ID", "POSTING_ID");
    }

    @Test
    public void getAccountDetailsByIban() throws LedgerAccountNotFoundException, DepositAccountNotFoundException, IOException, AccountNotFoundMiddlewareException {
        DepositAccountDetailsBO accountBO = getDepositAccountDetailsBO();
        AccountDetailsTO accountDetailsTO = getAccount(AccountDetailsTO.class);

        when(accountService.getDepositAccountByIBAN(IBAN, TIME, true)).thenReturn(accountBO);
        when(detailsMapper.toAccountDetailsTO(accountBO)).thenReturn(accountDetailsTO);
        AccountDetailsTO details = middlewareService.getAccountDetailsWithBalancesByIban(IBAN, TIME);

        assertThat(details).isNotNull();
        assertThat(details, is(accountDetailsTO));

        verify(accountService, times(1)).getDepositAccountByIBAN(IBAN, TIME, true);
        verify(detailsMapper, times(1)).toAccountDetailsTO(accountBO);
    }

    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getAccountDetailsByIbanDepositAccountNotFoundException() throws DepositAccountNotFoundException, AccountNotFoundMiddlewareException {

        when(accountService.getDepositAccountByIBAN(IBAN, TIME, true)).thenThrow(new DepositAccountNotFoundException());

        middlewareService.getAccountDetailsWithBalancesByIban(IBAN, TIME);
        verify(accountService, times(1)).getDepositAccountByIBAN(IBAN, TIME, true);
    }
    
    @Test
    public void getTransactionsByDates() throws TransactionNotFoundMiddlewareException, AccountNotFoundMiddlewareException, DepositAccountNotFoundException, TransactionNotFoundException {
        when(accountService.getTransactionsByDates(any(), any(), any())).thenReturn(Collections.singletonList(new TransactionDetailsBO()));
        when(paymentConverter.toTransactionTOList(any())).thenReturn(Collections.singletonList(new TransactionTO()));
        List<TransactionTO> result = middlewareService.getTransactionsByDates(ACCOUNT_ID, LocalDate.of(2018, 12, 12), LocalDate.of(2018, 12, 18));
        assertThat(result.isEmpty()).isFalse();
    }

    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getTransactionsByDates_Failure() throws AccountNotFoundMiddlewareException, DepositAccountNotFoundException {
        when(accountService.getTransactionsByDates(any(), any(), any())).thenThrow(new DepositAccountNotFoundException());
        middlewareService.getTransactionsByDates(ACCOUNT_ID, LocalDate.of(2018, 12, 12), LocalDate.of(2018, 12, 18));
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
    
    protected static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
    
}