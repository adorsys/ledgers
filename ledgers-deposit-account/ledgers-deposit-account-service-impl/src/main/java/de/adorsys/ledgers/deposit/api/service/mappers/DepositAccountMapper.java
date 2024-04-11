/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.AccountReferenceBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.db.domain.AccountReference;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepositAccountMapper {

    DepositAccountBO toDepositAccountBO(DepositAccount depositAccount);

    List<DepositAccountBO> toDepositAccountListBO(List<DepositAccount> list);

    DepositAccount toDepositAccount(DepositAccountBO depositAccount);

    AccountReferenceBO toAccountReferenceBO(DepositAccount depositAccount);

    AccountReference toAccountReference(AccountReferenceBO reference);

}
