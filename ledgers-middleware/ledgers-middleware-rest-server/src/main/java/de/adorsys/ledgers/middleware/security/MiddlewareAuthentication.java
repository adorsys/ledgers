package de.adorsys.ledgers.middleware.security;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;

public class MiddlewareAuthentication extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = -778888356552035882L;
    private final String token;

    public MiddlewareAuthentication(Object principal, Object credentials, String token) {
        super(principal, credentials);
        this.token = token;
    }

    public MiddlewareAuthentication(Object principal, UserTO credentials, Collection<? extends GrantedAuthority> authorities, String token) {
        super(principal, credentials, authorities);
        this.token = token;
    }

    // security issue if you export the token
    @JsonIgnore
    public String getToken() {
        return token;
    }
    
    private UserTO getUserTO() {
    	return (UserTO) getCredentials();
    }
    
    public boolean checkAccountInfoAccess(String iban) {
    	UserTO userTO = getUserTO();
    	// Staff allways have account access
    	if(userTO.getUserRoles().contains(UserRoleTO.STAFF)) {
    		return true;
    	}

    	// Customer must have explicit permission
    	if(userTO.getUserRoles().contains(UserRoleTO.CUSTOMER)) {
	    	List<AccountAccessTO> accountAccesses = userTO.getAccountAccesses();
	    	return accountAccesses.stream()
	    		.filter(a -> equalsIgnoreCase(iban, a.getIban()))
	    		.findAny().isPresent();
    	}
    	
    	return false;
    }

    public boolean checkPaymentInitAccess(String iban) {
    	UserTO userTO = getUserTO();
    	// Customer must have explicit permission
    	if(userTO.getUserRoles().contains(UserRoleTO.CUSTOMER)) {
	    	List<AccountAccessTO> accountAccesses = userTO.getAccountAccesses();
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
