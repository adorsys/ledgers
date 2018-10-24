package de.adorsys.ledgers.postings.db.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.postings.db.domain.PostingTrace;

public interface PostingTraceRepository extends PagingAndSortingRepository<PostingTrace, String> {}
