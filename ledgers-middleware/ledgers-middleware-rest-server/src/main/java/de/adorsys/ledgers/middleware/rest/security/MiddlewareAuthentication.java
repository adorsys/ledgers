package de.adorsys.ledgers.middleware.rest.security;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;

public class MiddlewareAuthentication extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = -778888356552035882L;
    private final String token;

    public MiddlewareAuthentication(Object principal, Object credentials, String token) {
        super(principal, credentials);
        this.token = token;
    }

    public MiddlewareAuthentication(Object principal, AccessTokenTO credentials, Collection<? extends GrantedAuthority> authorities, String token) {
        super(principal, credentials, authorities);
        this.token = token;
    }

    // security issue if you export the token
    @JsonIgnore
    public String getToken() {
        return token;
    }
    
    private AccessTokenTO getAccessTokenTO() {
    	return (AccessTokenTO) getCredentials();
    }
    
    public boolean checkAccountInfoAccess(String iban) {
    	AccessTokenTO token = getAccessTokenTO();
    	// Staff always have account access
    	if(UserRoleTO.STAFF == token.getRole() || UserRoleTO.SYSTEM == token.getRole()) {
    		return true;
    	}

    	// Customer must have explicit permission
    	if(UserRoleTO.CUSTOMER == token.getRole()) {
	    	List<AccountAccessTO> accountAccesses = token.getAccountAccesses();
	    	return accountAccesses.stream()
	    		.filter(a -> equalsIgnoreCase(iban, a.getIban()))
	    		.findAny().isPresent();
    	}
    	
    	return false;
    }

    public boolean checkPaymentInitAccess(String iban) {
    	AccessTokenTO token = getAccessTokenTO();
    	// Customer must have explicit permission
    	if(UserRoleTO.CUSTOMER == token.getRole()) {
	    	List<AccountAccessTO> accountAccesses = token.getAccountAccesses();
	    	return accountAccesses.stream()
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
}
