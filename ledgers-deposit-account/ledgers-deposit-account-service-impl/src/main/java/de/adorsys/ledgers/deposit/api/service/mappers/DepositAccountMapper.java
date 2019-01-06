package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.AccountReferenceBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = CurrencyMapper.class)
public interface DepositAccountMapper {

    DepositAccountBO toDepositAccountBO(DepositAccount depositAccount);

    List<DepositAccountBO> toDepositAccountListBO(List<DepositAccount> list);

    DepositAccount toDepositAccount(DepositAccountBO depositAccount);

    @Mapping(target = "id", expression = "java(de.adorsys.ledgers.util.Ids.id())")
    DepositAccount createDepositAccountObj(DepositAccount depositAccount);

    AccountReferenceBO toAccountReferenceBO(DepositAccount depositAccount);
}
