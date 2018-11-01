package de.adorsys.ledgers.middleware.converter;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.postings.api.domain.BalanceBO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountDetailsMapper {

    AccountDetailsTO toAccountDetailsTO(DepositAccountBO details, List<BalanceBO> balances);

    DepositAccountBO toDepositAccountBO(AccountDetailsTO details);

    List<AccountBalanceTO> toAccountBalancesTO(List<BalanceBO> balances);
}
