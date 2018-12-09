package de.adorsys.ledgers.middleware.rest.security;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareService;

public class AccountAccessMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations{
    private final MiddlewareAccountManagementService middlewareAccountService;
	private final MiddlewareService middlewareService;
	
	private Object filterObject;
	private Object returnObject;
	private Object target;


	
	public AccountAccessMethodSecurityExpressionRoot(Authentication authentication,
			MiddlewareAccountManagementService middlewareAccountService, MiddlewareService middlewareService) {
		super(authentication);
		this.middlewareAccountService = middlewareAccountService;
		this.middlewareService = middlewareService;
	}

	public boolean paymentInitByIban(String iban) {
		MiddlewareAuthentication m = (MiddlewareAuthentication) getAuthentication();
		return m.checkPaymentInitAccess(iban);
	}
	
	public boolean paymentInit(Object payment) {
		Map<String, ?> map = (Map<String, ?>) payment;
		Map<String, ?> debtorAccount = (Map<String, ?>) map.get("debtorAccount");
		String iban = (String) debtorAccount.get("iban");
		MiddlewareAuthentication m = (MiddlewareAuthentication) getAuthentication();
		return m.checkPaymentInitAccess(iban);
	}


	public boolean paymentInitById(String paymentId) {
		// load iban
		String iban = middlewareService.iban(paymentId);
		return paymentInitByIban(iban);
	}
	
	public boolean accountInfoByIban(String iban) {
		MiddlewareAuthentication m = (MiddlewareAuthentication) getAuthentication();
		return m.checkAccountInfoAccess(iban);
	}

	public boolean accountInfoById(String id) {
		// Load iban
		String iban = middlewareAccountService.iban(id);
		if(iban==null) {
			return false;
		}
		return accountInfoByIban(iban);
	}

	public void setFilterObject(Object filterObject) {
		this.filterObject = filterObject;
	}

	public Object getFilterObject() {
		return filterObject;
	}

	public void setReturnObject(Object returnObject) {
		this.returnObject = returnObject;
	}

	public Object getReturnObject() {
		return returnObject;
	}

	/**
	 * Sets the "this" property for use in expressions. Typically this will be the "this"
	 * property of the {@code JoinPoint} representing the method invocation which is being
	 * protected.
	 *
	 * @param target the target object on which the method in is being invoked.
	 */
	void setThis(Object target) {
		this.target = target;
	}

	public Object getThis() {
		return target;
	}
}
