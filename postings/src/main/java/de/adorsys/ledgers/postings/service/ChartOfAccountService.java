package de.adorsys.ledgers.postings.service;

import java.util.Optional;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;

/**
 * Service implementing all chart of account functionalities.
 * 
 * @author fpo
 *
 */
public interface ChartOfAccountService {
	
	/**
	 * 
	 * @param chartOfAccount
	 * @return
	 */
	public ChartOfAccount newChartOfAccount(ChartOfAccount chartOfAccount);
	
	/**
	 * List all chart of accounts with the given name. These are generally different versions of the same chart of account.
	 * 
	 * @param name
	 * @return an empty list if no chart of account with the given name.
	 */
	public Optional<ChartOfAccount> findChartOfAccountsByName(String name);
	
	public Optional<ChartOfAccount> findChartOfAccountsById(String id);	
}
