/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.keycloak.client.impl;

import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.keycloak.client.mapper.KeycloakAuthMapper;
import de.adorsys.ledgers.keycloak.client.model.TokenConfiguration;
import de.adorsys.ledgers.keycloak.client.rest.KeycloakTokenRestClient;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.JsonWebToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakTokenServiceImpl implements KeycloakTokenService {
    private static final String CLIENT_ID_KEY = "client_id";
    private static final String CLIENT_SECRET_KEY = "client_secret";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";
    private static final String GRANT_TYPE_KEY = "grant_type";
    private static final String PASSWORD_KEY = "password";

    @Value("${keycloak.resource:}")
    private String clientId;
    @Value("${keycloak.credentials.secret:}")
    private String clientSecret;
    private final KeycloakTokenRestClient keycloakTokenRestClient;
    private final KeycloakAuthMapper authMapper;

    @Override
    public BearerTokenTO login(String username, String password) {
        MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();
        formParams.add(GRANT_TYPE_KEY, "password");
        formParams.add("username", username);
        formParams.add(PASSWORD_KEY, password);
        formParams.add(CLIENT_ID_KEY, clientId);
        formParams.add(CLIENT_SECRET_KEY, clientSecret);
        ResponseEntity<Map<String, ?>> resp = keycloakTokenRestClient.login(formParams);
        HttpStatus statusCode = (HttpStatus) resp.getStatusCode();
        if (HttpStatus.OK != statusCode) {
            log.error("Could not obtain token by user credentials [{}]", username);
        }
        Map<String, ?> body = Objects.requireNonNull(resp).getBody();
        BearerTokenTO bearerTokenTO = new BearerTokenTO();
        bearerTokenTO.setAccess_token((String) Objects.requireNonNull(body).get(ACCESS_TOKEN_KEY));
        bearerTokenTO.setRefresh_token((String) Objects.requireNonNull(body).get(REFRESH_TOKEN_KEY));
        return bearerTokenTO;
    }

    @Override
    public BearerTokenTO exchangeToken(String oldToken, Integer timeToLive, String scope) {
        AccessTokenResponse response = keycloakTokenRestClient.exchangeToken("Bearer " + oldToken, new TokenConfiguration(timeToLive, scope)).getBody();
        return validate(Optional.ofNullable(response)
                                .map(AccessTokenResponse::getToken)
                                .orElse(""));
    }

    @Override
    public BearerTokenTO validate(String token) {
        MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();
        formParams.add("token", token);
        formParams.add(CLIENT_ID_KEY, clientId);
        formParams.add(CLIENT_SECRET_KEY, clientSecret);
        ResponseEntity<AccessToken> resp = keycloakTokenRestClient.validate(formParams);
        HttpStatus statusCode = (HttpStatus) resp.getStatusCode();
        if (HttpStatus.OK != statusCode) {
            log.error("Could not validate token");
        }
        Map<String, Object> claimsMap = Optional.ofNullable(resp.getBody())
                .map(JsonWebToken::getOtherClaims)
                .orElse(new HashMap<>());
        if (claimsMap.get("active").equals(false)) {
            throw new AccessDeniedException("Token Expired!");
        }
        return authMapper.toBearer(resp.getBody(), token);
    }

    @Override
    public BearerTokenTO refreshToken(String refreshToken) {
        MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();
        formParams.add(GRANT_TYPE_KEY, "refresh_token");
        formParams.add(CLIENT_ID_KEY, clientId);
        formParams.add(CLIENT_SECRET_KEY, clientSecret);
        formParams.add(REFRESH_TOKEN_KEY, refreshToken);
        ResponseEntity<Map<String, ?>> resp = keycloakTokenRestClient.login(formParams);
        HttpStatus statusCode = (HttpStatus) resp.getStatusCode();
        if (HttpStatus.OK != statusCode) {
            log.error("Could not obtain token by refresh token  [{}]", refreshToken);
            throw new AccessDeniedException("Invalid Refresh token");
        }
        Map<String, ?> body = Objects.requireNonNull(resp).getBody();
        BearerTokenTO bearerTokenTO = new BearerTokenTO();
        bearerTokenTO.setAccess_token((String) Objects.requireNonNull(body).get(ACCESS_TOKEN_KEY));
        bearerTokenTO.setRefresh_token((String) Objects.requireNonNull(body).get(REFRESH_TOKEN_KEY));

        return bearerTokenTO;
    }
}
