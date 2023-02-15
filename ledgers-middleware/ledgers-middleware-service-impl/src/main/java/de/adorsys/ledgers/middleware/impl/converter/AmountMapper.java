/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.deposit.api.domain.AmountBO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AmountMapper {
    AmountBO toAmountBO(AmountTO amount);
}
