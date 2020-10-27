package de.adorsys.ledgers.keycloak.client.rest;

import de.adorsys.ledgers.keycloak.client.model.TokenConfiguration;
import feign.Headers;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(value = "keycloakTokenRestClient",
        url = "${keycloak.auth-server-url}")
@Headers({"Content-Type: application/x-www-form-urlencoded"})
public interface KeycloakTokenRestClient {

    @PostMapping(value = "/realms/${keycloak.realm}/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<Map<String, ?>> login(MultiValueMap<String, Object> formParams);

    @PostMapping(value = "/realms/${keycloak.realm}/configurable-token")
    ResponseEntity<AccessTokenResponse> exchangeToken(@RequestHeader("Authorization") String token,
                                                      @RequestBody TokenConfiguration tokenConfiguration);

    @PostMapping(value = "/realms/${keycloak.realm}/protocol/openid-connect/token/introspect", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<AccessToken> validate(MultiValueMap<String, Object> formParams);

    @PutMapping(value = "/admin/realms/${keycloak.realm}/users/{user-id}/execute-actions-email")
    ResponseEntity<AccessTokenResponse> executeActionsEmail(@RequestHeader("Authorization") String bearerToken,
                                                            @PathVariable("user-id") String userId,
                                                            @RequestBody List<String> requiredActions);

}
