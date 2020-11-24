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
    @Value("${keycloak.resource:}")
    private String clientId;
    @Value("${keycloak.credentials.secret:}")
    private String clientSecret;
    private final KeycloakTokenRestClient keycloakTokenRestClient;
    private final KeycloakAuthMapper authMapper;

    @Override
    public BearerTokenTO login(String username, String password) {
        MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();
        formParams.add("grant_type", "password");
        formParams.add("username", username);
        formParams.add("password", password);
        formParams.add("client_id", clientId);
        formParams.add("client_secret", clientSecret);
        ResponseEntity<Map<String, ?>> resp = keycloakTokenRestClient.login(formParams);
        HttpStatus statusCode = resp.getStatusCode();
        if (HttpStatus.OK != statusCode) {
            log.error("Could not obtain token by user credentials [{}]", username); //todo: throw specific exception
        }
        Map<String, ?> body = Objects.requireNonNull(resp).getBody();
        BearerTokenTO bearerTokenTO = new BearerTokenTO();
        bearerTokenTO.setAccess_token((String) Objects.requireNonNull(body).get("access_token"));
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
        formParams.add("client_id", clientId);
        formParams.add("client_secret", clientSecret);
        ResponseEntity<AccessToken> resp = keycloakTokenRestClient.validate(formParams);
        HttpStatus statusCode = resp.getStatusCode();
        if (HttpStatus.OK != statusCode) {
            log.error("Could not validate token"); //todo: throw specific exception
        }
        Map<String, Object> claimsMap = Optional.ofNullable(resp.getBody())
                                                .map(JsonWebToken::getOtherClaims)
                                                .orElse(new HashMap<>());
        if (claimsMap.get("active").equals(false)) {
            throw new AccessDeniedException("Token Expired!");
        }
        return authMapper.toBearer(resp.getBody(), token);
    }
}