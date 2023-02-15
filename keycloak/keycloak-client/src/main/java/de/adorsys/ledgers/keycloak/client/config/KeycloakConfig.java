/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.keycloak.client.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Bean
    Keycloak getKeycloakAdminClient(KeycloakClientConfig config) {
        return KeycloakBuilder.builder()
                       .serverUrl(config.getAuthServerUrl())
                       .realm(config.getMasterRealm())
                       .clientId("admin-cli")
                       .username(config.getUserName())
                       .password(config.getPassword())
                       .build();
    }
}
