package de.adorsys.ledgers.postings.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import de.adorsys.ledgers.postings.basetypes.LedgerAccountName;
import de.adorsys.ledgers.postings.basetypes.LedgerName;
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
	public List<Ledger> findLedgersByName(LedgerName name);

	/**
	 * Find the ledger with the given name and current at the given reference time.
	 * 
	 * This is generally the ledger with the given name and the youngest validFrom time before the 
	 * given reference date.
	 * 
	 * @param name
	 * @param referenceDate
	 * @return
	 */
	public Optional<Ledger> findLedgertByName(LedgerName name, LocalDateTime referenceDate);
	
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
	public Optional<LedgerAccount> findLedgerAccount(LedgerAccountName name, LocalDateTime referenceDate);

	/**
	 * Loads all ledger accounts with the given name.
	 * 
	 * @param name
	 * @return
	 */
	public List<LedgerAccount> findLedgerAccounts(LedgerAccountName name);
}
