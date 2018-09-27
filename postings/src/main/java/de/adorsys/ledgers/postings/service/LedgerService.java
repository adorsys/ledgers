package de.adorsys.ledgers.postings.service;

import java.util.Optional;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.exception.NotFoundException;

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
	 * @throws NotFoundException 
	 */
	public Ledger newLedger(Ledger ledger) throws NotFoundException;
	
	public Optional<Ledger> findLedgerById(String id);
	
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
	 * @throws NotFoundException 
	 */
	public LedgerAccount newLedgerAccount(LedgerAccount ledgerAccount) throws NotFoundException;
	
	public Optional<LedgerAccount> findLedgerAccountById(String id);

	/**
	 * Find the ledger account with the given name 
	 * 
	 * @param name
	 * @return
	 */
	public Optional<LedgerAccount> findLedgerAccount(Ledger ledger, String name);
}
