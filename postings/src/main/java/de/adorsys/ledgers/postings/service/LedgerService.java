package de.adorsys.ledgers.postings.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;

/**
 * Service implementing all ledger functionalities.
 * 
 * @author fpo
 *
 */
public interface LedgerService {
	
	/**
	 * Creates a new Ledger.
	 * 
	 * @param ledger
	 * @return
	 */
	public Ledger newLedger(Ledger ledger);
	
	/**
	 * List all ledgers with the given name. These are generally different versions of the same ledger.
	 * 
	 * @param name
	 * @return
	 */
	public Optional<Ledger> findLedgerByName(String name);
	
	/**
	 * Create a new Ledger account.
	 * 
	 * While creating a ledger account, the parent hat to be specified.
	 * 
	 * @param ledgerAccount
	 * @return
	 */
	public LedgerAccount newLedgerAccount(LedgerAccount ledgerAccount);
	
	/**
	 * Find the ledger account with the given name 
	 * 
	 * @param name
	 * @return
	 */
	public Optional<LedgerAccount> findLedgerAccount(String name, LocalDateTime referenceDate);

	/**
	 * Loads all ledger accounts with the given name.
	 * 
	 * @param name
	 * @return
	 */
	public List<LedgerAccount> findLedgerAccounts(String name);
}
