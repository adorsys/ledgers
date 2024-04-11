/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl.mapper;

import de.adorsys.ledgers.sca.db.domain.RecoveryPointEntity;
import de.adorsys.ledgers.sca.domain.RecoveryPointBO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecoveryPointMapper {

    RecoveryPointBO toBO(RecoveryPointEntity source);

    List<RecoveryPointBO> toBOs(List<RecoveryPointEntity> source);

    RecoveryPointEntity toEntity(RecoveryPointBO source);
}
