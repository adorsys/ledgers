/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.service.OperationService;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pro.javatar.commons.reader.JsonReader;
import pro.javatar.commons.reader.ResourceReader;

import static de.adorsys.ledgers.middleware.rest.utils.Constants.PAYMENT_TYPE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OperationInitiationResourceTest {

    private static final String USER_ID = "kjk345knkj45";
    private static final String OPERATION_ID = "operation_id";

    @InjectMocks
    private OperationInitiationResource operationInitiationResource;

    @Mock
    private OperationService operationService;
    @Mock
    private ScaInfoHolder scaInfoHolder;

    private MockMvc mockMvc;
    private final ResourceReader jsonReader = JsonReader.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    private ScaInfoTO scaInfoTO;

    @BeforeEach
    void setUp() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new ParameterNamesModule());

        mockMvc = MockMvcBuilders.standaloneSetup(operationInitiationResource)
                          .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                          .build();

        scaInfoTO = new ScaInfoTO();
        scaInfoTO.setUserId(USER_ID);
    }

    @Test
    void initiatePayment() throws Exception {
        PaymentTO paymentTO = jsonReader.getObjectFromFile("json/resource/payment-to.json", PaymentTO.class);
        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);
        when(operationService.resolveInitiation(OpTypeTO.PAYMENT, null, paymentTO, scaInfoTO)).thenReturn(new GlobalScaResponseTO());

        mockMvc.perform(MockMvcRequestBuilders.post("/operation/payment")
                                .param(PAYMENT_TYPE, PaymentTypeTO.SINGLE.name())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(jsonReader.getStringFromFile("json/resource/payment-to.json")))
                .andExpect(status().isCreated());
    }

    @Test
    void initiatePmtCancellation() throws Exception {
        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);
        when(operationService.resolveInitiation(OpTypeTO.CANCEL_PAYMENT, OPERATION_ID, null, scaInfoTO)).thenReturn(new GlobalScaResponseTO());

        mockMvc.perform(MockMvcRequestBuilders.post("/operation/cancellation/{opId}", OPERATION_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
    }

    @Test
    void initiateAisConsent() throws Exception {
        AisConsentTO aisConsentTO = jsonReader.getObjectFromFile("json/resource/ais-consent-to.json", AisConsentTO.class);
        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);
        when(operationService.resolveInitiation(OpTypeTO.CONSENT, null, aisConsentTO, scaInfoTO)).thenReturn(new GlobalScaResponseTO());

        mockMvc.perform(MockMvcRequestBuilders.post("/operation/consent")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(jsonReader.getStringFromFile("json/resource/ais-consent-to.json")))
                .andExpect(status().isCreated());
    }

    @Test
    void execution() throws Exception {
        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);
        when(operationService.execute(OpTypeTO.PAYMENT, OPERATION_ID, scaInfoTO)).thenReturn(new GlobalScaResponseTO());

        mockMvc.perform(MockMvcRequestBuilders.post("/operation/{opType}/{opId}/execution", OpTypeTO.PAYMENT.name(), OPERATION_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }
}