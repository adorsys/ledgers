/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.util.domain.CustomPageImpl;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface PageMapper {
    default <T> CustomPageImpl<T> toCustomPageImpl(Page<T> page) {
        return new CustomPageImpl<>(page.getNumber(), page.getSize(), page.getTotalPages(), page.getNumberOfElements(), page.getTotalElements(), page.hasPrevious(), page.isFirst(), page.hasNext(), page.isLast(), page.getContent());
    }
}
