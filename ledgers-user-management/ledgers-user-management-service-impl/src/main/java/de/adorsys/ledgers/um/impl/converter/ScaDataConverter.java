package de.adorsys.ledgers.um.impl.converter;

import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.db.domain.AccessType;
import de.adorsys.ledgers.um.db.domain.ScaMethodType;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import org.mapstruct.Mapper;

import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;

@Mapper(componentModel = "spring")
public interface ScaDataConverter {

    ScaUserDataBO toScaDataBO(ScaUserDataEntity user);

    ScaUserDataEntity toScaDataPO(ScaUserDataBO user);

    default ScaMethodTypeBO toScanMethodTypeBo(ScaMethodType entity) {
        return ScaMethodTypeBO.valueOf(entity.name());
    }

    default ScaMethodType toScanMethodTypeEntity(ScaMethodTypeBO bo) {
        return ScaMethodType.valueOf(bo.name());
    }

    default AccessType toAccessType(AccessTypeBO bo) {
        return AccessType.valueOf(bo.name());
    }

    default AccessTypeBO toAccessType(AccessType entity) {
        return AccessTypeBO.valueOf(entity.name());
    }
}
