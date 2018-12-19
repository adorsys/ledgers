package de.adorsys.ledgers.middleware.rest.security;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareService;

public class AccountAccessMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

	private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
    private final MiddlewareAccountManagementService middlewareAccountService;
	private final MiddlewareService middlewareService;

	public AccountAccessMethodSecurityExpressionHandler(MiddlewareAccountManagementService middlewareAccountService,
			MiddlewareService middlewareService) {
		super();
		this.middlewareAccountService = middlewareAccountService;
		this.middlewareService = middlewareService;
	}


	@Override
	protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication,
			MethodInvocation invocation) {
		AccountAccessMethodSecurityExpressionRoot root = new AccountAccessMethodSecurityExpressionRoot(authentication, middlewareAccountService, middlewareService);
		root.setPermissionEvaluator(getPermissionEvaluator());
		root.setTrustResolver(this.trustResolver);
		root.setRoleHierarchy(getRoleHierarchy());
		return root;
	}
}
