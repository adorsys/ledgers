package de.adorsys.ledgers.um.impl.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.db.domain.AisConsentEntity;

@Mapper(componentModel = "spring")
public interface AisConsentMapper {
	
	@Mappings({
		@Mapping(target="access.availableAccounts", source="availableAccounts"),
		@Mapping(target="access.allPsd2", source="allPsd2"),
		@Mapping(target="access.accounts", source="accounts"),
		@Mapping(target="access.balances", source="balances"),
		@Mapping(target="access.transactions", source="transactions")
	})
	AisConsentBO toAisConsentBO(AisConsentEntity consent);

	@Mappings({
		@Mapping(source="access.availableAccounts", target="availableAccounts"),
		@Mapping(source="access.allPsd2", target="allPsd2"),
		@Mapping(source="access.accounts", target="accounts"),
		@Mapping(source="access.balances", target="balances"),
		@Mapping(source="access.transactions",target="transactions")
	})
	AisConsentEntity toAisConsentPO(AisConsentBO consent);
}
