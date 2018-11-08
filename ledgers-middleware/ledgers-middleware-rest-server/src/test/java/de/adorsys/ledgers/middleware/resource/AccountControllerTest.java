package de.adorsys.ledgers.middleware.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.middleware.exception.ExceptionAdvisor;
import de.adorsys.ledgers.middleware.service.MiddlewareService;
import de.adorsys.ledgers.middleware.service.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.UserNotFoundMiddlewareException;
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
import pro.javatar.commons.reader.JsonReader;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Arrays;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {
    private static final String ACCOUNT_ID = "XXXYYYZZZ";

    private MockMvc mockMvc;

    @InjectMocks
    private AccountController controller;

    @Mock
    private MiddlewareService middlewareService;

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
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/accounts//balances/{accountId}", ACCOUNT_ID))
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

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts//balances/{accountId}", ACCOUNT_ID))
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        verify(middlewareService, times(1)).getBalances(ACCOUNT_ID);
    }

    private AccountDetailsTO getDetails() throws IOException {
        AccountDetailsTO file = YamlReader.getInstance().getObjectFromResource(AccountController.class, "AccountDetails.yml", AccountDetailsTO.class);
        file.setBalances(Collections.emptyList());
        return file;
    }

    private static <T> List<T> readBalances(Class<T> tClass) throws IOException {
        return Arrays.asList(
                YamlReader.getInstance().getObjectFromResource(AccountController.class, "Balance1.yml", tClass),
                YamlReader.getInstance().getObjectFromResource(AccountController.class, "Balance2.yml", tClass)
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
            throw new IllegalStateException("File not found",e);
        }
    }
}