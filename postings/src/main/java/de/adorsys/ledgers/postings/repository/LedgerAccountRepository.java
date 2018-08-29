package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;

public interface LedgerAccountRepository extends TimeBasedEntityRepository<LedgerAccount> {
	/*Find root account by coa and validity*/
	List<LedgerAccount> findByLedgerAndLevelAndAccountTypeAndValidFromBeforeAndValidToAfter(Ledger ledger, int level, LedgerAccountType accountType, LocalDateTime validFrom, LocalDateTime validTo);

	Optional<LedgerAccount> findFirstOptionalByLedgerAndNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(Ledger ledger, String name, LocalDateTime validFrom, LocalDateTime validTo);	

	List<LedgerAccount> findByLedgerAndName(Ledger ledger, String name);
}
