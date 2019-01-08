package de.adorsys.ledgers.middleware.rest.security;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;

public class MiddlewareAuthentication extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = -778888356552035882L;

    public MiddlewareAuthentication(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public MiddlewareAuthentication(Object principal, BearerTokenTO credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
    
    public BearerTokenTO getBearerToken() {
    	return (BearerTokenTO) getCredentials();
    }
    
    public boolean checkAccountInfoAccess(String iban) {
    	BearerTokenTO bearerToken = getBearerToken();
    	if(bearerToken==null) {
    		return false;
    	}
    	
    	AccessTokenTO token = bearerToken.getAccessTokenObject();
    	// Staff always have account access
    	if(UserRoleTO.STAFF == token.getRole() || UserRoleTO.SYSTEM == token.getRole()) {
    		return true;
    	}

    	// Customer must have explicit permission
    	if(UserRoleTO.CUSTOMER == token.getRole()) {
	    	return token.getAccountAccesses()!=null && token.getAccountAccesses().stream()
	    		.filter(a -> equalsIgnoreCase(iban, a.getIban()))
	    		.findAny().isPresent();
    	}
    	
    	return false;
    }

    public boolean checkPaymentInitAccess(String iban) {
    	AccessTokenTO token = getBearerToken().getAccessTokenObject();
    	// Customer must have explicit permission
    	if(UserRoleTO.CUSTOMER == token.getRole()) {
	    	return token.getAccountAccesses()!=null && token.getAccountAccesses().stream()
	    		.filter(a -> paymentAccess(a, iban))
	    		.findAny().isPresent();
    	}
    	return false;
    }
    
    private static boolean paymentAccess(AccountAccessTO a, String iban) {
    	return equalsIgnoreCase(iban, a.getIban()) && 
			(
				AccessTypeTO.OWNER.equals(a.getAccessType()) || 
				AccessTypeTO.DISPOSE.equals(a.getAccessType())
			);
    }

	public boolean checkTokenUsage(String usageType) {
		return getBearerToken().getAccessTokenObject().getTokenUsage()!=null &&
				getBearerToken().getAccessTokenObject().getTokenUsage().name().equals(usageType);
	}

	public boolean checkLoginToken(String scaId, String authorizationId) {
		return checkTokenUsage(TokenUsageTO.LOGIN.name()) 
				&&
				scaId.equals(getBearerToken().getAccessTokenObject().getScaId())
				&&
				authorizationId.equals(getBearerToken().getAccessTokenObject().getAuthorisationId());
	}
}
