package de.adorsys.ledgers.keycloak.client.rest;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "keycloakTokenRestClient",
        url = "${keycloak.auth-server-url}" + "/realms/" + "${keycloak.realm}")
@Headers({"Content-Type: application/x-www-form-urlencoded"})
public interface KeycloakTokenRestClient {

    @PostMapping(value = "/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<Map<String, ?>> login(MultiValueMap<String, Object> formParams);

    @PostMapping(value = "/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    ResponseEntity<Map<String, ?>> exchangeToken(@RequestParam("grant_type") String grantType,
                                                 @RequestParam("client_id") String clientId,
                                                 @RequestParam("client_secret") String clientSecret,
                                                 @RequestParam("subject_token") String subjectToken);

    @PostMapping(value = "/protocol/openid-connect/token/introspect", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<Map<String, ?>> validate(MultiValueMap<String, Object> formParams);

}
