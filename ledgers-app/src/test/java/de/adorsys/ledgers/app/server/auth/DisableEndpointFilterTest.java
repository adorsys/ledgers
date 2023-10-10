/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.server.auth;

import de.adorsys.ledgers.middleware.rest.resource.DataMgmtStaffAPI;
import jakarta.servlet.ServletException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisableEndpointFilterTest {
    private final String servletPrefix = DataMgmtStaffAPI.BASE_PATH;
    @InjectMocks
    private DisableEndpointFilter filter;
    @Mock
    private Environment environment;

    @Test
    void doFilterInternal_develop_should_pass() {
        List<String> servletPaths = Arrays.asList(
                "/SomeServletPath/SomeCallUri",
                servletPrefix + "/transactions/132",
                servletPrefix + "/branch/132",
                servletPrefix + "/upload",
                servletPrefix + "/currencies");
        servletPaths.forEach(p -> testSuccessCases(new String[]{"sandbox", "postgres"}, p, false));
        servletPaths.forEach(p -> testSuccessCases(new String[]{"develop", "postgres"}, p, false));
    }

    @Test
    void doFilterInternal_prod_currencies_should_pass() {
        List<String> okServletPaths = Arrays.asList(
                "/SomeServletPath/SomeCallUri",
                servletPrefix + "/currencies",
                servletPrefix + "/branch");
        List<String> nokServletPaths = Arrays.asList(
                servletPrefix + "/transactions/132",
                servletPrefix + "/branch/132",
                servletPrefix + "/upload");
        okServletPaths.forEach(p -> testSuccessCases(new String[]{"postgres"}, p, false));
        nokServletPaths.forEach(p -> testSuccessCases(new String[]{"postgres"}, p, true));
    }


    @SneakyThrows({ServletException.class, IOException.class})
    void testSuccessCases(String[] profiles, String servletPath, boolean shouldFail) {
        when(environment.getActiveProfiles()).thenReturn(profiles);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath(servletPath);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, new MockFilterChain());

        if (shouldFail) {
            assertThat(response.getStatus()).as("path: %s", servletPath).isEqualTo(404);
        } else {
            assertThat(response.getStatus()).as("path: %s", servletPath).isNotEqualTo(404);
        }
    }
}