package de.adorsys.ledgers.middleware.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.middleware.exception.ExceptionAdvisor;
import de.adorsys.ledgers.middleware.service.MiddlewareService;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ScaMethodResourceTest {
    private static final String USER_LOGIN = "userLogin";

    private MockMvc mockMvc;

    @InjectMocks
    private ScaMethodResource resource;

    @Mock
    private MiddlewareService middlewareService;

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
    public void getUserScaMethods() throws Exception {

        List<SCAMethodTO> methods = fileToObj("sca-methods.yml", new TypeReference<List<SCAMethodTO>>() {
        });

        when(middlewareService.getSCAMethods(USER_LOGIN)).thenReturn(methods);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(ScaMethodResource.SCA_METHODS+"/{login}", USER_LOGIN))
                .andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        List<SCAMethodTO> actual = strToObj(content, new TypeReference<List<SCAMethodTO>>() {
        });
        assertThat(mvcResult.getResponse().getStatus(), is(200));
        assertThat(actual, is(methods));

        verify(middlewareService, times(1)).getSCAMethods(USER_LOGIN);
    }

    @Test
    public void getUserScaMethodsUserNotFound() throws Exception {

        when(middlewareService.getSCAMethods(USER_LOGIN)).thenThrow(UserNotFoundMiddlewareException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(ScaMethodResource.SCA_METHODS+"/{login}", USER_LOGIN))
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andReturn();

        verify(middlewareService, times(1)).getSCAMethods(USER_LOGIN);
    }

    @Test
    public void updateUserScaMethods() throws Exception {
        List<SCAMethodTO> methods = fileToObj("sca-methods.yml", new TypeReference<List<SCAMethodTO>>() {
        });

        doNothing().when(middlewareService).updateScaMethods(methods, USER_LOGIN);

        mockMvc.perform(MockMvcRequestBuilders.put(ScaMethodResource.SCA_METHODS+"/{login}", USER_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(objToStr(methods)))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.ACCEPTED.value()))
                                      .andReturn();

        verify(middlewareService, times(1)).updateScaMethods(methods, USER_LOGIN);
    }

    @Test
    public void updateUserScaMethodsUserNotFound() throws Exception {
        List<SCAMethodTO> methods = fileToObj("sca-methods.yml", new TypeReference<List<SCAMethodTO>>() {
        });

        doThrow(UserNotFoundMiddlewareException.class).when(middlewareService).updateScaMethods(methods, USER_LOGIN);

        mockMvc.perform(MockMvcRequestBuilders.put(ScaMethodResource.SCA_METHODS+"/{login}", USER_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(objToStr(methods)))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                                      .andReturn();

        verify(middlewareService, times(1)).updateScaMethods(methods, USER_LOGIN);
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
    private String objToStr(Object source) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return objectMapper.writeValueAsString(source);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}