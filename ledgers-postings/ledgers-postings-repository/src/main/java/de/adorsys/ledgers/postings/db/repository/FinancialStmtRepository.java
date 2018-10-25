package de.adorsys.ledgers.postings.db.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import de.adorsys.ledgers.postings.db.domain.FinancialStmt;
import de.adorsys.ledgers.postings.db.domain.Ledger;

public interface FinancialStmtRepository extends NamedEntityRepository<FinancialStmt> {
	
	/**
	 * Find the financial statement of the given posting.
	 * 
	 * @param pstId
	 * @return
	 */
	Optional<FinancialStmt> findOptionalByPstId(String pstId);

	/**
	 * Find all financial statements with this name from this ledger.
	 * 
	 * @param ledger
	 * @param name
	 * @return
	 */
	List<FinancialStmt> findByLedgerAndNameOrderByPstTimeDescCreatedDesc(Ledger ledger, String name);
	
	/**
	 * Find all financial statements with this name and posting time from this ledger.
	 * 
	 * @param ledger
	 * @param name
	 * @param pstTime
	 * @return
	 */
	List<FinancialStmt> findByLedgerAndNameAndPstTimeOrderByCreatedDesc(Ledger ledger, String name, LocalDateTime pstTime);

	/**
	 * Find all financial statements with this name and posting time from this ledger.
	 * 
	 * @param ledger
	 * @param pstTime
	 * @return
	 */
	List<FinancialStmt> findByLedgerAndPstTimeAfterOrderByCreatedDesc(Ledger ledger, LocalDateTime pstTime);
}
