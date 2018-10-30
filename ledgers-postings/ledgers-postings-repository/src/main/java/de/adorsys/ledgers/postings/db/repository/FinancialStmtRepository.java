package de.adorsys.ledgers.postings.db.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.postings.db.domain.FinancialStmt;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.Posting;
import de.adorsys.ledgers.postings.db.domain.StmtStatus;
import de.adorsys.ledgers.postings.db.domain.StmtType;

public interface FinancialStmtRepository extends PagingAndSortingRepository<FinancialStmt, String> {
	
	/**
	 * Find the financial statement of the given posting.
	 * 
	 * @param posting
	 * @return
	 */
	Optional<FinancialStmt> findByPosting(Posting posting);

	/**
	 * Find all financial statements with this stmtId from this ledger, ordered by posting time oldest first.
	 * 
	 * e.g. Find all my account statements.
	 * 
	 * @param ledger
	 * @param stmtType
	 * @param stmtTarget
	 * @return
	 */
	List<FinancialStmt> findByLedgerAndStmtTypeAndStmtTargetOrderByPstTimeDesc(Ledger ledger, StmtType stmtType, String stmtTarget);
	
	Optional<FinancialStmt> findFirstByLedgerAndStmtTypeAndStmtTargetAndStmtStatusOrderByPstTimeDesc(Ledger ledger, StmtType stmtType, String stmtTarget, StmtStatus stmtStatus);
	
	/**
	 * Find all financial statements with this stmtId and posting time from this ledger.
	 * 
	 * @param ledger
	 * @param stmtType
	 * @param stmtTarget
	 * @param pstTime
	 * @return
	 */
	List<FinancialStmt> findByLedgerAndStmtTypeAndStmtTargetAndPstTimeOrderByStmtSeqNbrDesc(Ledger ledger, StmtType stmtType, String stmtTarget, LocalDateTime pstTime);

}
