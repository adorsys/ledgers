package de.adorsys.ledgers.deposit.api.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.domain.BalanceBO;
import de.adorsys.ledgers.deposit.api.service.AccountBalancesService;

@Service
public class AccountBalancesServiceImpl implements AccountBalancesService {
	
	@Override
	public List<BalanceBO> getBalances(String iban) {
		return Collections.emptyList();
	}

}
