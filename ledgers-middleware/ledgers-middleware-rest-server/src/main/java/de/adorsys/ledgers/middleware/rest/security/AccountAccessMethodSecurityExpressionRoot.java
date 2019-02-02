package de.adorsys.ledgers.middleware.rest.security;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import de.adorsys.ledgers.middleware.api.domain.um.AisAccountAccessInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;

public class AccountAccessMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations{
    private final MiddlewareAccountManagementService middlewareAccountService;
	private final MiddlewarePaymentService middlewareService;
	
	private Object filterObject;
	private Object returnObject;
	private Object target;


	
	public AccountAccessMethodSecurityExpressionRoot(Authentication authentication,
			MiddlewareAccountManagementService middlewareAccountService, MiddlewarePaymentService middlewareService) {
		super(authentication);
		this.middlewareAccountService = middlewareAccountService;
		this.middlewareService = middlewareService;
	}

	public boolean paymentInitByIban(String iban) {
		MiddlewareAuthentication m = (MiddlewareAuthentication) getAuthentication();
		return m.checkPaymentInitAccess(iban);
	}
	
	public boolean paymentInit(Object payment) {
		// Either the payment is directly available or wrapped
		Map<String, ?> map = (Map<String, ?>) payment;
		if(map.size()==1) {
			map = (Map<String, ?>) map.values().iterator().next();
		}
		Map<String, ?> debtorAccount = (Map<String, ?> ) map.get("debtorAccount");
		String iban = (String) debtorAccount.get("iban");
		MiddlewareAuthentication m = (MiddlewareAuthentication) getAuthentication();
		return m.checkPaymentInitAccess(iban);
	}


	public boolean paymentInitById(String paymentId) {
		// load iban
		String iban = middlewareService.iban(paymentId);
		return paymentInitByIban(iban);
	}

	public boolean paymentInfoById(String paymentId) {
		// load iban
		String iban = middlewareService.iban(paymentId);
		MiddlewareAuthentication m = (MiddlewareAuthentication) getAuthentication();
		return m.checkAccountInfoAccess(iban) || paymentInitByIban(iban);
	}
	
	public boolean accountInfoByIban(String iban) {
		MiddlewareAuthentication m = (MiddlewareAuthentication) getAuthentication();
		return m.checkAccountInfoAccess(iban);
	}

	public boolean accountInfoById(String id) {
		// Load iban
		String iban = middlewareAccountService.iban(id);
		return iban!=null && accountInfoByIban(iban);
	}
	
	public boolean accountInfoFor(AisConsentTO consent) {
		AisAccountAccessInfoTO access = consent.getAccess();
		return access!=null &&
				accountInfoByIbanList(access.getAccounts()) &&
				accountInfoByIbanList(access.getTransactions()) && 
				accountInfoByIbanList(access.getBalances());
	}
	
	private boolean accountInfoByIbanList(List<String> ibanList) {
		if(ibanList==null || ibanList.isEmpty()) {
			return true;
		}
		for (String iban : ibanList) {
			if(!accountInfoByIban(iban)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean tokenUsage(String usageType) {
		MiddlewareAuthentication m = (MiddlewareAuthentication) getAuthentication();
		return m.checkTokenUsage(usageType);
	}

	public boolean loginToken(String scaId, String authorizationId) {
		MiddlewareAuthentication m = (MiddlewareAuthentication) getAuthentication();
		return m.checkLoginToken(scaId, authorizationId);
	}
	
	@Override
	public void setFilterObject(Object filterObject) {
		this.filterObject = filterObject;
	}

	@Override
	public Object getFilterObject() {
		return filterObject;
	}

	@Override
	public void setReturnObject(Object returnObject) {
		this.returnObject = returnObject;
	}

	@Override
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

	@Override
	public Object getThis() {
		return target;
	}
}
