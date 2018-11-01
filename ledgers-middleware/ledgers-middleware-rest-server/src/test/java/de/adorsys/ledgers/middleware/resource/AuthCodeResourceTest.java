package de.adorsys.ledgers.middleware.resource;

import de.adorsys.ledgers.middleware.domain.SCAOperationTO;
import de.adorsys.ledgers.middleware.domain.ValidationResultTO;
import de.adorsys.ledgers.middleware.exception.*;
import de.adorsys.ledgers.middleware.service.MiddlewareService;
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    private MockMvc mockMvc;

    @InjectMocks
    private AuthCodeResource resource;

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
    public void generate() throws Exception {

        SCAOperationTO operation = new SCAOperationTO(OP_DATA, VALIDITY_SECONDS);

        when(middlewareService.generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS)).thenReturn(TAN_CODE);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .post("/auth-codes/{id}/generate", OP_ID)
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                                      .content(SerializationUtils.writeValueAsBytes(operation)))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.NO_CONTENT.value()))
                                      .andReturn();

        verify(middlewareService, times(1)).generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS);
    }

    @Test
    public void generateWithValidationError() throws Exception {

        SCAOperationTO operation = new SCAOperationTO(OP_DATA, VALIDITY_SECONDS);

        String message = "Validation error";
        when(middlewareService.generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS)).thenThrow(new AuthCodeGenerationMiddlewareException(message));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .post("/auth-codes/{id}/generate", OP_ID)
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

        verify(middlewareService, times(1)).generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS);
    }

    @Test
    public void validate() throws Exception {
        SCAOperationTO operation = new SCAOperationTO(OP_DATA, VALIDITY_SECONDS, TAN_CODE);

        when(middlewareService.validateAuthCode(OP_ID, OP_DATA, TAN_CODE)).thenReturn(Boolean.FALSE);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                                      .post("/auth-codes/{id}/validate", OP_ID)
                                                      .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                                      .content(SerializationUtils.writeValueAsBytes(operation)))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        ValidationResultTO authCode = READER.getObjectFromString(mvcResult.getResponse().getContentAsString(), ValidationResultTO.class);

        assertThat(authCode.getValid(), is(Boolean.FALSE));

        verify(middlewareService, times(1)).validateAuthCode(OP_ID, OP_DATA, TAN_CODE);
    }

    @Test
    public void validateNotFound() throws Exception {
        SCAOperationTO operation = new SCAOperationTO(OP_DATA, VALIDITY_SECONDS, TAN_CODE);

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
        SCAOperationTO operation = new SCAOperationTO(OP_DATA, VALIDITY_SECONDS, TAN_CODE);

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
        SCAOperationTO operation = new SCAOperationTO(OP_DATA, VALIDITY_SECONDS, TAN_CODE);

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
        SCAOperationTO operation = new SCAOperationTO(OP_DATA, VALIDITY_SECONDS, TAN_CODE);

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
}