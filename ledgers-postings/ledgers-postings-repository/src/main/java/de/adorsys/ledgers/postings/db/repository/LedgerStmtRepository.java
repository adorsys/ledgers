package de.adorsys.ledgers.postings.db.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerStmt;
import de.adorsys.ledgers.postings.db.domain.StmtStatus;

public interface LedgerStmtRepository extends PagingAndSortingRepository<LedgerStmt, String> {
	
	/**
	 * Select the latest statement for the given reference time.
	 * 
	 * @param ledger
	 * @param stmtStatus
	 * @param refTime
	 * @return
	 */
	Optional<LedgerStmt> findFirstByLedgerAndStmtStatusAndPstTimeLessThanEqualOrderByPstTimeDescStmtSeqNbrDesc(
			Ledger ledger, StmtStatus stmtStatus, LocalDateTime refTime);
}
