package de.adorsys.ledgers.um.rest.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
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

import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import pro.javatar.commons.reader.JsonReader;
import pro.javatar.commons.reader.ResourceReader;
import pro.javatar.commons.reader.YamlReader;

@SpringBootTest
@AutoConfigureMockMvc
public class UserResourceTest {

    private static final String USER_LOGIN = "vne";

    @InjectMocks
    private UserResource userResource; // user controller

    @Mock
    public UserService userService;

    private ResourceReader reader = YamlReader.getInstance();

    private MockMvc mockMvc;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders
                          .standaloneSetup(userResource)
                          .setMessageConverters(new MappingJackson2HttpMessageConverter())
                          .build();
    }

    @Test
    public void createUser() throws Exception {
        String jsonUser = JsonReader.getInstance().getStringFromFile("de/adorsys/ledgers/um/rest/controller/user.json");
        UserBO userBO = JsonReader.getInstance().getObjectFromString(jsonUser, UserBO.class);

        when(userService.create(any())).thenReturn(userBO);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post("/users/")
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(jsonUser))
            .andDo(print())
            .andExpect(status().is(HttpStatus.CREATED.value()))
            .andReturn();

        assertThat(mvcResult.getResponse().getStatus(), is(201));
        assertThat(mvcResult.getResponse().getHeader("Location"), is("/users/vne_ua"));

        verify(userService, times(1)).create(any());
    }

    @Test
    public void getUserByLogin() throws Exception {
        UserBO userBO = getUserBO();
        when(userService.findByLogin(USER_LOGIN)).thenReturn(userBO);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/users/").param("login", USER_LOGIN))
            .andDo(print())
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andReturn();

        assertThat(mvcResult.getResponse().getStatus(), is(200));

        String userString = mvcResult.getResponse().getContentAsString();
        UserBO user = JsonReader.getInstance().getObjectFromString(userString, UserBO.class);

        assertNotNull(user);
        assertEquals(user, userBO);

        verify(userService, times(1)).findByLogin(USER_LOGIN);
    }

    private UserBO getUserBO() {
        try {
            return this.reader.getObjectFromFile("de/adorsys/ledgers/um/rest/controller/user-BO.yml", UserBO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
