/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.account.AdditionalAccountInformationTO;
import de.adorsys.ledgers.um.api.domain.AdditionalAccountInfoBO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdditionalAccountInformationMapper {
    AdditionalAccountInformationTO toAdditionalAccountInformationTO(AdditionalAccountInfoBO source);

    List<AdditionalAccountInformationTO> toAdditionalAccountInformationTOs(List<AdditionalAccountInfoBO> source);
}
