package de.adorsys.ledgers.middleware.rest.mockbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.middleware.LedgersMiddlewareRestApplication;
import de.adorsys.ledgers.middleware.rest.resource.AppMgmtResource;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = LedgersMiddlewareRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@WebAppConfiguration
@ActiveProfiles("h2")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseTearDown(value = {"MiddlewareServiceImplIT-db-delete.xml"}, type = DatabaseOperation.DELETE_ALL)
class AppManagementResourceAdminIT {

    private ObjectMapper mapper = new ObjectMapper();
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    void before() {
        this.mockMvc = MockMvcBuilders
                               .webAppContextSetup(this.wac)
                               .apply(springSecurity())
                               .build();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesGreetController() {
        // When
        ServletContext servletContext = wac.getServletContext();

        // Then
        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(wac.getBean(AppMgmtResource.class));
    }

    @Test
    void givenPingURI_whenMockMVC_thenReturnsPong() throws Exception {
        // When
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/management/app/ping"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.content().string("pong"));
    }

    @Test
    void givenAdminURI_whenMockMVC_thenReturnsAccessToken() throws Exception {
        String payload = mapper.writeValueAsString(AdminPayload.adminPayload());
        this.mockMvc.perform(
                MockMvcRequestBuilders.post("/management/app/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(print())
                .andExpect(MockMvcResultMatchers.content().string(StringContains.containsString(".")));
    }
}