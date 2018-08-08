package de.adorsys.ledgers.postings.repository;

import org.springframework.data.repository.CrudRepository;

import de.adorsys.ledgers.postings.domain.LedgerAccountType;

public interface LedgerAccountTypeRepository extends CrudRepository<LedgerAccountType, String> {}
