package de.adorsys.ledgers.mockbank.simple.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.rest.security.AccountAccessMethodSecurityExpressionHandler;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
	@Autowired
    private MiddlewareAccountManagementService middlewareAccountService;
	@Autowired
	private MiddlewarePaymentService middlewareService;

	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		AccountAccessMethodSecurityExpressionHandler expressionHandler = 
				new AccountAccessMethodSecurityExpressionHandler(middlewareAccountService, middlewareService);
		return expressionHandler;
	}
}
