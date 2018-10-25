package de.adorsys.ledgers.middleware.converter;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountDetailsMapper {
    AccountDetailsTO toAccountDetailsTO(DepositAccountBO details);

    DepositAccountBO toDepositAccountBO(AccountDetailsTO details);
}
