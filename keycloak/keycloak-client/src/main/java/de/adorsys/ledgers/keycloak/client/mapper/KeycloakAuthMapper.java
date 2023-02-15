/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.keycloak.client.mapper;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface KeycloakAuthMapper {

    @Mapping(target = "act", ignore = true)
    @Mapping(target = "scaId", ignore = true)
    @Mapping(target = "consent", ignore = true)
    @Mapping(target = "authorisationId", ignore = true)
    @Mapping(target = "iat", source = "source.token.issuedAt")
    @Mapping(target = "role", expression = "java(getLedgersUserRoles(source.getToken()))")
    @Mapping(target = "sub", source = "source.token.subject")
    @Mapping(target = "scopes", source = "source.token.scope")
    @Mapping(target = "login", source = "source.token.name")
    @Mapping(target = "exp", source = "source.token.exp")
    @Mapping(target = "jti", source = "source.token.id")
    @Mapping(target = "accessToken", source = "source.tokenString")
    @Mapping(target = "tokenUsage", expression = "java(de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO.DIRECT_ACCESS)")
//TODO This is a stub!!!
    AccessTokenTO toAccessToken(RefreshableKeycloakSecurityContext source);

    @Mapping(target = "accessTokenObject", ignore = true)
    @Mapping(target = "scopes", source = "source.scope")
    @Mapping(target = "access_token", source = "token")
    @Mapping(target = "expires_in", source = "expiresIn")
    @Mapping(target = "refresh_token", source = "refreshToken")
    @Mapping(target = "token_type", source = "tokenType")
    BearerTokenTO toBearerTokenTO(AccessTokenResponse source);

    default Set<String> toScopes(String scope) {
        return Optional.ofNullable(scope)
                       .map(s -> new HashSet<>(Arrays.asList(s.split(" "))))
                       .orElse(new HashSet<>());
    }

    default BearerTokenTO toBearer(AccessToken source, String token) {
        AccessTokenTO to = toTokenTO(source, token);
        long ttl = (to.getExp().getTime() - new Date().getTime()) / DateUtils.MILLIS_PER_SECOND;
        return new BearerTokenTO(token, "Bearer", (int) ttl, null, to, to.getScopes());
    }

    @Mapping(target = "scaId", ignore = true)
    @Mapping(target = "consent", ignore = true)
    @Mapping(target = "authorisationId", ignore = true)
    @Mapping(target = "act", ignore = true)
    @Mapping(target = "exp", source = "source.exp")
    @Mapping(target = "jti", source = "source.id")
    @Mapping(target = "sub", source = "source.subject")
    @Mapping(target = "iat", source = "source.iat")
    @Mapping(target = "accessToken", source = "token")
    @Mapping(target = "scopes", source = "source.scope")
    @Mapping(target = "role", source = "source")
    @Mapping(target = "login", source = "source.name")
    @Mapping(target = "tokenUsage", expression = "java(de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO.DIRECT_ACCESS)")
    AccessTokenTO toTokenTO(AccessToken source, String token);

    default Date toDate(long source) {
        return new Date(source * DateUtils.MILLIS_PER_SECOND);
    }

    default UserRoleTO getLedgersUserRoles(AccessToken token) {
        Set<String> tokenizedRoles = Optional.ofNullable(token.getRealmAccess())
                                             .map(AccessToken.Access::getRoles)
                                             .orElseGet(Collections::emptySet);
        Collection<UserRoleTO> roles = CollectionUtils.intersection(
                tokenizedRoles
                        .stream()
                        .map(UserRoleTO::getByValue)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()), Arrays.asList(UserRoleTO.values())
        );

        return roles.isEmpty()
                       ? null
                       : UserRoleTO.getByValue(roles.iterator().next().toString()).orElse(null);

    }
}
