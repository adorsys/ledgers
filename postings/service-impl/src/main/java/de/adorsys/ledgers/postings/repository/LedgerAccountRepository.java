package de.adorsys.ledgers.postings.repository;

import java.util.Optional;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;

public interface LedgerAccountRepository extends NamedEntityRepository<LedgerAccount> {

	Optional<LedgerAccount> findOptionalByLedgerAndName(Ledger ledger, String name);	
}
