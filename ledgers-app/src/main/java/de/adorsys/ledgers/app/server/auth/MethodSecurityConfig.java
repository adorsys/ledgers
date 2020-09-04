package de.adorsys.ledgers.app.server.auth;

import de.adorsys.ledgers.keycloak.client.mapper.KeycloakAuthMapper;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.security.AccountAccessMethodSecurityExpressionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    private final MiddlewareAccountManagementService middlewareAccountService;
    private final MiddlewarePaymentService middlewareService;
    private final KeycloakAuthMapper authMapper;
    private final MiddlewareUserManagementService userManagementService;

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return new AccountAccessMethodSecurityExpressionHandler(middlewareAccountService, middlewareService, userManagementService, authMapper);
    }
}
