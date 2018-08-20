package de.adorsys.ledgers.postings.repository;

import java.util.List;
import java.util.Optional;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;

public interface LedgerAccountTypeRepository extends NamedEntityRepository<LedgerAccountType> {
	
	/**
	 * Find a chart of account of this name.
	 * 
	 * @param coa
	 * @param name
	 * @return
	 */
	Optional<LedgerAccountType> findOptionalByCoaAndName(ChartOfAccount coa, String name);
	
	/**
	 * Find all children ledger account types of this parent.
	 *  
	 * @param coa
	 * @param parent
	 * @return
	 */
	List<LedgerAccountType> findByCoaAndParent(ChartOfAccount coa, String parent);
	
	/**
	 * Resolve all ledger account type of this coa.
	 * 
	 * @param coa
	 * @return
	 */
	List<LedgerAccountType> findByCoaOrderByLevelDesc(ChartOfAccount coa);
	
	/**
	 * Find account by coa and level
	 * @param coa
	 * @param level
	 * @return
	 */
	List<LedgerAccountType> findByCoaAndLevel(ChartOfAccount coa, int level);
}
