package de.adorsys.ledgers.middleware.resource;

import de.adorsys.ledgers.middleware.domain.SCAGenerationRequest;
import de.adorsys.ledgers.middleware.domain.SCAGenerationResponse;
import de.adorsys.ledgers.middleware.domain.SCAValidationRequest;
import de.adorsys.ledgers.middleware.domain.SCAValidationResponse;
import de.adorsys.ledgers.middleware.exception.*;
import de.adorsys.ledgers.middleware.service.MiddlewareService;
import de.adorsys.ledgers.middleware.service.domain.sca.SCAMethodTO;
import de.adorsys.ledgers.middleware.service.domain.sca.SCAMethodTypeTO;
import de.adorsys.ledgers.middleware.service.exception.*;
import de.adorsys.ledgers.util.SerializationUtils;
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
import pro.javatar.commons.reader.ResourceReader;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthCodeResourceTest {

    private static final String OP_ID = "opId";
    private static final String OP_DATA = "opData";
    private static final String TAN_CODE = "my tan code";
    private static final int VALIDITY_SECONDS = 60;
    private static final ResourceReader READER = JsonReader.getInstance();
    private static final String USER_LOGIN = "userLogin";
    private static final String USER_MESSAGE = "userMessage";

    private MockMvc mockMvc;

    @InjectMocks
    private AuthCodeResource resource;

    @Mock
    private MiddlewareService middlewareService;
    private SCAMethodTO scaMethod;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders
                          .standaloneSetup(resource)
                          .setControllerAdvice(new ExceptionAdvisor())
                          .setMessageConverters(new MappingJackson2HttpMessageConverter())
                          .build();

        scaMethod = new SCAMethodTO();
        scaMethod.setType(SCAMethodTypeTO.EMAIL);
        scaMethod.setValue("spe@adorsys.com.ua");
    }

    @Test
    public void generate() throws Exception {

        SCAGenerationRequest request = new SCAGenerationRequest(USER_LOGIN, scaMethod, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS);


        when(middlewareService.generateAuthCode(USER_LOGIN, scaMethod, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS)).thenReturn(OP_ID);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .post("/auth-codes/generate")
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                                      .content(SerializationUtils.writeValueAsBytes(request)))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        SCAGenerationResponse actual = strToObj(content, SCAGenerationResponse.class);

        assertThat(actual, is(new SCAGenerationResponse(OP_ID)));

        verify(middlewareService, times(1)).generateAuthCode(USER_LOGIN, scaMethod, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS);
    }

    @Test
    public void generateWithValidationError() throws Exception {

        SCAGenerationRequest request = new SCAGenerationRequest(USER_LOGIN, scaMethod, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS);

        String message = "Validation error";
        when(middlewareService.generateAuthCode(USER_LOGIN, scaMethod, OP_DATA,USER_MESSAGE, VALIDITY_SECONDS)).thenThrow(new AuthCodeGenerationMiddlewareException(message));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .post("/auth-codes/generate")
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                                      .content(SerializationUtils.writeValueAsBytes(request)))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        RestException exception = READER.getObjectFromString(mvcResult.getResponse().getContentAsString(), ValidationRestException.class);

        assertThat(exception.getStatus(), is(HttpStatus.UNPROCESSABLE_ENTITY));
        assertThat(exception.getCode(), is(ValidationRestException.ERROR_CODE));
        assertThat(exception.getDevMessage(), is(message));
        assertThat(exception.getMessage(), is(message));

        verify(middlewareService, times(1)).generateAuthCode(USER_LOGIN, scaMethod, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS);
    }

    @Test
    public void validate() throws Exception {
        SCAValidationRequest operation = new SCAValidationRequest(OP_DATA, TAN_CODE);

        when(middlewareService.validateAuthCode(OP_ID, OP_DATA, TAN_CODE)).thenReturn(Boolean.FALSE);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .post("/auth-codes/{id}/validate", OP_ID)
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                                      .content(SerializationUtils.writeValueAsBytes(operation)))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        SCAValidationResponse authCode = READER.getObjectFromString(mvcResult.getResponse().getContentAsString(), SCAValidationResponse.class);

        assertThat(authCode.getValid(), is(Boolean.FALSE));

        verify(middlewareService, times(1)).validateAuthCode(OP_ID, OP_DATA, TAN_CODE);
    }

    @Test
    public void validateNotFound() throws Exception {
        SCAValidationRequest operation = new SCAValidationRequest(OP_DATA, TAN_CODE);

        String message = "Operation not found";
        when(middlewareService.validateAuthCode(OP_ID, OP_DATA, TAN_CODE)).thenThrow(new SCAOperationNotFoundMiddlewareException(message));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .post("/auth-codes/{id}/validate", OP_ID)
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                                      .content(SerializationUtils.writeValueAsBytes(operation)))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        RestException exception = READER.getObjectFromString(mvcResult.getResponse().getContentAsString(), NotFoundRestException.class);

        assertThat(exception.getStatus(), is(HttpStatus.NOT_FOUND));
        assertThat(exception.getCode(), is(NotFoundRestException.ERROR_CODE));
        assertThat(exception.getDevMessage(), is(message));
        assertThat(exception.getMessage(), is(message));

        verify(middlewareService, times(1)).validateAuthCode(OP_ID, OP_DATA, TAN_CODE);
    }

    @Test
    public void validateValidationError() throws Exception {
        SCAValidationRequest operation = new SCAValidationRequest(OP_DATA, TAN_CODE);

        String message = "Operation is not valid";
        when(middlewareService.validateAuthCode(OP_ID, OP_DATA, TAN_CODE)).thenThrow(new SCAOperationValidationMiddlewareException(message));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .post("/auth-codes/{id}/validate", OP_ID)
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                                      .content(SerializationUtils.writeValueAsBytes(operation)))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        RestException exception = READER.getObjectFromString(mvcResult.getResponse().getContentAsString(), ValidationRestException.class);

        assertThat(exception.getStatus(), is(HttpStatus.UNPROCESSABLE_ENTITY));
        assertThat(exception.getCode(), is(ValidationRestException.ERROR_CODE));
        assertThat(exception.getDevMessage(), is(message));
        assertThat(exception.getMessage(), is(message));

        verify(middlewareService, times(1)).validateAuthCode(OP_ID, OP_DATA, TAN_CODE);
    }

    @Test
    public void validateStolenException() throws Exception {
        SCAValidationRequest operation = new SCAValidationRequest(OP_DATA, TAN_CODE);

        String message = "Operation auth code was stolen";
        when(middlewareService.validateAuthCode(OP_ID, OP_DATA, TAN_CODE)).thenThrow(new SCAOperationUsedOrStolenMiddlewareException(message));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .post("/auth-codes/{id}/validate", OP_ID)
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                                      .content(SerializationUtils.writeValueAsBytes(operation)))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.CONFLICT.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        RestException exception = READER.getObjectFromString(mvcResult.getResponse().getContentAsString(), ConflictRestException.class);

        assertThat(exception.getStatus(), is(HttpStatus.CONFLICT));
        assertThat(exception.getCode(), is(ConflictRestException.ERROR_CODE));
        assertThat(exception.getDevMessage(), is(message));
        assertThat(exception.getMessage(), is(message));

        verify(middlewareService, times(1)).validateAuthCode(OP_ID, OP_DATA, TAN_CODE);
    }

    @Test
    public void validateExpiredException() throws Exception {
        SCAValidationRequest operation = new SCAValidationRequest(OP_DATA, TAN_CODE);

        String message = "Operation auth code was expired";
        when(middlewareService.validateAuthCode(OP_ID, OP_DATA, TAN_CODE)).thenThrow(new SCAOperationExpiredMiddlewareException(message));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .post("/auth-codes/{id}/validate", OP_ID)
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                                      .content(SerializationUtils.writeValueAsBytes(operation)))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.CONFLICT.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        RestException exception = READER.getObjectFromString(mvcResult.getResponse().getContentAsString(), ConflictRestException.class);

        assertThat(exception.getStatus(), is(HttpStatus.CONFLICT));
        assertThat(exception.getCode(), is(ConflictRestException.ERROR_CODE));
        assertThat(exception.getDevMessage(), is(message));
        assertThat(exception.getMessage(), is(message));

        verify(middlewareService, times(1)).validateAuthCode(OP_ID, OP_DATA, TAN_CODE);
    }

    private <T> T strToObj(String source, Class<T> tClass) {
        try {
            return JsonReader.getInstance().getObjectFromString(source, tClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Can't build object from the string", e);
        }
    }
}