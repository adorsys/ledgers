package de.adorsys.ledgers.um.impl.converter;

import org.mapstruct.Mapper;

import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.db.domain.AisConsentEntity;

@Mapper(componentModel = "spring")
public interface AisConsentMapper {
	
	AisConsentBO toAisConsentBO(AisConsentEntity consent);

	AisConsentEntity toAisConsentPO(AisConsentBO consent);
}
