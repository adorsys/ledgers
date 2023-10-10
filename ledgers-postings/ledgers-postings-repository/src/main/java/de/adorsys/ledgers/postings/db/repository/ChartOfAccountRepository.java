/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.repository;

import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ChartOfAccountRepository extends NamedEntityRepository<ChartOfAccount>, CrudRepository<ChartOfAccount, String> {
	/**
	 * Find a chart of account with this name.
	 * 
	 * @param name of chart of account
	 * @return chart of account wrapped with Optional
	 */
	Optional<ChartOfAccount> findOptionalByName(String name);
}
