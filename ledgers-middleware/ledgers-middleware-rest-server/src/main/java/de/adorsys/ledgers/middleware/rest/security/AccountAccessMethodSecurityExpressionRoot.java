package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class AccountAccessMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
    private final MiddlewareAccountManagementService middlewareAccountService;
    private final MiddlewarePaymentService middlewarePaymentService;

    private Object filterObject;
    private Object returnObject;
    private Object target;


    public AccountAccessMethodSecurityExpressionRoot(Authentication authentication,
                                                     MiddlewareAccountManagementService middlewareAccountService, MiddlewarePaymentService middlewarePaymentService) {
        super(authentication);
        this.middlewareAccountService = middlewareAccountService;
        this.middlewarePaymentService = middlewarePaymentService;
    }

    public boolean paymentInit(Object payment) {
        // Either the payment is directly available or wrapped
        Map<String, ?> map = (Map<String, ?>) payment;
        if (map.size() == 1) {
            map = (Map<String, ?>) map.values().iterator().next();
        }
        Map<String, ?> debtorAccount = (Map<String, ?>) map.get("debtorAccount");
        String iban = (String) debtorAccount.get("iban");
        return checkPaymentInitAccess(iban);
    }

    public boolean accountInfoById(String id) {
        // Load iban
        String iban = middlewareAccountService.iban(id);
        return checkAccountInfoAccess(iban);
    }

    public boolean accountInfoFor(AisConsentTO consent) {
        AisAccountAccessInfoTO access = consent.getAccess();
        return access != null &&
                       accountInfoByIbanList(access.getAccounts()) &&
                       accountInfoByIbanList(access.getTransactions()) &&
                       accountInfoByIbanList(access.getBalances());
    }

    public boolean tokenUsage(String usageType) {
        return checkTokenUsage(usageType);
    }

    public boolean loginToken(String scaId, String authorizationId) {
        AccessTokenTO token = getAccessTokenTO();
        return checkTokenUsage(TokenUsageTO.LOGIN.name())
                       && scaId.equals(token.getScaId())
                       && authorizationId.equals(token.getAuthorisationId());
    }

    public boolean paymentInitById(String paymentId) {
        // load iban
        String iban = middlewarePaymentService.iban(paymentId);
        return checkPaymentInitAccess(iban);
    }

    public boolean paymentInfoById(String paymentId) {
        // load iban
        String iban = middlewarePaymentService.iban(paymentId);
        return checkAccountInfoAccess(iban) || checkPaymentInitAccess(iban);
    }

    private boolean checkPaymentInitAccess(String iban) {
        AccessTokenTO token = getAccessTokenTO();
        // Customer must have explicit permission
        if (UserRoleTO.CUSTOMER == token.getRole()) {
            return getAccountAccesses(token.getSub()).stream()
                           .anyMatch(a -> a.hasPaymentAccess(iban));
        }
        return UserRoleTO.STAFF == token.getRole();
    }

    private List<AccountAccessTO> getAccountAccesses(String userId) {
        return middlewareAccountService.getAccountAccesses(userId);
    }

    private boolean checkAccountInfoAccess(String iban) {
        if (StringUtils.isBlank(iban)) {
            return false;
        }
        AccessTokenTO token = getAccessTokenTO();

        // Staff always have account access
        if (EnumSet.of(UserRoleTO.STAFF, UserRoleTO.SYSTEM).contains(token.getRole())) {
            return true;
        }
        // Customer must have explicit permission
        if (UserRoleTO.CUSTOMER == token.getRole()) {
            return getAccountAccesses(token.getSub()).stream()
                           .anyMatch(a -> a.hasIban(iban))
                           || checkConsentAccess(token, iban);
        }
        return false;
    }

    private boolean checkConsentAccess(AccessTokenTO token, String iban) {
        return token.hasValidConsent() && checkConsentAccess(iban, token.getConsent().getAccess());
    }

    private boolean checkConsentAccess(String iban, AisAccountAccessInfoTO access) {
        return access != null && access.hasIbanInAccess(iban);
    }

    private boolean accountInfoByIbanList(List<String> ibanList) {
        if (CollectionUtils.isEmpty(ibanList)) {
            return true;
        }
        for (String iban : ibanList) {
            if (!checkAccountInfoAccess(iban)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkTokenUsage(String usageType) {
        AccessTokenTO token = getAccessTokenTO();
        return token.getTokenUsage() != null &&
                       token.getTokenUsage().name().equals(usageType);
    }

    private AccessTokenTO getAccessTokenTO() {
        MiddlewareAuthentication authentication = (MiddlewareAuthentication) getAuthentication();
        return authentication.getBearerToken()
                       .getAccessTokenObject();
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
