package de.adorsys.ledgers.middleware.impl.converter;

import org.mapstruct.Mapper;

import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;

@Mapper(componentModel = "spring")
public interface AisConsentMapper {
	
	AisConsentBO toAisConsentBO(AisConsentTO consent);

	AisConsentTO toAisConsentTO(AisConsentBO consent);
}
