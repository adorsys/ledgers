package de.adorsys.ledgers.deposit.api.service;

import java.util.List;

import de.adorsys.ledgers.deposit.api.domain.BalanceBO;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;

public interface AccountBalancesService {

	List<BalanceBO> getBalances(String iban)  throws LedgerAccountNotFoundException ;

}
