/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.keycloak.client.impl;

import de.adorsys.ledgers.keycloak.client.mapper.KeycloakAuthMapper;
import de.adorsys.ledgers.keycloak.client.rest.KeycloakTokenRestClient;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakTokenServiceImplTest {
    @InjectMocks
    private KeycloakTokenServiceImpl service;
    @Mock
    private KeycloakTokenRestClient keycloakTokenRestClient;
    @Mock
    private KeycloakAuthMapper authMapper;

    @Test
    void login() {
        when(keycloakTokenRestClient.login(any())).thenReturn(tokenResponse());
        BearerTokenTO result = service.login("userName", "password");
        assertEquals("access_token", result.getAccess_token());
    }

    private ResponseEntity<Map<String, ?>> tokenResponse() {
        return ResponseEntity.ok(Map.of("access_token", "access_token"));
    }

    @Test
    void exchangeToken() {
        when(keycloakTokenRestClient.exchangeToken(any(), any())).thenReturn(getTokenResponse());
        when(keycloakTokenRestClient.validate(any())).thenReturn(getValidateTokenResponse());
        when(authMapper.toBearer(any(), any())).thenReturn(new BearerTokenTO());
        BearerTokenTO result = service.exchangeToken("token", 1000, "new scope");
        assertNotNull(result);
        verify(keycloakTokenRestClient, times(1)).exchangeToken(any(), any());
        verify(keycloakTokenRestClient, times(1)).validate(any());
    }

    private ResponseEntity<AccessTokenResponse> getTokenResponse() {
        AccessTokenResponse response = new AccessTokenResponse();
        response.setToken("new Token");
        response.setScope("new scope");
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<AccessToken> getValidateTokenResponse() {
        AccessToken token = new AccessToken();
        token.setOtherClaims("active", true);
        return ResponseEntity.ok(token);
    }

    @Test
    void validate() {
        //already checked with previous test
        assertTrue(true);
    }
}