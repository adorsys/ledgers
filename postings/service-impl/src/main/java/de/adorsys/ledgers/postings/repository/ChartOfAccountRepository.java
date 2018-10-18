package de.adorsys.ledgers.postings.repository;

import java.util.Optional;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;

public interface ChartOfAccountRepository extends NamedEntityRepository<ChartOfAccount> {
	/**
	 * Find a chart of account with this name.
	 * 
	 * @param name
	 * @return
	 */
	Optional<ChartOfAccount> findOptionalByName(String name);
}
