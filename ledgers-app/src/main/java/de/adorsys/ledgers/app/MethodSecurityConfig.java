package de.adorsys.ledgers.app;

import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.rest.security.AccountAccessMethodSecurityExpressionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
	private final MiddlewareAccountManagementService middlewareAccountService;
	private final MiddlewarePaymentService middlewareService;

	@Autowired
	public MethodSecurityConfig(MiddlewareAccountManagementService middlewareAccountService, MiddlewarePaymentService middlewareService) {
		this.middlewareAccountService = middlewareAccountService;
		this.middlewareService = middlewareService;
	}

	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		return 	new AccountAccessMethodSecurityExpressionHandler(middlewareAccountService, middlewareService);
	}
}
