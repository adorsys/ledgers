/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import org.mapstruct.Mapper;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.um.api.domain.AccessTokenBO;

@Mapper(componentModel = "spring")
public interface AccessTokenMapper {

	AccessTokenBO toAccessTokenBO(AccessTokenTO token);
    
	AccessTokenTO toAccessTokenTO(AccessTokenBO token);
}
