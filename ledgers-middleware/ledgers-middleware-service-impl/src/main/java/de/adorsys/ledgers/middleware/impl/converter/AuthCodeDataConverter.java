/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.sca.AuthCodeDataTO;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthCodeDataConverter {

    AuthCodeDataBO toAuthCodeDataBO(AuthCodeDataTO to);
}
