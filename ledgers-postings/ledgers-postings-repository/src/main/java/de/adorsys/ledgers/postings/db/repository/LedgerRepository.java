package de.adorsys.ledgers.postings.db.repository;

import java.util.List;
import java.util.Optional;

import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;

public interface LedgerRepository extends NamedEntityRepository<Ledger> {
	/**
	 * Find a ledger with this name.
	 * 
	 * @param name of ledger
	 * @return ledger wrapped with Optional
	 */
	Optional<Ledger> findOptionalByName(String name);

	/**
	 * Resolve all ledgers using this coa.
	 * 
	 * @param coa: chart of account
	 * @return list of ledgers
	 */
	List<Ledger> findByCoa(ChartOfAccount coa);
}
