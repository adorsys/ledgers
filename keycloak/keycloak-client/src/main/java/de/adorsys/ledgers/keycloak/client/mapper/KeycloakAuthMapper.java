/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.keycloak.client.mapper;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.keycloak.representations.AccessToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface KeycloakAuthMapper {

    default AccessTokenTO toAccessTokenFromJwt(Jwt source) {
        AccessTokenTO token = new AccessTokenTO();
        token.setIat(Date.from(source.getIssuedAt()));
        token.setRole(getLedgersUserRolesFromJwt(source));
        token.setSub(source.getClaimAsString("sub"));
        token.setScopes(new HashSet(Arrays.asList(source.getClaimAsString("scope").split(" "))));
        token.setLogin(source.getClaimAsString("name"));
        token.setExp(Date.from(source.getExpiresAt()));
        token.setJti(source.getClaimAsString("jti"));
        token.setAccessToken(source.getTokenValue());
        token.setTokenUsage(TokenUsageTO.DIRECT_ACCESS);

        return token;
    }

    default BearerTokenTO toBearerTokenFromJwt(Jwt source) {
        AccessTokenTO to = toAccessTokenFromJwt(source);
        long ttl = (to.getExp().getTime() - new Date().getTime()) / DateUtils.MILLIS_PER_SECOND;

        return new BearerTokenTO(source.getTokenValue(), "Bearer", (int) ttl, null, to, to.getScopes());
    }

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

    default UserRoleTO getLedgersUserRolesFromJwt(Jwt token) {
        List<String> tokenizedRoles = (ArrayList) token.getClaimAsMap("realm_access").get("roles");

        Collection<UserRoleTO> roles = CollectionUtils.intersection(
                tokenizedRoles
                        .stream()
                        .map(UserRoleTO::getByValue)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList(), Arrays.asList(UserRoleTO.values())
        );

        return roles.isEmpty()
                       ? null
                       : UserRoleTO.getByValue(roles.iterator().next().toString()).orElse(null);
    }

}
