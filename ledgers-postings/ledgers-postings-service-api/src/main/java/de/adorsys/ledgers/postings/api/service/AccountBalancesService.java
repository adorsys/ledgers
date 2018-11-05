package de.adorsys.ledgers.postings.api.service;

import java.util.List;

import de.adorsys.ledgers.postings.api.domain.BalanceBO;

public interface AccountBalancesService {

	List<BalanceBO> getBalances(String iban);

}
