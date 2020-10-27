package de.adorsys.ledgers.keycloak.client.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

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

    @Value("${spring.mail.host:}")
    private String emailHost;

    @Value("${spring.mail.port:}")
    private String emailPort;

    @Value("${spring.mail.properties.mail.ssl:false}")
    private String emailSsl;

    @Value("${spring.mail.properties.mail.auth:false}")
    private String emailAuth;

    @Value("${spring.mail.username:}")
    private String emailUser;

    @Value("${spring.mail.password:}")
    private String emailPassword;

    @Value("${spring.mail.properties.mail.from:}")
    private String emailFrom;

    @Value("${spring.mail.properties.mail.fromDisplayName:}")
    private String emailFromDisplayName;

    @Getter
    private Map<String, String> smtpServer = new HashMap<>();

    @PostConstruct
    public void doInit() {
        smtpServer.put("host", emailHost);
        smtpServer.put("port", emailPort);
        smtpServer.put("auth", emailAuth);
        smtpServer.put("ssl", emailSsl);
        smtpServer.put("from", emailFrom);
        smtpServer.put("fromDisplayName", emailFromDisplayName);
        smtpServer.put("user", emailUser);
        smtpServer.put("password", emailPassword);
    }
}