package de.adorsys.ledgers.middleware.security;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareService;

public class AccountAccessPermissionEvaluator implements PermissionEvaluator {

	@Autowired
    private MiddlewareAccountManagementService middlewareAccountService;
	@Autowired
	private MiddlewareService middlewareService;

    @Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		return false;
	}
	
	public boolean paymentInitByIban(Authentication authentication, String iban) {
		MiddlewareAuthentication m = (MiddlewareAuthentication) authentication;
		return m.checkPaymentInitAccess(iban);
	}

	public boolean paymentInitById(Authentication authentication, String paymentId) {
		// load iban
		String iban = middlewareService.iban(paymentId);
		return paymentInitByIban(authentication, iban);
	}
	
	public boolean accountInfoByIban(Authentication authentication, String iban) {
		MiddlewareAuthentication m = (MiddlewareAuthentication) authentication;
		return m.checkAccountInfoAccess(iban);
	}

	public boolean accountInfoById(Authentication authentication, String id) {
		// Load iban
		String iban = middlewareAccountService.iban(id);
		if(iban==null) {
			return false;
		}
		return accountInfoByIban(authentication, iban);
	}
}
