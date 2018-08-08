package de.adorsys.ledgers.postings.repository;

import org.springframework.data.repository.CrudRepository;

import de.adorsys.ledgers.postings.domain.PostingTrace;

public interface PostingTraceRepository extends CrudRepository<PostingTrace, String> {}
