/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.test;

import de.adorsys.ledgers.deposit.api.client.ExchangeRateClient;
import de.adorsys.ledgers.deposit.api.service.EnableDepositAccountService;
import de.adorsys.ledgers.keycloak.client.KeycloakClientConfiguration;
import de.adorsys.ledgers.middleware.impl.EnableLedgersMiddlewareService;
import de.adorsys.ledgers.postings.impl.EnablePostingService;
import de.adorsys.ledgers.sca.service.EnableSCAService;
import de.adorsys.ledgers.um.impl.EnableUserManagementService;
import de.adorsys.ledgers.util.EnableUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableLedgersMiddlewareService
@EnableDepositAccountService
@EnableUserManagementService
@EnableSCAService
@EnablePostingService
@EnableUtils
@Import(KeycloakClientConfiguration.class)
@EnableFeignClients(basePackageClasses = {ExchangeRateClient.class})
public class MiddlewareServiceApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(MiddlewareServiceApplication.class).run(args);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
