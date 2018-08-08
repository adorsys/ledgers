package de.adorsys.ledgers.postings.repository;

import org.springframework.data.repository.CrudRepository;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;

public interface ChartOfAccountRepository extends CrudRepository<ChartOfAccount, String> {}
