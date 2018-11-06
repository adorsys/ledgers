package de.adorsys.ledgers.deposit.api.service;

import java.util.List;

import de.adorsys.ledgers.deposit.api.domain.BalanceBO;

public interface AccountBalancesService {

	List<BalanceBO> getBalances(String iban);

}
