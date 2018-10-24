package de.adorsys.ledgers.postings.db.repository;

import java.util.Optional;

import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;

public interface LedgerAccountRepository extends NamedEntityRepository<LedgerAccount> {

	Optional<LedgerAccount> findOptionalByLedgerAndName(Ledger ledger, String name);
}
