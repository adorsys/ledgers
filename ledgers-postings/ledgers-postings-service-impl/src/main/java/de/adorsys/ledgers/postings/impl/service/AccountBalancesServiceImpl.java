package de.adorsys.ledgers.postings.impl.service;

import java.util.Collections;
import java.util.List;

import de.adorsys.ledgers.postings.api.domain.BalanceBO;
import de.adorsys.ledgers.postings.api.service.AccountBalancesService;

public class AccountBalancesServiceImpl implements AccountBalancesService{

	@Override
	public List<BalanceBO> getBalances(String iban) {
		return Collections.emptyList();
	}

}
