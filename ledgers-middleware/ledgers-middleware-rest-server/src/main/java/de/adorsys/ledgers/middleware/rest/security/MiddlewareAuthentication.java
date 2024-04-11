/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

//TODO REMOVE ME!
public class MiddlewareAuthentication extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = -778888356552035882L;

    public MiddlewareAuthentication(Object principal, BearerTokenTO credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public BearerTokenTO getBearerToken() {
        return (BearerTokenTO) getCredentials();
    }
}
