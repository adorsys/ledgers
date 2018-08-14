package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;
import java.util.List;

import de.adorsys.ledgers.postings.domain.LedgerAccountType;

public interface LedgerAccountTypeRepository extends LedgerEntityRepository<LedgerAccountType> {
	/*We assume a valid to is only present when needed. If valid to is not null and */
	List<LedgerAccountType> findByParentAndValidFromBeforeAndValidToAfterOrderByLevelDescValidFromDesc(String parent, LocalDateTime validFrom, LocalDateTime validTo);
	List<LedgerAccountType> findByCoaAndValidFromBeforeAndValidToAfterOrderByLevelDescValidFromDesc(String coa, LocalDateTime validFrom, LocalDateTime validTo);
	
	/*Find root account by coa and validity*/
	List<LedgerAccountType> findByCoaAndLevelAndValidFromBeforeAndValidToAfter(String coa, int level, LocalDateTime validFrom, LocalDateTime validTo);
}
