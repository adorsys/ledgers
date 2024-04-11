/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.postings.db.domain.PostingTrace;

public interface PostingTraceRepository extends PagingAndSortingRepository<PostingTrace, String>, CrudRepository<PostingTrace, String> {
}
