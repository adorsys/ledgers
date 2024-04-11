/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.keycloak.client.mapper.KeycloakAuthMapper;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareRedirectScaService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
    private final MiddlewareAccountManagementService middlewareAccountService;
    private final MiddlewarePaymentService middlewareService;
    private final MiddlewareUserManagementService userManagementService;
    private final KeycloakAuthMapper authMapper;
    private final MiddlewareRedirectScaService scaService;

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return new AccountAccessSecurityFilterHandler(middlewareAccountService, middlewareService, userManagementService, authMapper, scaService);
    }
}
