package de.adorsys.ledgers.postings.service;

import java.util.List;
import java.util.Optional;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;

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
	public ChartOfAccount newChartOfAccount(ChartOfAccount chartOfAccount, List<String> rootAccountTypes);
	
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
	 * @param ledgerAccountType
	 * @param name
	 * @param desc
	 * @return
	 */
	public LedgerAccountType newLedgerAccountType(LedgerAccountType parent, String name, String desc);
	
	/**
	 * Find the ledger account type with the given name 
	 * 
	 * @param name
	 * @return
	 */
	public Optional<LedgerAccountType> findLedgerAccountType(String name);

	public Optional<LedgerAccountType> findLedgerAccountTypeById(String id);
	
	/**
	 * Returns all valid children of this node.
	 * @param parentName
	 * @param referenceDate
	 * @return
	 */
	List<LedgerAccountType> findChildLedgerAccountTypes(String parentName);

	/**
	 * Return all valid ledger account types attached to this coa.
	 * 
	 * @param coaName
	 * @param referenceDate
	 * @return
	 */
	List<LedgerAccountType> findCoaLedgerAccountTypes(String coaName);

}
