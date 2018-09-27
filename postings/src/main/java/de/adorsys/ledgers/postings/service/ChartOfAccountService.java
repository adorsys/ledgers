package de.adorsys.ledgers.postings.service;

import java.util.List;
import java.util.Optional;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.exception.NotFoundException;

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

	/**
	 * Create a new Ledger account type.
	 * 
	 * While creating a ledger account type, the parent hat to be specified.
	 * 
	 * @param model
	 * @return
	 * @throws NotFoundException 
	 */
	public LedgerAccountType newLedgerAccountType(LedgerAccountType model) throws NotFoundException;
	
	/**
	 * Find the ledger account type with the given name 
	 * 
	 * @param name
	 * @return
	 * @throws NotFoundException 
	 */
	public Optional<LedgerAccountType> findLedgerAccountType(ChartOfAccount chartOfAccount, String name) throws NotFoundException;

	public Optional<LedgerAccountType> findLedgerAccountTypeById(String id);
	
	/**
	 * Returns all valid children of this node.
	 * @param parentName
	 * @return
	 * @throws NotFoundException 
	 */
	List<LedgerAccountType> findChildLedgerAccountTypes(ChartOfAccount chartOfAccount, String parentName) throws NotFoundException;

	/**
	 * Return all valid ledger account types attached to this coa.
	 * 
	 * @param coaName
	 * @return
	 * @throws NotFoundException 
	 */
	List<LedgerAccountType> findCoaLedgerAccountTypes(ChartOfAccount chartOfAccount) throws NotFoundException;

	List<LedgerAccountType> findCoaAccountTypesByLevel(ChartOfAccount chartOfAccount, int level) throws NotFoundException;

}
