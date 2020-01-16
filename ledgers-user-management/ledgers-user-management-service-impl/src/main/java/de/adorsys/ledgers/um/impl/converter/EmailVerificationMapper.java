package de.adorsys.ledgers.um.impl.converter;

import de.adorsys.ledgers.um.api.domain.EmailVerificationBO;
import de.adorsys.ledgers.um.api.domain.EmailVerificationStatusBO;
import de.adorsys.ledgers.um.db.domain.EmailVerificationEntity;
import de.adorsys.ledgers.um.db.domain.EmailVerificationStatus;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface EmailVerificationMapper {

    EmailVerificationEntity toEmailVerificationEntity(EmailVerificationBO emailVerificationBO);

    EmailVerificationBO toEmailVerificationBO(EmailVerificationEntity entity);

    EmailVerificationStatus toEmailVerificationStatus(EmailVerificationStatusBO statusBO);
}