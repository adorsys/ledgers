package de.adorsys.ledgers.um.impl.converter;

import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.db.domain.AisConsentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AisConsentMapper {

    @Mapping(target = "access.availableAccounts", source = "availableAccounts")
    @Mapping(target = "access.allPsd2", source = "allPsd2")
    @Mapping(target = "access.accounts", source = "accounts")
    @Mapping(target = "access.balances", source = "balances")
    @Mapping(target = "access.transactions", source = "transactions")
    AisConsentBO toAisConsentBO(AisConsentEntity consent);

    @Mapping(source = "access.availableAccounts", target = "availableAccounts")
    @Mapping(source = "access.allPsd2", target = "allPsd2")
    @Mapping(source = "access.accounts", target = "accounts")
    @Mapping(source = "access.balances", target = "balances")
    @Mapping(source = "access.transactions", target = "transactions")
    AisConsentEntity toAisConsentPO(AisConsentBO consent);
}
