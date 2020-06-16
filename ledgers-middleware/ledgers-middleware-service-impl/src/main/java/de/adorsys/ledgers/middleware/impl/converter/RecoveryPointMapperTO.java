package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.general.RecoveryPointTO;
import de.adorsys.ledgers.sca.domain.RecoveryPointBO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecoveryPointMapperTO {
    RecoveryPointTO toTO(RecoveryPointBO source);

    List<RecoveryPointTO> toTOs(List<RecoveryPointBO> source);

    RecoveryPointBO toBO(RecoveryPointTO source);
}
