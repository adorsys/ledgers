package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.keycloak.client.mapper.KeycloakAuthMapper;
import de.adorsys.ledgers.middleware.api.domain.account.AccountIdentifierTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.springframework.security.core.Authentication;

import java.util.*;

import static de.adorsys.ledgers.middleware.api.domain.Constants.*;
import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.*;

public class AccountAccessMethodSecurityExpressionRoot extends SecurityExpressionAdapter {

    public AccountAccessMethodSecurityExpressionRoot(Authentication authentication, MiddlewareAccountManagementService accountService, MiddlewarePaymentService paymentService, KeycloakAuthMapper authMapper, MiddlewareUserManagementService userManagementService) {
        super(authentication, accountService, paymentService, userManagementService, authMapper);
    }

    public boolean hasFullAccessScope() {
        return hasAnyScope(SCOPE_FULL_ACCESS);
    }

    public boolean hasScaScope() {
        return hasAnyScope(SCOPE_SCA, SCOPE_PARTIAL_ACCESS, SCOPE_FULL_ACCESS);
    }

    public boolean hasPartialScope() {
        return hasAnyScope(SCOPE_PARTIAL_ACCESS, SCOPE_FULL_ACCESS);
    }

    private boolean hasAnyScope(String... scopes) {
        Set<String> scopesInToken = getScopes();
        return Arrays.stream(scopes)
                       .anyMatch(scopesInToken::contains);
    }

    private Set<String> getScopes() {
        RefreshableKeycloakSecurityContext credentials = (RefreshableKeycloakSecurityContext) authentication.getCredentials();
        return new HashSet<>(Arrays.asList(credentials.getToken()
                                                   .getScope()
                                                   .split(" ")));
    }

    public boolean accountInfoByIdentifier(AccountIdentifierTypeTO type, String accountIdentifier) {
        return type == AccountIdentifierTypeTO.IBAN
                       ? accountInfoByIban(accountIdentifier)
                       : accountInfoById(accountIdentifier);
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

    public boolean tokenUsages(String usageTypeFirst, String usageTypeSecond) {
        return checkTokenUsage(usageTypeFirst) || checkTokenUsage(usageTypeSecond);
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
            return getAccountAccesses(token.getLogin()).stream()
                           .anyMatch(a -> a.hasPaymentAccess(iban));
        }
        return SYSTEM == token.getRole();
    }

    private List<AccountAccessTO> getAccountAccesses(String login) {
        return userManagementService.findByUserLogin(login).getAccountAccesses();
    }

    private boolean checkAccountInfoAccess(String iban) { //TODO fix me or remove me!
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
            return getAccountAccesses(token.getLogin()).stream()
                           .anyMatch(a -> a.hasIban(iban));
        }
        return false;
    }

   /* private boolean checkConsentAccess(AccessTokenTO token, String iban) {

        return token.hasValidConsent() && checkConsentAccess(iban, token.getConsent().getAccess());
    }*/

   /* private boolean checkConsentAccess(String iban, AisAccountAccessInfoTO access) {
        return access != null && access.hasIbanInAccess(iban);
    }*/

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
        RefreshableKeycloakSecurityContext credentials = (RefreshableKeycloakSecurityContext) authentication.getCredentials();
        return authMapper.toAccessToken(credentials);
    }
}
