package de.adorsys.ledgers.postings.repository;

import org.springframework.data.repository.CrudRepository;

import de.adorsys.ledgers.postings.domain.LedgerAccount;

public interface LedgerAccountRepository extends CrudRepository<LedgerAccount, String> {}
