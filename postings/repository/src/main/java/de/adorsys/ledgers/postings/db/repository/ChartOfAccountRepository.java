package de.adorsys.ledgers.postings.db.repository;

import java.util.Optional;

import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;

public interface ChartOfAccountRepository extends NamedEntityRepository<ChartOfAccount> {
	/**
	 * Find a chart of account with this name.
	 * 
	 * @param name
	 * @return
	 */
	Optional<ChartOfAccount> findOptionalByName(String name);
}
