package de.adorsys.ledgers.postings.repository;

import java.util.List;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;

public interface LedgerAccountTypeRepository extends NamedEntityRepository<LedgerAccountType> {
	/*We assume a valid to is only present when needed. If valid to is not null and */
	List<LedgerAccountType> findByParentOrderByLevelDesc(String parent);
	
	/*Resolve all ledger account type of this coa*/
	List<LedgerAccountType> findByCoaOrderByLevelDesc(ChartOfAccount coa);
	
	/*Find root account by coa and validity*/
	List<LedgerAccountType> findByCoaAndLevel(ChartOfAccount coa, int level);
}
