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
import de.adorsys.ledgers.middleware.api.domain.general.BbanStructure;
import de.adorsys.ledgers.middleware.api.domain.general.RecoveryPointTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.api.service.CurrencyService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareCleanupService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareRecoveryService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DataMgmtStaffResourceTest {

    private static final String ACCOUNT_ID = "account-id";
    private static final String USER_ID = "kjk345knkj45";
    private static final String BRANCH_ID = "branch-id";

    private static final byte[] EMPTY_BODY = new byte[0];

    @InjectMocks
    private DataMgmtStaffResource dataMgmtStaffResource;

    @Mock
    private MiddlewareCleanupService cleanupService;
    @Mock
    private ScaInfoHolder scaInfoHolder;
    @Mock
    private CurrencyService currencyService;
    @Mock
    private AppManagementService appManagementService;
    @Mock
    private MiddlewareRecoveryService recoveryService;

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

        mockMvc = MockMvcBuilders.standaloneSetup(dataMgmtStaffResource)
                          .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                          .build();
        scaInfoTO = new ScaInfoTO();
        scaInfoTO.setUserRole(UserRoleTO.CUSTOMER);
    }

    @Test
    void account() throws Exception {
        when(scaInfoHolder.getUserId()).thenReturn(USER_ID);
        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);

        mockMvc.perform(MockMvcRequestBuilders.delete("/staff-access/data/transactions/{accountId}", ACCOUNT_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().bytes(EMPTY_BODY));

        verify(cleanupService, times(1)).deleteTransactions(USER_ID, UserRoleTO.CUSTOMER, ACCOUNT_ID);
    }

    @Test
    void depositAccount() throws Exception {
        when(scaInfoHolder.getUserId()).thenReturn(USER_ID);
        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);

        mockMvc.perform(MockMvcRequestBuilders.delete("/staff-access/data/account/{accountId}", ACCOUNT_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().bytes(EMPTY_BODY));

        verify(cleanupService, times(1)).deleteAccount(USER_ID, UserRoleTO.CUSTOMER, ACCOUNT_ID);
    }

    @Test
    void user() throws Exception {
        when(scaInfoHolder.getUserId()).thenReturn(USER_ID);
        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);

        mockMvc.perform(MockMvcRequestBuilders.delete("/staff-access/data/user/{userId}", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().bytes(EMPTY_BODY));

        verify(cleanupService, times(1)).deleteUser(USER_ID, UserRoleTO.CUSTOMER, USER_ID);
    }

    @Test
    void branch() throws Exception {
        when(scaInfoHolder.getUserId()).thenReturn(USER_ID);
        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);

        mockMvc.perform(MockMvcRequestBuilders.delete("/staff-access/data/branch/{branchId}", BRANCH_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().bytes(EMPTY_BODY));

        verify(cleanupService, times(1)).removeBranch(USER_ID, UserRoleTO.CUSTOMER, BRANCH_ID);
    }

    @Test
    void uploadData() throws Exception {
        UploadedDataTO uploadedDataTO = new UploadedDataTO();
        uploadedDataTO.setBranch(BRANCH_ID);

        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/staff-access/data/upload")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(mapper.writeValueAsString(uploadedDataTO)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(EMPTY_BODY));

        verify(appManagementService, times(1)).uploadData(uploadedDataTO, scaInfoTO);
    }

    @Test
    void currencies() throws Exception {
        when(currencyService.getSupportedCurrencies()).thenReturn(Set.of(Currency.getInstance("USD")));

        mockMvc.perform(MockMvcRequestBuilders.get("/staff-access/data/currencies")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(equalToIgnoringCase("[\"USD\"]")));
    }

    @Test
    void branchId() throws Exception {
        BbanStructure bbanStructure = new BbanStructure();
        bbanStructure.setCountryPrefix("pref");
        bbanStructure.setLength(200);
        bbanStructure.setEntryType(BbanStructure.EntryType.A);

        when(appManagementService.generateNextBban(bbanStructure)).thenReturn(BRANCH_ID);

        mockMvc.perform(MockMvcRequestBuilders.post("/staff-access/data/branch")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(mapper.writeValueAsString(bbanStructure)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(BRANCH_ID)));
    }

    @Test
    void createPoint() throws Exception {
        RecoveryPointTO recoveryPointTO = new RecoveryPointTO();
        recoveryPointTO.setId(123L);
        recoveryPointTO.setBranchId(BRANCH_ID);
        recoveryPointTO.setDescription("description");
        recoveryPointTO.setRollBackTime(LocalDateTime.now());

        when(scaInfoHolder.getUserId()).thenReturn(USER_ID);

        mockMvc.perform(MockMvcRequestBuilders.post("/staff-access/data/point")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(mapper.writeValueAsString(recoveryPointTO)))
                .andExpect(status().isCreated())
                .andExpect(content().bytes(EMPTY_BODY));

        verify(recoveryService, times(1)).createRecoveryPoint(eq(USER_ID), any(RecoveryPointTO.class));
    }

    @Test
    void getAllPoints() throws Exception {
        when(scaInfoHolder.getUserId()).thenReturn(USER_ID);
        when(recoveryService.getAll(USER_ID)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/staff-access/data/point/all")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getPoint() throws Exception {
        RecoveryPointTO recoveryPointTO = new RecoveryPointTO();
        recoveryPointTO.setId(123L);
        recoveryPointTO.setBranchId(BRANCH_ID);
        recoveryPointTO.setDescription("description");
        recoveryPointTO.setRollBackTime(LocalDateTime.now());

        when(scaInfoHolder.getUserId()).thenReturn(USER_ID);
        when(recoveryService.getPointById(USER_ID, 123L)).thenReturn(recoveryPointTO);


        mockMvc.perform(MockMvcRequestBuilders.get("/staff-access/data/point/{id}", 123)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(recoveryPointTO)));
    }

    @Test
    void deletePoint() throws Exception {
        when(scaInfoHolder.getUserId()).thenReturn(USER_ID);

        mockMvc.perform(MockMvcRequestBuilders.delete("/staff-access/data/point/{id}", 123)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andExpect(content().bytes(EMPTY_BODY));

        verify(recoveryService, times(1)).deleteById(USER_ID, 123L);
    }
}