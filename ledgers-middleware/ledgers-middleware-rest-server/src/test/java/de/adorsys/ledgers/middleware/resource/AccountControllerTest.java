package de.adorsys.ledgers.middleware.resource;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.middleware.exception.ExceptionAdvisor;
import de.adorsys.ledgers.middleware.service.MiddlewareService;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.exception.AccountNotFoundMiddlewareException;
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
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Collections;

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
    public void getPaymentStatusById() throws Exception {
        when(middlewareService.getAccountDetailsByAccountId(ACCOUNT_ID)).thenReturn(getDetails());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/accounts/" + ACCOUNT_ID))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        AccountDetailsTO actual = strToObj(content, AccountDetailsTO.class);
        assertThat(mvcResult.getResponse().getStatus(), is(200));
        assertThat(actual.getId(), is(ACCOUNT_ID));
        verify(middlewareService, times(1)).getAccountDetailsByAccountId(ACCOUNT_ID);
    }

    @Test
    public void getPaymentStatusById_Failure_NotFound() throws Exception {
        when(middlewareService.getAccountDetailsByAccountId(ACCOUNT_ID))
                .thenThrow(new AccountNotFoundMiddlewareException("Account with id=" + ACCOUNT_ID + " not found"));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/accounts/" + ACCOUNT_ID))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        verify(middlewareService, times(1)).getAccountDetailsByAccountId(ACCOUNT_ID);
    }


    private AccountDetailsTO getDetails() {
        AccountDetailsTO file = YamlReader.getInstance().getObjectFromFile("de/adorsys/ledgers/middleware/resource/AccountDetails.yml", AccountDetailsTO.class);
        file.setBalances(Collections.emptyList());
        return file;
    }

    private <T> T strToObj(String source, Class<T> tClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return objectMapper.readValue(source, tClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}