package de.adorsys.ledgers.middleware.converter;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.postings.api.domain.BalanceBO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AccountConverter {

    private final AccountDetailsMapper detailsMapper;

    private final AccountBalancesMapper balancesMapper;

    public AccountConverter(AccountDetailsMapper detailsMapper, AccountBalancesMapper balancesMapper) {
        this.detailsMapper = detailsMapper;
        this.balancesMapper = balancesMapper;
    }

    public AccountDetailsTO toAccountDetailsTO(DepositAccountBO accountDetails, List<BalanceBO> balances) {
        AccountDetailsTO details = detailsMapper.toAccountDetailsTO(accountDetails);
        details.setBalances(balancesMapper.toAccountBalancesTO(balances));
        return details;
    }

    public DepositAccountBO toDepositAccountBO(AccountDetailsTO accountDetails) {
        return detailsMapper.toDepositAccountBO(accountDetails);
    }
}
