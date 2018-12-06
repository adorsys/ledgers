package de.adorsys.ledgers.middleware.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class BearerTokenAuthentication extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = -778888356552035882L;
    private final String token;

    public BearerTokenAuthentication(Object principal, Object credentials, String token) {
        super(principal, credentials);
        this.token = token;
    }

    public BearerTokenAuthentication(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String token) {
        super(principal, credentials, authorities);
        this.token = token;
    }

    // security issue if you export the token
    @JsonIgnore
    public String getToken() {
        return token;
    }
}
