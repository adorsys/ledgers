package de.adorsys.ledgers.middleware.resource;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.middleware.exception.ExceptionAdvisor;
import de.adorsys.ledgers.middleware.service.MiddlewareService;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentResultTO;
import de.adorsys.ledgers.middleware.service.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.service.exception.PaymentNotFoundMiddlewareException;
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
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentResourceTest {

    public static final String PAYMENT_ID = "myPaymentId";
    private MockMvc mockMvc;

    @InjectMocks
    private PaymentResource resource;

    @Mock
    private MiddlewareService middlewareService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders
                          .standaloneSetup(resource)
                          .setControllerAdvice(new ExceptionAdvisor())
                          .setMessageConverters(new MappingJackson2HttpMessageConverter())
                          .build();
    }

    @Test
    public void getPaymentStatusById() throws Exception {

        ResourceReader reader = YamlReader.getInstance();
        PaymentResultTO<TransactionStatusTO> paymentResult = readPaymentResult(reader);

        when(middlewareService.getPaymentStatusById(PAYMENT_ID)).thenReturn(paymentResult);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/payments/{id}/status", PAYMENT_ID))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.OK.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        PaymentResultTO actual = strToObj(content, PaymentResultTO.class);

        assertThat(actual.getResponseStatus(), is(paymentResult.getResponseStatus()));
        assertThat(actual.getPaymentResult(), is(paymentResult.getPaymentResult()));
        assertThat(actual.getMessages(), is(nullValue()));

        verify(middlewareService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }

    @Test
    public void getPaymentStatusByIdNotFound() throws Exception {


        when(middlewareService.getPaymentStatusById(PAYMENT_ID))
                .thenThrow(new PaymentNotFoundMiddlewareException("Payment with id=" + PAYMENT_ID + " not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/payments/{id}/status", PAYMENT_ID))
                                      .andDo(print())
                                      .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                                      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                      .andReturn();

        verify(middlewareService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }

    private PaymentResultTO<TransactionStatusTO> readPaymentResult(ResourceReader reader) throws IOException {
        return reader.getObjectFromResource(PaymentResource.class,"payment-result.yml", PaymentResultTO.class);
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