package de.adorsys.ledgers.middleware.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.ledgers.middleware.exception.ExceptionAdvisor;
import de.adorsys.ledgers.middleware.service.MiddlewareUserService;
import de.adorsys.ledgers.middleware.service.domain.sca.SCAMethodTO;
import de.adorsys.ledgers.middleware.service.exception.UserNotFoundMiddlewareException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserManagementResourceTest {
    private static final String LOGIN = "login";
    private static final String PIN = "pin";

    private MockMvc mockMvc;

    @InjectMocks
    private UserManagementResource resource;

    @Mock
    private MiddlewareUserService userService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders
                          .standaloneSetup(resource)
                          .setControllerAdvice(new ExceptionAdvisor())
                          .setMessageConverters(new MappingJackson2HttpMessageConverter())
                          .build();
    }

    @Test
    public void authorise() throws Exception {
        when(userService.authorise(LOGIN, PIN)).thenReturn(Boolean.FALSE);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .post(UserManagementResource.USERS + "/authorise")
                                                      .param("login", LOGIN)
                                                      .param("pin", PIN)
        )
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        assertThat(mvcResult.getResponse().getStatus(), is(200));
        assertThat(content, is(Boolean.FALSE.toString()));

        verify(userService, times(1)).authorise(LOGIN, PIN);
    }

    @Test
    public void authoriseUserNotFound() throws Exception {
        when(userService.authorise(LOGIN, PIN)).thenThrow(new UserNotFoundMiddlewareException("User with login="+LOGIN+" not found"));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .post(UserManagementResource.USERS + "/authorise")
                                                      .param("login", LOGIN)
                                                      .param("pin", PIN)
        )
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        verify(userService, times(1)).authorise(LOGIN, PIN);
    }
}