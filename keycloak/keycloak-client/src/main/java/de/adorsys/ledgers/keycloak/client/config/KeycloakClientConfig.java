/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

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

    @Value("${spring.mail.username:}")
    private String emailUser;

    @Value("${spring.mail.password:}")
    private String emailPassword;

    @Value("${spring.mail.properties.smtp.auth:false}")
    private String emailAuth;

    @Value("${spring.mail.properties.smtp.ssl:false}")
    private String emailSsl;

    @Value("${spring.mail.properties.smtp.start-tls:false}")
    private String startTls;

    @Value("${spring.mail.properties.smtp.from:}")
    private String emailFrom;

    @Value("${spring.mail.properties.smtp.envelope-from:}")
    private String envelopeFrom;

    @Value("${spring.mail.properties.smtp.from-display-name:}")
    private String emailFromDisplayName;

    @Value("${spring.mail.properties.smtp.reply-to:}")
    private String replyTo;

    @Value("${spring.mail.properties.smtp.reply-to-display-name:}")
    private String replyToDisplayName;

    @Getter
    private Map<String, String> smtpServer = new HashMap<>();

    @PostConstruct
    public void doInit() {
        smtpServer.put("host", emailHost);
        smtpServer.put("port", emailPort);
        smtpServer.put("user", emailUser);
        smtpServer.put("password", emailPassword);
        smtpServer.put("auth", emailAuth);
        smtpServer.put("ssl", emailSsl);
        smtpServer.put("starttls", startTls);
        smtpServer.put("from", emailFrom);
        smtpServer.put("fromDisplayName", emailFromDisplayName);
        smtpServer.put("envelopeFrom", envelopeFrom);
        smtpServer.put("replyTo", replyTo);
        smtpServer.put("replyToDisplayName", replyToDisplayName);
    }
}