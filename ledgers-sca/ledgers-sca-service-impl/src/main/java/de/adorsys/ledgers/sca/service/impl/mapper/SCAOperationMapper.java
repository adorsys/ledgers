package de.adorsys.ledgers.sca.service.impl.mapper;

import org.mapstruct.Mapper;

import de.adorsys.ledgers.sca.db.domain.AuthCodeStatus;
import de.adorsys.ledgers.sca.db.domain.OpType;
import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.domain.AuthCodeStatusBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;

@Mapper(componentModel = "spring")
public interface SCAOperationMapper {

    SCAOperationBO toBO(SCAOperationEntity entity);

    default AuthCodeStatusBO toBO(AuthCodeStatus entity){
        return AuthCodeStatusBO.valueOf(entity.name());
    }

    default AuthCodeStatus toPO(AuthCodeStatusBO bo){
        return AuthCodeStatus.valueOf(bo.name());
    }

    default OpType toPO(OpTypeBO bo) {
        return OpType.valueOf(bo.name());
    }

    default OpTypeBO toBO(OpType entity) {
        return OpTypeBO.valueOf(entity.name());
    }
}
