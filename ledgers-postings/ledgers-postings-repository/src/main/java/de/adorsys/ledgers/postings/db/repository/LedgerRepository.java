/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.repository;

import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface LedgerRepository extends NamedEntityRepository<Ledger>, CrudRepository<Ledger, String> {
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
