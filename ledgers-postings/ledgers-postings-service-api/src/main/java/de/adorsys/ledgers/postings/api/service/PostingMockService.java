/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.api.service;

import de.adorsys.ledgers.postings.api.domain.PostingBO;

import java.util.List;

public interface PostingMockService {
    void addPostingsAsBatch(List<PostingBO> postings);
}
