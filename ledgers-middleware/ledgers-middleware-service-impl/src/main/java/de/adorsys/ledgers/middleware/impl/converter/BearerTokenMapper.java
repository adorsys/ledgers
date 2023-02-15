/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import org.mapstruct.Mapper;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;

@Mapper(componentModel = "spring")
public interface BearerTokenMapper {

	BearerTokenBO toBearerTokenBO(BearerTokenTO token);
    
	BearerTokenTO toBearerTokenTO(BearerTokenBO token);
}
