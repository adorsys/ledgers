/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.keycloak.client.mapper;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessToken;
import org.mapstruct.factory.Mappers;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeycloakAuthMapperTest {

    private final KeycloakAuthMapper mapper = Mappers.getMapper(KeycloakAuthMapper.class);
    private static final Date NOW = Date.from(Instant.now());
    private static final String NAME = "anton.brueckner";
    private static final String TOKEN_STRING = "tokenString";
    private static final String JTI = "id";
    private static final String SUBJECT = "subj";

    private static final int MILLIS_IN_SECOND = 1000;

    @Test
    void toAccessToken() {
        // Given
        ArrayList<String> rolesString = new ArrayList<>(List.of("CUSTOMER"));
        Map<String, List> roles = Map.of("roles", rolesString);

        Jwt jwt = Jwt.withTokenValue(TOKEN_STRING)
                          .header("header", "value")
                          .claim("realm_access", roles)
                          .claim("jti", JTI)
                          .claim("name", NAME)
                          .claim("scope", "profile openId")
                          .subject(SUBJECT)
                          .issuedAt(NOW.toInstant())
                          .expiresAt(NOW.toInstant().plusSeconds(10))
                          .build();

        // When
        AccessTokenTO token = mapper.toAccessTokenFromJwt(jwt);

        // Then
        assertEquals(TOKEN_STRING, token.getAccessToken());
        assertEquals(NOW.toInstant().plusSeconds(10), token.getExp().toInstant());
        assertEquals(NOW.toInstant().toEpochMilli(), token.getIat().getTime());
        assertEquals(JTI, token.getJti());
        assertEquals(new HashSet<>(Arrays.asList("profile", "openId")), token.getScopes());
        assertEquals(NAME, token.getLogin());
        assertEquals(SUBJECT, token.getSub());
        assertEquals(UserRoleTO.CUSTOMER, token.getRole());
    }


    @Test
    void toBearer() {
        // When
        BearerTokenTO result = mapper.toBearer(getAccessToken(), TOKEN_STRING);

        // Then
        assertEquals(TOKEN_STRING, result.getAccess_token());
        assertEquals(TOKEN_STRING, result.getAccessTokenObject().getAccessToken());
        assertEquals(NOW.getTime() * MILLIS_IN_SECOND, result.getAccessTokenObject().getExp().getTime());
        assertEquals(new Date(615586932L).getTime() * MILLIS_IN_SECOND, result.getAccessTokenObject().getIat().getTime());
        assertEquals(JTI, result.getAccessTokenObject().getJti());
        assertEquals(new HashSet<>(Arrays.asList("profile", "openId")), result.getScopes());
        assertEquals(NAME, result.getAccessTokenObject().getLogin());
        assertEquals(SUBJECT, result.getAccessTokenObject().getSub());
        assertEquals(UserRoleTO.CUSTOMER, result.getAccessTokenObject().getRole());
        System.out.println();
    }

    private AccessToken getAccessToken() {
        AccessToken token = new AccessToken();
        token.id("id");
        token.setName(NAME);
        token.issuedAt(615586932);
        token.exp(NOW.getTime());
        token.setScope("profile openId");
        token.setPreferredUsername(NAME);
        token.setSubject(SUBJECT);
        token.setRealmAccess(new AccessToken.Access().addRole("CUSTOMER"));

        return token;
    }

}