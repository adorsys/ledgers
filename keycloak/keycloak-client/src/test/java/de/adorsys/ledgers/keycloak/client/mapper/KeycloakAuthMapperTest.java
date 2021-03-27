package de.adorsys.ledgers.keycloak.client.mapper;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeycloakAuthMapperTest {

    private final KeycloakAuthMapper mapper = Mappers.getMapper(KeycloakAuthMapper.class);
    private static final Date NOW = Date.from(Instant.now());
    private static final int MILLIS_IN_SECOND = 1000;

    @Test
    void toAccessToken() {
        RefreshableKeycloakSecurityContext context = getTokenContext();
        AccessTokenTO token = mapper.toAccessToken(context);
        assertEquals("tokenString", token.getAccessToken());
        assertEquals(NOW.getTime() * MILLIS_IN_SECOND, token.getExp().getTime());
        assertEquals(new Date(615586932L).getTime() * MILLIS_IN_SECOND, token.getIat().getTime());
        assertEquals("id", token.getJti());
        assertEquals(new HashSet<>(Arrays.asList("profile", "openId")), token.getScopes());
        assertEquals("anton.brueckner", token.getLogin());
        assertEquals("subj", token.getSub());
        assertEquals(UserRoleTO.CUSTOMER, token.getRole());
    }

    private RefreshableKeycloakSecurityContext getTokenContext() {
        return new RefreshableKeycloakSecurityContext(null, null, "tokenString", getAccessToken(), null, null, null);
    }

    private AccessToken getAccessToken() {
        AccessToken token = new AccessToken();
        token.id("id");
        token.setName("anton.brueckner");
        token.issuedAt(615586932);
        token.exp(NOW.getTime());
        token.setScope("profile openId");
        token.setPreferredUsername("anton.brueckner");
        token.setSubject("subj");
        token.setRealmAccess(new AccessToken.Access().addRole("CUSTOMER"));

        return token;
    }

    @Test
    void toBearer() {
        BearerTokenTO result = mapper.toBearer(getAccessToken(), "tokenString");
        assertEquals("tokenString", result.getAccess_token());
        assertEquals("tokenString", result.getAccessTokenObject().getAccessToken());
        assertEquals(NOW.getTime() * MILLIS_IN_SECOND, result.getAccessTokenObject().getExp().getTime());
        assertEquals(new Date(615586932L).getTime() * MILLIS_IN_SECOND, result.getAccessTokenObject().getIat().getTime());
        assertEquals("id", result.getAccessTokenObject().getJti());
        assertEquals(new HashSet<>(Arrays.asList("profile", "openId")), result.getScopes());
        assertEquals("anton.brueckner", result.getAccessTokenObject().getLogin());
        assertEquals("subj", result.getAccessTokenObject().getSub());
        assertEquals(UserRoleTO.CUSTOMER, result.getAccessTokenObject().getRole());
        System.out.println();
    }
}