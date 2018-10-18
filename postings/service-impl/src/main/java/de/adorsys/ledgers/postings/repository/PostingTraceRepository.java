package de.adorsys.ledgers.postings.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.postings.domain.PostingTrace;

public interface PostingTraceRepository extends PagingAndSortingRepository<PostingTrace, String> {}
