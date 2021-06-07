package de.adorsys.ledgers.middleware.rest.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConsentResourceTest {

    private static final String CONSENT_ID = "consent-id";

    @InjectMocks
    private ConsentResource resource;

    @Mock
    private ScaInfoHolder scaInfoHolder;
    @Mock
    private MiddlewareAccountManagementService middlewareAccountService;

    private MockMvc mockMvc;
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

        mockMvc = MockMvcBuilders.standaloneSetup(resource)
                          .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                          .build();
        scaInfoTO = new ScaInfoTO();
        scaInfoTO.setUserRole(UserRoleTO.CUSTOMER);
    }

    @Test
    void initiateAisConsent() throws Exception {
        AisConsentTO aisConsentTO = new AisConsentTO();
        aisConsentTO.setId(CONSENT_ID);
        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/consents/{consentId}", CONSENT_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(mapper.writeValueAsString(aisConsentTO)))
                .andExpect(status().isOk());

        verify(middlewareAccountService, times(1)).startAisConsent(scaInfoTO, CONSENT_ID, aisConsentTO);
    }

    @Test
    void initiatePiisConsent() throws Exception {
        AisConsentTO aisConsentTO = new AisConsentTO();
        aisConsentTO.setId(CONSENT_ID);
        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/consents/piis")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(mapper.writeValueAsString(aisConsentTO)))
                .andExpect(status().isOk());

        verify(middlewareAccountService, times(1)).startPiisConsent(scaInfoTO, aisConsentTO);
    }
}