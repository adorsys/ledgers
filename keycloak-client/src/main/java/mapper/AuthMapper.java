package mapper;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import org.apache.commons.collections4.CollectionUtils;
import org.keycloak.representations.AccessToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "sub", source = "subject")
    @Mapping(target = "tokenUsage", expression = "java(de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO.DIRECT_ACCESS)")
    @Mapping(target = "login", source = "preferredUsername")
    @Mapping(target = "role", expression = "java(getLedgersUserRoles(source))")
    @Mapping(target = "jti", source = "id")
    AccessTokenTO toAccessToken(AccessToken source);

    default Date toDate(long source) {
        return new Date(source);
    }

    default UserRoleTO getLedgersUserRoles(AccessToken token) {

        Collection<UserRoleBO> roles = CollectionUtils.intersection(
                token.getRealmAccess().getRoles()
                        .stream()
                        .map(UserRoleBO::getByValue)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()), Arrays.asList(UserRoleBO.values()));

        return roles.isEmpty()
                       ? null
                       : UserRoleTO.getByValue(roles.iterator().next().toString()).orElse(null);


    }
}
