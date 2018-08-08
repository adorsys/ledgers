package de.adorsys.ledgers.postings.repository;

import org.springframework.data.repository.CrudRepository;

import de.adorsys.ledgers.postings.domain.Posting;

public interface PostingRepository extends CrudRepository<Posting, String> {}
