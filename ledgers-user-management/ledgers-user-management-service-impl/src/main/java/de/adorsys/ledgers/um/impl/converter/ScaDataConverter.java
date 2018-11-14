package de.adorsys.ledgers.um.impl.converter;

import org.mapstruct.Mapper;

import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.db.domain.AccessType;
import de.adorsys.ledgers.um.db.domain.ScaMethodTypeEntity;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;

@Mapper(componentModel = "spring")
public interface ScaDataConverter {

    ScaUserDataBO toScaDataBO(ScaUserDataEntity user);

    ScaUserDataEntity toScaDataPO(ScaUserDataBO user);

    default ScaMethodTypeBO toScanMethodTypeBo(ScaMethodTypeEntity entity){
        return ScaMethodTypeBO.valueOf(entity.name());
    }

    default ScaMethodTypeEntity toScanMethodTypeEntity(ScaMethodTypeBO bo){
        return ScaMethodTypeEntity.valueOf(bo.name());
    }

    default AccessType toAccessType(AccessTypeBO bo) {
        return AccessType.valueOf(bo.name());
    }

    default AccessTypeBO toAccessType(AccessType entity) {
        return AccessTypeBO.valueOf(entity.name());
    }
}
