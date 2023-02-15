/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package utils;

import de.adorsys.ledgers.keycloak.client.rest.KeycloakTokenRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackageClasses = {KeycloakTokenRestClient.class})
public class TestFeignConfiguration {
}
