package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsExtendedTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountStatusTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class AccountDetailsMapper {

    @Mapping(target = "accountStatus", expression = "java(toAccountStatusTO(details))")
    public abstract AccountDetailsTO toAccountDetailsTO(DepositAccountBO details, List<BalanceBO> balances);

    public abstract AccountDetailsExtendedTO toAccountDetailsExtendedTO(DepositAccountBO source, String branchLogin);

    public abstract DepositAccountBO toDepositAccountBO(AccountDetailsTO source);

    public AccountDetailsTO toAccountDetailsTO(DepositAccountDetailsBO source) {
        return toAccountDetailsTO(source.getAccount(), source.getBalances());
    }

    public abstract List<AccountDetailsTO> toAccountDetailsTOList(List<DepositAccountDetailsBO> source);

    public abstract FundsConfirmationRequestBO toFundsConfirmationRequestBO(FundsConfirmationRequestTO source);

    public abstract List<AccountDetailsTO> toAccountDetailsList(List<DepositAccountBO> source);

    public abstract List<AccountReferenceBO> toAccountReferenceList(List<AccountAccessBO> source);

    protected AccountStatusTO toAccountStatusTO(DepositAccountBO details) {
        return details.isEnabled()
                       ? AccountStatusTO.ENABLED
                       : AccountStatusTO.BLOCKED;
    }
}
