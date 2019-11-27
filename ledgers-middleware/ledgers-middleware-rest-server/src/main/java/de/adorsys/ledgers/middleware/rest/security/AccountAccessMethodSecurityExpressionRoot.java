package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;

import java.util.EnumSet;
import java.util.List;

import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.*;

public class AccountAccessMethodSecurityExpressionRoot extends SecurityExpressionAdapter {

    public AccountAccessMethodSecurityExpressionRoot(Authentication authentication, MiddlewareAccountManagementService accountService, MiddlewarePaymentService paymentService) {
        super(authentication, accountService, paymentService);
    }

    public boolean accountInfoById(String id) {
        // Load iban
        String iban = accountService.iban(id);
        return checkAccountInfoAccess(iban);
    }

    public boolean accountInfoByIban(String iban) {
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
        String iban = paymentService.iban(paymentId);
        return checkPaymentInitAccess(iban);
    }

    public boolean paymentInfoById(String paymentId) {
        // load iban
        String iban = paymentService.iban(paymentId);
        return checkAccountInfoAccess(iban) || checkPaymentInitAccess(iban);
    }

    private boolean checkPaymentInitAccess(String iban) {
        AccessTokenTO token = getAccessTokenTO();
        // Customer must have explicit permission
        if (EnumSet.of(CUSTOMER, STAFF).contains(token.getRole())) {
            return getAccountAccesses(token.getSub()).stream()
                           .anyMatch(a -> a.hasPaymentAccess(iban));
        }
        return SYSTEM == token.getRole();
    }

    private List<AccountAccessTO> getAccountAccesses(String userId) {
        return accountService.getAccountAccesses(userId);
    }

    private boolean checkAccountInfoAccess(String iban) {
        if (StringUtils.isBlank(iban)) {
            return false;
        }
        AccessTokenTO token = getAccessTokenTO();

        // System always have account access
        if (SYSTEM == token.getRole()) {
            return true;
        }
        // Customer and Staff must have explicit permission
        if (EnumSet.of(CUSTOMER, STAFF).contains(token.getRole())) {
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
}
