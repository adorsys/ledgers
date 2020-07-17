package de.adorsys.ledgers.app.server.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakConfigProperties {
    private String resource;
    private Credentials credentials;
    private String realm;
    private String authServerUrl;

    @Data
    public static class Credentials {
        private String secret;
    }

    public String getRootPath() {
        return authServerUrl + "/realms/" + realm;
    }
}
