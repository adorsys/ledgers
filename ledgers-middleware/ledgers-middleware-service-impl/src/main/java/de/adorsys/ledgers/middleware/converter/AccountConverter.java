package de.adorsys.ledgers.middleware.converter;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AccountConverter {

    private final AccountDetailsMapper detailsMapper;

    public AccountConverter(AccountDetailsMapper detailsMapper) {
        this.detailsMapper = detailsMapper;
    }

    public AccountDetailsTO toAccountDetailsTO(DepositAccountBO accountDetails, List<String> balances) {
        AccountDetailsTO details = detailsMapper.toAccountDetailsTO(accountDetails);
        details.setBalances(Collections.emptyList());
        return details;
    }

    public DepositAccountBO toDepositAccountBO(AccountDetailsTO accountDetails) {
        return detailsMapper.toDepositAccountBO(accountDetails);
    }
}
