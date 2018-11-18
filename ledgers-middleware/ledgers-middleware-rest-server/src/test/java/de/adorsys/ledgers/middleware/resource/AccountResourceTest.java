package de.adorsys.ledgers.middleware.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.adorsys.ledgers.middleware.exception.ExceptionAdvisor;
import de.adorsys.ledgers.middleware.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.service.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.service.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.UserNotFoundMiddlewareException;
import pro.javatar.commons.reader.JsonReader;
import pro.javatar.commons.reader.YamlReader;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountResourceTest {
    private static final String ACCOUNT_ID = "XXXYYYZZZ";
    private static final String TRANSACTION_ID = "TRANSACTION_ID";
    private static final String IBAN = "DE91100000000123456789";
    private static final LocalDate DATE_FROM = LocalDate.of(2018, 12, 12);
    private static final LocalDate DATE_TO = LocalDate.of(2018, 12, 18);
    private static final LocalDateTime DATE_TIME = LocalDateTime.of(2018, 12, 18, 0,0,0);

    private MockMvc mockMvc;

    @InjectMocks
    private AccountResource controller;

    @Mock
    private MiddlewareAccountManagementService middlewareService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders
                          .standaloneSetup(controller)
                          .setControllerAdvice(new ExceptionAdvisor())
                          .setMessageConverters(new MappingJackson2HttpMessageConverter())
                          .build();
    }

    @Test
    public void getAccountDetailsByAccountId() throws Exception {
        when(middlewareService.getAccountDetailsByAccountId(ACCOUNT_ID)).thenReturn(getDetails());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{accountId}", ACCOUNT_ID))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        AccountDetailsTO actual = strToObj(content, new TypeReference<AccountDetailsTO>() {
        });
        assertThat(mvcResult.getResponse().getStatus(), is(200));
        assertThat(Optional.ofNullable(actual).map(AccountDetailsTO::getId).orElse(null), is(ACCOUNT_ID));
        verify(middlewareService, times(1)).getAccountDetailsByAccountId(ACCOUNT_ID);
    }

    @Test
    public void getAccountDetailsByAccountId_Failure_NotFound() throws Exception {
        when(middlewareService.getAccountDetailsByAccountId(ACCOUNT_ID))
                .thenThrow(new AccountNotFoundMiddlewareException("Account with id=" + ACCOUNT_ID + " not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{accountId}", ACCOUNT_ID))
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        verify(middlewareService, times(1)).getAccountDetailsByAccountId(ACCOUNT_ID);
    }

    @Test
    public void getListOfAccountDetails() throws Exception {
        String userLogin = "userLogin";

        List<AccountDetailsTO> details = fileToObj("account-access-to.yml", new TypeReference<List<AccountDetailsTO>>() {
        });
        when(middlewareService.getAllAccountDetailsByUserLogin(userLogin)).thenReturn(details);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/accounts/users/{login}", userLogin))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        List<AccountDetailsTO> actual = strToObj(content, new TypeReference<List<AccountDetailsTO>>() {
        });
        assertThat(mvcResult.getResponse().getStatus(), is(200));

        assertThat(actual.size(), is(1));
        assertThat(actual, is(details));

        verify(middlewareService, times(1)).getAllAccountDetailsByUserLogin(userLogin);
    }

    @Test
    public void getListOfAccountDetailsNotFound() throws Exception {
        String userLogin = "userLogin";

        when(middlewareService.getAllAccountDetailsByUserLogin(userLogin)).thenThrow(new UserNotFoundMiddlewareException());

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/users/{login}", userLogin))
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        verify(middlewareService, times(1)).getAllAccountDetailsByUserLogin(userLogin);
    }

    @Test
    public void getBalances_Success() throws Exception {
        when(middlewareService.getBalances(ACCOUNT_ID)).thenReturn(readBalances(AccountBalanceTO.class));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/accounts/balances/{accountId}", ACCOUNT_ID))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        List<AccountBalanceTO> actual = JsonReader.getInstance().getListFromString(content, AccountBalanceTO.class);
        assertThat(mvcResult.getResponse().getStatus(), is(200));
        assertThat(actual.size(), is(2));

        actual.forEach(a -> assertThat(a).isNotNull());
        assertThat(actual.get(0)).isEqualToComparingFieldByFieldRecursively(readBalances(AccountBalanceTO.class).get(0));
        assertThat(actual.get(1)).isEqualToComparingFieldByFieldRecursively(readBalances(AccountBalanceTO.class).get(1));
        verify(middlewareService, times(1)).getBalances(ACCOUNT_ID);
    }

    @Test
    public void getBalances_Failure_NotFound() throws Exception {
        when(middlewareService.getBalances(ACCOUNT_ID))
                .thenThrow(new AccountNotFoundMiddlewareException("Account with id=" + ACCOUNT_ID + " not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/balances/{accountId}", ACCOUNT_ID))
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        verify(middlewareService, times(1)).getBalances(ACCOUNT_ID);
    }

    @Test
    public void getTransactionById() throws Exception {
        when(middlewareService.getTransactionById(ACCOUNT_ID, TRANSACTION_ID))
                .thenReturn(readYml(TransactionTO.class, "Transaction.yml"));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{accountId}/transactions/{transactionId}", ACCOUNT_ID, TRANSACTION_ID))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        TransactionTO actual = JsonReader.getInstance().getObjectFromString(content, TransactionTO.class);
        assertThat(mvcResult.getResponse().getStatus(), is(200));

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualToComparingFieldByFieldRecursively(readYml(TransactionTO.class, "Transaction.yml"));
        verify(middlewareService, times(1)).getTransactionById(ACCOUNT_ID, TRANSACTION_ID);
    }

    @Test
    public void getTransactionById_Failure_NotFound() throws Exception {
        when(middlewareService.getTransactionById(ACCOUNT_ID, TRANSACTION_ID))
                .thenThrow(new TransactionNotFoundMiddlewareException("Transaction with id=" + TRANSACTION_ID + "account " + ACCOUNT_ID + " not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{accountId}/transactions/{transactionId}", ACCOUNT_ID, TRANSACTION_ID))
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        verify(middlewareService, times(1)).getTransactionById(ACCOUNT_ID, TRANSACTION_ID);
    }

    @Test
    public void getTransactionByDates() throws Exception {
        when(middlewareService.getTransactionsByDates(any(), any(), any()))
                .thenReturn(Collections.singletonList(readYml(TransactionTO.class, "Transaction.yml")));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{accountId}/transactions", ACCOUNT_ID)
                                                      .param("dateFrom", DATE_FROM.toString())
                                                      .param("dateTo", DATE_TO.toString()))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        List<TransactionTO> actual = JsonReader.getInstance().getListFromString(content, TransactionTO.class);
        assertThat(mvcResult.getResponse().getStatus(), is(200));

        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.get(0)).isEqualToComparingFieldByFieldRecursively(readYml(TransactionTO.class, "Transaction.yml"));
        verify(middlewareService, times(1)).getTransactionsByDates(ACCOUNT_ID, DATE_FROM, DATE_TO);
    }

    @Test
    public void getTransactionByDates_Failure_WrongDateInput() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{accountId}/transactions", ACCOUNT_ID)
                                .param("dateFrom", DATE_TO.toString())
                                .param("dateTo", DATE_FROM.toString()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.CONFLICT.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        verify(middlewareService, times(0)).getTransactionsByDates(ACCOUNT_ID, DATE_FROM, DATE_TO);
    }

    @Test
    public void getAccountDetailsByIban() throws Exception {
        AccountDetailsTO details = getDetails();
        when(middlewareService.getAccountDetailsByIban(IBAN)).thenReturn(details);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/accounts/ibans/{iban}", IBAN))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        AccountDetailsTO actual = JsonReader.getInstance().getObjectFromString(content, AccountDetailsTO.class);

        assertThat(actual).isEqualToComparingFieldByFieldRecursively(details);
        verify(middlewareService, times(1)).getAccountDetailsByIban(IBAN);

    }

    @Test
    public void getAccountDetailsByIbanAccountNotFoundMiddlewareException() throws Exception {
        when(middlewareService.getAccountDetailsByIban(IBAN))
                .thenThrow(new AccountNotFoundMiddlewareException("Account with iban=" + IBAN + " not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/ibans/{iban}", IBAN))
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        verify(middlewareService, times(1)).getAccountDetailsByIban(IBAN);
    }


    private AccountDetailsTO getDetails() {
        AccountDetailsTO file = readYml(AccountDetailsTO.class, "AccountDetails.yml");
        Optional.ofNullable(file)
                .ifPresent(d -> d.setBalances(Collections.emptyList()));
        return file;
    }


    private static <T> List<T> readBalances(Class<T> tClass) {
        return Arrays.asList(
                readYml(tClass, "Balance1.yml"),
                readYml(tClass, "Balance2.yml")
        );
    }

    private <T> T strToObj(String source, TypeReference<T> ref) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return objectMapper.readValue(source, ref);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //    todo: replace by javatar-commons version 0.7

    private <T> T fileToObj(String source, TypeReference<T> ref) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        InputStream inputStream = getClass().getResourceAsStream(source);
        try {
            return objectMapper.readValue(inputStream, ref);
        } catch (IOException e) {
            throw new IllegalStateException("File not found", e);
        }
    }

    private static <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(AccountResource.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}