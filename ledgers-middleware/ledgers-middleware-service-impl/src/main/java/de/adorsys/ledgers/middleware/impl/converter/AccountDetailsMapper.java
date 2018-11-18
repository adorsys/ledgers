package de.adorsys.ledgers.middleware.impl.converter;

import java.util.List;

import org.mapstruct.Mapper;

import de.adorsys.ledgers.deposit.api.domain.BalanceBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;

@Mapper(componentModel = "spring")
public abstract class AccountDetailsMapper {

	public abstract AccountDetailsTO toAccountDetailsTO(DepositAccountBO details, List<BalanceBO> balances);


	public abstract DepositAccountBO toDepositAccountBO(AccountDetailsTO details);

	public abstract List<AccountBalanceTO> toAccountBalancesTO(List<BalanceBO> balances);
    
    public AccountDetailsTO toAccountDetailsTO(DepositAccountDetailsBO depositAccountDetailBO){
    	return toAccountDetailsTO(depositAccountDetailBO.getAccount(), depositAccountDetailBO.getBalances());
    }
    
}
