/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.repository;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TimeBasedEntityRepository<T> extends BaseEntityRepository<T> {
	
}
