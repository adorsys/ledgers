package de.adorsys.ledgers.keycloak.client.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class KeycloakClientConfig {
    public static final String DEFAULT_ADMIN_CLIENT = "admin-cli";
    private String masterRealm = "master";

    @Value("${keycloak.auth-server-url:}")
    private String authServerUrl;

    @Value("${keycloak-sync.admin.username:}")
    private String userName;

    @Value("${keycloak-sync.admin.password:}")
    private String password;

    @Value("${keycloak.resource:" + DEFAULT_ADMIN_CLIENT + "}")
    private String adminClient;

    @Value("${keycloak.resource}")
    private String externalClientId;

    @Value("${keycloak.credentials.secret:}")
    private String clientSecret;

    @Value("${keycloak.realm}")
    private String clientRealm;

    @Value("${keycloak.public-client}")
    private boolean publicClient;

    public String getRootPath() {
        return authServerUrl + "/realms/" + clientRealm;
    }
}