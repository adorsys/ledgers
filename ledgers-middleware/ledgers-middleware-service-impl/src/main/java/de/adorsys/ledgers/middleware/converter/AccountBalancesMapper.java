package de.adorsys.ledgers.middleware.converter;

import de.adorsys.ledgers.middleware.service.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.postings.api.domain.BalanceBO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface AccountBalancesMapper {

    List<AccountBalanceTO> toAccountBalancesTO(List<BalanceBO> balances);
}
