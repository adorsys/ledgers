package de.adorsys.ledgers.postings.repository;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;

import java.util.Optional;

public interface LedgerAccountRepository extends NamedEntityRepository<LedgerAccount> {

	Optional<LedgerAccount> findOptionalByLedgerAndName(Ledger ledger, String name);
}
