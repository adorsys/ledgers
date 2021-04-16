package de.adorsys.ledgers.middleware.rest.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareRecoveryService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserMgmtStaffResourceTest {

    private static final String BRANCH = "branchId";
    private static final String USER_ID = "kjk345knkj45";

    private static final byte[] EMPTY_BODY = new byte[0];

    @InjectMocks
    private UserMgmtStaffResource userMgmtStaffResource;

    @Mock
    private MiddlewareUserManagementService middlewareUserService;
    @Mock
    private ScaInfoHolder scaInfoHolder;
    @Mock
    private MiddlewareRecoveryService middlewareRecoveryService;

    private MockMvc mockMvc;
    private final ResourceReader jsonReader = JsonReader.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();
    private ScaInfoTO scaInfoTO;
    private UserTO userTO;


    @BeforeEach
    void setUp() throws IOException {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new ParameterNamesModule());

        mockMvc = MockMvcBuilders.standaloneSetup(userMgmtStaffResource)
                          .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                          .build();

        scaInfoTO = new ScaInfoTO();
        scaInfoTO.setUserId(USER_ID);

        userTO = jsonReader.getObjectFromFile("json/resource/user-to.json", UserTO.class);
    }

    @Test
    void register() throws Exception {
        when(middlewareUserService.create(userTO)).thenReturn(userTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/staff-access/users/register")
                                .param("branch", BRANCH)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(jsonReader.getStringFromFile("json/resource/user-to.json")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalToIgnoringCase(mapper.writeValueAsString(userTO))));


        assertNull(userTO.getPin());
        assertEquals(Collections.singletonList(UserRoleTO.STAFF), userTO.getUserRoles());
    }

    @Test
    void modifyUser() throws Exception {
        when(middlewareUserService.updateUser(BRANCH, userTO)).thenReturn(userTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/staff-access/users/modify")
                                .param("branch", BRANCH)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(jsonReader.getStringFromFile("json/resource/user-to.json")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalToIgnoringCase(mapper.writeValueAsString(userTO))));
    }

    @Test
    void createUser() throws Exception {
        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);
        when(middlewareUserService.findById(USER_ID)).thenReturn(userTO);
        when(middlewareUserService.create(userTO)).thenReturn(userTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/staff-access/users")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(jsonReader.getStringFromFile("json/resource/user-to.json")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalToCompressingWhiteSpace(mapper.writeValueAsString(userTO))));
    }

    @Test
    void getBranchUsersByRoles() throws Exception {
        ArgumentCaptor<CustomPageableImpl> customPageableCaptor = ArgumentCaptor.forClass(CustomPageableImpl.class);
        when(scaInfoHolder.getUserId()).thenReturn(USER_ID);
        when(middlewareUserService.findById(USER_ID)).thenReturn(userTO);

        CustomPageImpl<UserTO> customPage = new CustomPageImpl<>();
        customPage.setContent(Collections.singletonList(userTO));
        when(middlewareUserService.getUsersByBranchAndRoles(eq(""), eq(userTO.getBranch()), eq(""), eq(""),
                                                            eq(List.of(UserRoleTO.STAFF)), eq(true), customPageableCaptor.capture()))
                .thenReturn(customPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/staff-access/users")
                                .param("roles", "STAFF")
                                .param("blockedParam", "true")
                                .param("page", "1")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(jsonReader.getStringFromFile("json/resource/user-to.json")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonReader.getStringFromFile("json/resource/created-user-resp.json")));

        assertEquals(1, customPageableCaptor.getValue().getPage());
        assertEquals(10, customPageableCaptor.getValue().getSize());
    }

    @Test
    void getBranchUserLogins() throws Exception {
        when(scaInfoHolder.getUserId()).thenReturn(USER_ID);
        when(middlewareUserService.findById(USER_ID)).thenReturn(userTO);
        when(middlewareUserService.getBranchUserLogins(BRANCH)).thenReturn(Collections.singletonList("superman"));

        mockMvc.perform(MockMvcRequestBuilders.get("/staff-access/users/logins")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(equalToIgnoringCase("[\"superman\"]")));
    }

    @Test
    void getBranchUserLoginsByBranchId() throws Exception {
        when(middlewareUserService.findById(BRANCH)).thenReturn(userTO);
        when(middlewareUserService.getBranchUserLogins(BRANCH)).thenReturn(Collections.singletonList("superman"));

        mockMvc.perform(MockMvcRequestBuilders.get("/staff-access/users/logins/{branchId}", BRANCH)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(equalToIgnoringCase("[\"superman\"]")));

        verifyNoInteractions(scaInfoHolder);
    }

    @Test
    void getBranchUserById() throws Exception {
        when(middlewareUserService.findById(USER_ID)).thenReturn(userTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/staff-access/users/{userId}", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(equalToIgnoringCase(mapper.writeValueAsString(userTO))));
    }

    @Test
    void updateUserScaData() throws Exception {
        List<ScaUserDataTO> scaUserDataTOList = jsonReader.getListFromFile("json/resource/sca-user-data.json", ScaUserDataTO.class);
        when(middlewareUserService.findById(USER_ID)).thenReturn(userTO);
        when(middlewareUserService.updateScaData("superman", scaUserDataTOList)).thenReturn(userTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/staff-access/users/{userId}/sca-data", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(jsonReader.getStringFromFile("json/resource/sca-user-data.json")))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", equalTo("/staff-access/users/null")))
                .andExpect(content().bytes(EMPTY_BODY));
    }

    @Test
    void updateAccountAccessForUser() throws Exception {
        when(scaInfoHolder.getScaInfo()).thenReturn(scaInfoTO);

        mockMvc.perform(MockMvcRequestBuilders.put("/staff-access/users/access/{userId}", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(jsonReader.getStringFromFile("json/resource/account-access.json")))
                .andExpect(status().isOk())
                .andExpect(content().bytes(EMPTY_BODY));

        AccountAccessTO accountAccessTO = jsonReader.getObjectFromFile("json/resource/account-access.json", AccountAccessTO.class);
        verify(middlewareUserService).updateAccountAccess(scaInfoTO, USER_ID, accountAccessTO);
    }

    @Test
    void changeStatus() throws Exception {
        when(middlewareUserService.changeStatus(USER_ID, false)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/staff-access/users/{userId}/status", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("true")));
    }

    @Test
    void revertDatabase() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/staff-access/users/revert")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(jsonReader.getStringFromFile("json/resource/revert-request.json")))
                .andExpect(status().isOk())
                .andExpect(content().bytes(EMPTY_BODY));

        verify(middlewareRecoveryService, times(1)).revertDatabase(BRANCH, 321L);
    }
}