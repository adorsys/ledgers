package de.adorsys.ledgers.keycloak.client.impl;

import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.keycloak.client.mapper.KeycloakAuthMapper;
import de.adorsys.ledgers.keycloak.client.model.TokenConfiguration;
import de.adorsys.ledgers.keycloak.client.rest.KeycloakTokenRestClient;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.Objects;

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
    public BearerTokenBO login(String username, String password) {
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
        BearerTokenBO bearerTokenBO = new BearerTokenBO();
        bearerTokenBO.setAccess_token((String) Objects.requireNonNull(body).get("access_token"));
        return bearerTokenBO;
    }

    @Override
    public BearerTokenBO exchangeToken(String oldToken, Integer timeToLive) {
        return authMapper.toBearerTokenBO(
                keycloakTokenRestClient.exchangeToken("Bearer " + oldToken, new TokenConfiguration(timeToLive)).getBody()
        );
    }

    @Override
    public boolean validate(BearerTokenBO token) {
        MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();
        formParams.add("username", token.getAccessTokenObject().getLogin());
        formParams.add("token", token.getAccess_token());
        formParams.add("client_id", clientId);
        formParams.add("client_secret", clientSecret);
        ResponseEntity<Map<String, ?>> resp = keycloakTokenRestClient.validate(formParams);

        HttpStatus statusCode = resp.getStatusCode();
        if (HttpStatus.OK != statusCode) {
            log.error("Could not validate token for user [{}]", token.getAccessTokenObject().getLogin()); //todo: throw specific exception
        }
        Map<String, ?> body = Objects.requireNonNull(resp).getBody();
        return BooleanUtils.isTrue((Boolean) Objects.requireNonNull(body).get("active"));
    }
}
