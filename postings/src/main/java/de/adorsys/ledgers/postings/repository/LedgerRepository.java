package de.adorsys.ledgers.postings.repository;

import org.springframework.data.repository.CrudRepository;

import de.adorsys.ledgers.postings.domain.Ledger;

public interface LedgerRepository extends CrudRepository<Ledger, String> {}
