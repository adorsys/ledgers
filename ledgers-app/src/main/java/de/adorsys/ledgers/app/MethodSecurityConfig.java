package de.adorsys.ledgers.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareService;
import de.adorsys.ledgers.middleware.rest.security.AccountAccessMethodSecurityExpressionHandler;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
	@Autowired
    private MiddlewareAccountManagementService middlewareAccountService;
	@Autowired
	private MiddlewareService middlewareService;

	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		return 	new AccountAccessMethodSecurityExpressionHandler(middlewareAccountService, middlewareService);
	}
}
