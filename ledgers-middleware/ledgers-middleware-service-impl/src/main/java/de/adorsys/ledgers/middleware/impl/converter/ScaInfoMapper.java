/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.um.api.domain.ScaInfoBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScaInfoMapper {

    ScaInfoBO toScaInfoBO(ScaInfoTO scaInfoTO);

    ScaInfoTO toScaInfoTO(ScaInfoBO scaInfoBO);

}
