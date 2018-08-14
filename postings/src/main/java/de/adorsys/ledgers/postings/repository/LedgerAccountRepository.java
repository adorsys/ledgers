package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;
import java.util.List;

import de.adorsys.ledgers.postings.domain.LedgerAccount;

public interface LedgerAccountRepository extends LedgerEntityRepository<LedgerAccount> {
	
	/*Find root account by coa and validity*/
	List<LedgerAccount> findByLedgerAndLevelAndAccountTypeAndValidFromBeforeAndValidToAfter(String ledger, int level, String accountType, LocalDateTime validFrom, LocalDateTime validTo);
}
