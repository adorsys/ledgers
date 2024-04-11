/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app;

import de.adorsys.ledgers.app.initiation.BankInitService;
import de.adorsys.ledgers.deposit.api.client.ExchangeRateClient;
import de.adorsys.ledgers.deposit.api.service.EnableDepositAccountService;
import de.adorsys.ledgers.keycloak.client.KeycloakClientConfiguration;
import de.adorsys.ledgers.middleware.client.rest.AccountRestClient;
import de.adorsys.ledgers.middleware.impl.EnableLedgersMiddlewareService;
import de.adorsys.ledgers.middleware.rest.EnableLedgersMiddlewareRest;
import de.adorsys.ledgers.postings.impl.EnablePostingService;
import de.adorsys.ledgers.sca.mock.MockSmtpServer;
import de.adorsys.ledgers.sca.service.EnableSCAService;
import de.adorsys.ledgers.um.impl.EnableUserManagementService;
import de.adorsys.ledgers.util.EnableUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

@EnableScheduling
@SpringBootApplication
@EnableUserManagementService
@EnableSCAService
@EnablePostingService
@EnableDepositAccountService
@EnableLedgersMiddlewareService
@EnableLedgersMiddlewareRest
@EnableUtils
@EnableFeignClients(basePackageClasses = {AccountRestClient.class, ExchangeRateClient.class})
@Import(KeycloakClientConfiguration.class)
public class LedgersApplication implements ApplicationListener<ApplicationReadyEvent> {
    private final BankInitService bankInitService;
    private final Environment env;

    @Autowired
    public LedgersApplication(BankInitService bankInitService, Environment env) {
        this.bankInitService = bankInitService;
        this.env = env;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(LedgersApplication.class).run(args);
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        bankInitService.init();
        if (Arrays.asList(this.env.getActiveProfiles()).contains("develop")
                    || Arrays.asList(this.env.getActiveProfiles()).contains("sandbox")) {
            bankInitService.uploadTestData();
        }
    }

    // enabled when mock-smtp maven profile is active
    @Bean
    @ConditionalOnClass(name = "org.subethamail.smtp.server.SMTPServer")
    MockSmtpServer mockSmtpServer() {
        return new MockSmtpServer();
    }
}
