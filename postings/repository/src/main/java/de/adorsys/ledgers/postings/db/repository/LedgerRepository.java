package de.adorsys.ledgers.postings.db.repository;

import java.util.List;
import java.util.Optional;

import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;

public interface LedgerRepository extends NamedEntityRepository<Ledger> {
	/**
	 * Find a ledger with this name.
	 * 
	 * @param name
	 * @return
	 */
	Optional<Ledger> findOptionalByName(String name);

	/**
	 * Resolve all ledgers using this coa.
	 * 
	 * @param coa
	 * @return
	 */
	List<Ledger> findByCoa(ChartOfAccount coa);
}
