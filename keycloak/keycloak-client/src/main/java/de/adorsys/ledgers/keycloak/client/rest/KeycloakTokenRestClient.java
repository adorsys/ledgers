package de.adorsys.ledgers.keycloak.client.rest;

import de.adorsys.ledgers.keycloak.client.model.TokenConfiguration;
import feign.Headers;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = "keycloakTokenRestClient",
        url = "${keycloak.auth-server-url}" + "/realms/" + "${keycloak.realm}")
@Headers({"Content-Type: application/x-www-form-urlencoded"})
public interface KeycloakTokenRestClient {

    @PostMapping(value = "/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<Map<String, ?>> login(MultiValueMap<String, Object> formParams);

    @PostMapping(value = "/configurable-token")
    ResponseEntity<AccessTokenResponse> exchangeToken(@RequestHeader("Authorization") String token,
                                                      @RequestBody TokenConfiguration tokenConfiguration);

    @PostMapping(value = "/protocol/openid-connect/token/introspect", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<AccessToken> validate(MultiValueMap<String, Object> formParams);

}
