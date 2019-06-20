package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import org.mapstruct.factory.Mappers;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class MiddlewareAuthentication extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = -778888356552035882L;
    private final UserService userService;
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    public MiddlewareAuthentication(Object principal, BearerTokenTO credentials, Collection<? extends GrantedAuthority> authorities, UserService userService) {
        super(principal, credentials, authorities);
        this.userService = userService;
    }

    public BearerTokenTO getBearerToken() {
        return (BearerTokenTO) getCredentials();
    }

    public boolean checkAccountInfoAccess(String iban) {
        BearerTokenTO bearerToken = getBearerToken();
        if (bearerToken == null) {
            return false;
        }

        AccessTokenTO token = bearerToken.getAccessTokenObject();
        // Staff always have account access
        if (UserRoleTO.STAFF == token.getRole() || UserRoleTO.SYSTEM == token.getRole()) {
            return true;
        }

        // Customer must have explicit permission
        if (UserRoleTO.CUSTOMER == token.getRole()) {
            boolean access = getAccountAccesses(token.getSub())
                                     .stream()
                                     .anyMatch(a -> equalsIgnoreCase(iban, a.getIban()));
            return access || checkCosentAccess(token, iban);
        }

        return false;
    }

    private boolean checkCosentAccess(AccessTokenTO token, String iban) {
        return validConsent(token.getConsent()) && checkConsentAccess(iban, token.getConsent().getAccess());
    }

    private boolean validConsent(AisConsentTO consent) {
        return consent != null &&
                       (consent.getValidUntil() == null || consent.getValidUntil().isAfter(LocalDate.now()));
    }

    private boolean checkConsentAccess(String iban, AisAccountAccessInfoTO access) {
        return access != null &&
                       (
                               access.getAvailableAccounts() != null ||
                                       access.getAllPsd2() != null ||
                                       access.getAccounts() != null && access.getAccounts().contains(iban) ||
                                       access.getBalances() != null && access.getBalances().contains(iban) ||
                                       access.getTransactions() != null && access.getTransactions().contains(iban)
                       );
    }

    public boolean checkPaymentInitAccess(String iban) {
        AccessTokenTO token = getBearerToken().getAccessTokenObject();
        // Customer must have explicit permission
        if (UserRoleTO.CUSTOMER == token.getRole()) {
            return getAccountAccesses(token.getSub()).stream()
                           .anyMatch(a -> paymentAccess(a, iban));
        }
        return UserRoleTO.STAFF == token.getRole();
    }

    private static boolean paymentAccess(AccountAccessTO a, String iban) {
        return equalsIgnoreCase(iban, a.getIban()) &&
                       (
                               AccessTypeTO.OWNER.equals(a.getAccessType()) ||
                                       AccessTypeTO.DISPOSE.equals(a.getAccessType())
                       );
    }

    public boolean checkTokenUsage(String usageType) {
        return getBearerToken().getAccessTokenObject().getTokenUsage() != null &&
                       getBearerToken().getAccessTokenObject().getTokenUsage().name().equals(usageType);
    }

    public boolean checkLoginToken(String scaId, String authorizationId) {
        return checkTokenUsage(TokenUsageTO.LOGIN.name())
                       &&
                       scaId.equals(getBearerToken().getAccessTokenObject().getScaId())
                       &&
                       authorizationId.equals(getBearerToken().getAccessTokenObject().getAuthorisationId());
    }

    private List<AccountAccessTO> getAccountAccesses(String userId) {
        try {
            return userMapper.toAccountAccessListTO(userService.findById(userId).getAccountAccesses());
        } catch (UserNotFoundException e) {
            return Collections.emptyList();
        }
    }
}
