package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.BalanceBO;
import de.adorsys.ledgers.postings.api.service.AccountBalancesService;

import java.util.Collections;
import java.util.List;

public class AccountBalanceServiceImpl implements AccountBalancesService {
    @Override
    public List<BalanceBO> getBalances(String iban) {
        return Collections.emptyList(); //TODO @fpo implement get balances here please
    }
}
