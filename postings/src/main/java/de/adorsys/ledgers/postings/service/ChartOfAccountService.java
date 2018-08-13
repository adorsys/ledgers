package de.adorsys.ledgers.postings.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import de.adorsys.ledgers.postings.basetypes.ChartOfAccountName;
import de.adorsys.ledgers.postings.basetypes.LedgerAccountName;
import de.adorsys.ledgers.postings.basetypes.LedgerAccountTypeName;
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
	public ChartOfAccount newChartOfAccount(ChartOfAccount chartOfAccount, List<LedgerAccountTypeName> rootAccountTypes);
	
	/**
	 * List all chart of accounts with the given name. These are generally different versions of the same chart of account.
	 * 
	 * @param name
	 * @return an empty list if no chart of account with the given name.
	 */
	public List<ChartOfAccount> findChartOfAccountsByName(ChartOfAccountName name);

	/**
	 * Find the chart of account with the given and current at the given reference time.
	 * 
	 * This is generally the chart of account with the given name and the youngest validFrom time before the 
	 * reference date.
	 * 
	 * @param name
	 * @param referenceDate
	 * @return
	 */
	public Optional<ChartOfAccount> findChartOfAccountByName(ChartOfAccountName name, LocalDateTime referenceDate);
	
	/**
	 * Create a new Ledger account type.
	 * 
	 * While creating a ledger account type, the parent hat to be specified.
	 * 
	 * @param ledgerAccountType
	 * @return
	 */
	public LedgerAccountType newLedgerAccountType(LedgerAccountType parent, LedgerAccountTypeName name, LocalDateTime validFrom);
	
	/**
	 * Find the ledger account type with the given name 
	 * 
	 * @param name
	 * @return
	 */
	public Optional<LedgerAccountType> findLedgerAccountType(LedgerAccountName name, LocalDateTime referenceDate);

	/**
	 * Loads all ledger account types with the given name.
	 * 
	 * @param name
	 * @return an empty list of no ledger account type with given name found.
	 */
	public List<LedgerAccountType> findLedgerAccountTypes(LedgerAccountName name);
}
