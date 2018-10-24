package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CurrencyMapper.class)
public interface DepositAccountMapper {

    DepositAccountBO toDepositAccountBO(DepositAccount depositAccount);

    DepositAccount toDepositAccount(DepositAccountBO depositAccount);
}
