package de.adorsys.ledgers.postings.repository;

import java.util.List;
import java.util.Optional;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;

public interface LedgerAccountRepository extends NamedEntityRepository<LedgerAccount> {
	/*Find root account by coa and validity*/
	List<LedgerAccount> findByLedgerAndLevelAndAccountType(Ledger ledger, int level, LedgerAccountType accountType);

	Optional<LedgerAccount> findOptionalByLedgerAndName(Ledger ledger, String name);	
}
