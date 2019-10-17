package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.oauth.OauthServerInfoTO;
import de.adorsys.ledgers.um.api.domain.oauth.OauthServerInfoBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OauthServerInfoMapper {
    OauthServerInfoBO toOauthServerInfoBO(OauthServerInfoTO source);

    OauthServerInfoTO toOauthServerInfoTO(OauthServerInfoBO source);
}
