/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.db.repository;

import de.adorsys.ledgers.um.db.domain.AccountAccess;
import org.springframework.data.repository.CrudRepository;

public interface AccountAccessRepository extends CrudRepository<AccountAccess, String> {
}
