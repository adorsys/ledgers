package de.adorsys.ledgers.middleware.converter;

import java.util.List;

import org.mapstruct.Mapper;

import de.adorsys.ledgers.deposit.api.domain.BalanceBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;

@Mapper(componentModel = "spring")
public interface AccountDetailsMapper {

    AccountDetailsTO toAccountDetailsTO(DepositAccountBO details, List<BalanceBO> balances);


    List<AccountDetailsTO> toAccountDetailsListTO(List<DepositAccountBO> details);

    DepositAccountBO toDepositAccountBO(AccountDetailsTO details);

    List<AccountBalanceTO> toAccountBalancesTO(List<BalanceBO> balances);
}
