/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.server.auth;

import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    // This is required for roles processing, because by default Spring boot uses scopes instead of roles.
    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        List<String> tokenizedRoles = (ArrayList) source.getClaimAsMap("realm_access").get("roles");

        List<SimpleGrantedAuthority> authorities = tokenizedRoles.stream()
                                                           .map(UserRoleTO::getByValue)
                                                           .filter(Optional::isPresent)
                                                           .map(Optional::get)
                                                           .map(a -> "ROLE_" + a)
                                                           .map(SimpleGrantedAuthority::new)
                                                           .toList();

        return new JwtAuthenticationToken(source, authorities, "sub");
    }
}
