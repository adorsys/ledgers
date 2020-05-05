package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class AccountDetailsMapper {

    public abstract AccountDetailsTO toAccountDetailsTO(DepositAccountBO details, List<BalanceBO> balances);

    public abstract AccountDetailsTO toAccountDetailsTO(DepositAccountBO source);

    public abstract DepositAccountBO toDepositAccountBO(AccountDetailsTO details);

    public abstract List<AccountBalanceTO> toAccountBalancesTO(List<BalanceBO> balances);

    public AccountDetailsTO toAccountDetailsTO(DepositAccountDetailsBO depositAccountDetailBO) {
        return toAccountDetailsTO(depositAccountDetailBO.getAccount(), depositAccountDetailBO.getBalances());
    }

    public abstract FundsConfirmationRequestBO toFundsConfirmationRequestBO(FundsConfirmationRequestTO request);

    public abstract List<AccountDetailsTO> toAccountDetailsList(List<DepositAccountBO> accountsByIbanAndCurrency);

    public abstract List<AccountReferenceBO> toAccountReferenceList(List<AccountAccessBO> accessBOList);

    public abstract AccountReferenceBO toAccountReference(AccountAccessBO access);
}
