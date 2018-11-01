package de.adorsys.ledgers.postings.api.service;

import de.adorsys.ledgers.postings.api.domain.BalanceBO;

import java.util.List;

public interface AccountBalancesService {
    List<BalanceBO> getBalances(String iban);
}
