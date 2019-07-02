package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

@RequiredArgsConstructor
public class AccountAccessMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

	private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
    private final MiddlewareAccountManagementService middlewareAccountService;
	private final MiddlewarePaymentService middlewareService;

	@Override
	protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, MethodInvocation invocation) {
		AccountAccessMethodSecurityExpressionRoot root = new AccountAccessMethodSecurityExpressionRoot(authentication, middlewareAccountService, middlewareService);
		root.setPermissionEvaluator(getPermissionEvaluator());
		root.setTrustResolver(this.trustResolver);
		root.setRoleHierarchy(getRoleHierarchy());
		return root;
	}
}
