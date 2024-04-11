/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.mockbank;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import de.adorsys.ledgers.middleware.LedgersMiddlewareRestApplication;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.rest.resource.AppMgmtResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.ServletContext;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = LedgersMiddlewareRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@WebAppConfiguration
@ActiveProfiles("h2")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
class AppManagementResourceAdminIT {

    private MockMvc mockMvc;

    @Configuration
    static class ContextConfiguration {

        // this bean will be injected into the test
        @Bean
        public AccessTokenTO token() {
            AccessTokenTO token = new AccessTokenTO();
            token.setRole(UserRoleTO.CUSTOMER);
            token.setAccessToken("token");
            return token;
        }

        // this bean will be injected into the test also
        @Bean
        public BearerTokenTO bearerToken() {
            return new BearerTokenTO();
        }

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        @RequestScope
        public Authentication getAuthentication() {
            return auth().orElse(null);
        }

        static Optional<Authentication> auth() {
            return SecurityContextHolder.getContext() == null
                           ? Optional.empty()
                           : Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    void before() {
        this.mockMvc = MockMvcBuilders
                               .webAppContextSetup(this.wac)
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
}