/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.middleware.api.domain.oauth.OauthCodeResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.impl.converter.OauthServerInfoMapper;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.oauth.OauthCodeResponseBO;
import de.adorsys.ledgers.um.api.domain.oauth.OauthTokenHolder;
import de.adorsys.ledgers.um.api.service.OauthAuthorisationService;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.ledgers.middleware.api.domain.Constants.SCOPE_FULL_ACCESS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiddlewareOauthServiceImplTest {
    @InjectMocks
    private MiddlewareOauthServiceImpl service;
    @Mock
    private OauthAuthorisationService oauthAuthorisationService;
    @Mock
    private OauthServerInfoMapper oauthServerInfoMapper;
    @Mock
    private KeycloakTokenService tokenService;
    @Mock
    private UserService userService;

    private static final String LOGIN = "login";
    private static final String PIN = "pin";

    @Test
    void oauthCode() {
        String uri = "http://localhost:8080";
        when(tokenService.login(any(), any())).thenReturn(new BearerTokenTO());
        when(userService.findByLogin(any())).thenReturn(new UserBO());
        when(oauthAuthorisationService.oauthCode(any(), any(), eq(false))).thenReturn(new OauthCodeResponseBO());
        OauthCodeResponseTO result = service.oauthCode(LOGIN, PIN, uri);
        assertNotNull(result);
        assertTrue(result.getRedirectUri().contains("?code=null"));
        assertTrue(result.getRedirectUri().contains(uri));
    }

    @Test
    void testOauthCode() {
        String uri = "http://localhost:8080";
        when(oauthAuthorisationService.oauthCode(any(), any(), eq(true))).thenReturn(new OauthCodeResponseBO());
        OauthCodeResponseTO result = service.oauthCode(new ScaInfoTO(), uri);
        assertNotNull(result);
        assertTrue(result.getRedirectUri().contains("?code=null"));
        assertTrue(result.getRedirectUri().contains(uri));
    }

    @Test
    void oauthToken_unfinished() {
        when(oauthAuthorisationService.oauthToken(any())).thenReturn(new OauthTokenHolder("old", false));
        when(tokenService.validate(any())).thenReturn(new BearerTokenTO());
        BearerTokenTO result = service.oauthToken("code");
        assertNotNull(result);
        verify(tokenService, times(1)).validate(any());
    }

    @Test
    void oauthToken_finished() {
        when(oauthAuthorisationService.oauthToken(any())).thenReturn(new OauthTokenHolder("old", true));
        when(tokenService.exchangeToken(any(), any(), eq(SCOPE_FULL_ACCESS))).thenReturn(new BearerTokenTO());
        BearerTokenTO result = service.oauthToken("code");
        assertNotNull(result);
        verify(tokenService, times(1)).exchangeToken(any(), any(), any());
    }
}