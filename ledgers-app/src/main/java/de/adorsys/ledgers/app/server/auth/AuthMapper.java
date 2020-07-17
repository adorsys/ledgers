package de.adorsys.ledgers.app.server.auth;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import org.keycloak.representations.AccessToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Date;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "sub", source = "subject")
    @Mapping(target = "tokenUsage", defaultValue = "DIRECT_ACCESS")
    @Mapping(target = "login", source = "preferredUsername")
    AccessTokenTO toAccessToken(AccessToken source);

    default Date toDate(long source) {
        return new Date(source);
    }
}
