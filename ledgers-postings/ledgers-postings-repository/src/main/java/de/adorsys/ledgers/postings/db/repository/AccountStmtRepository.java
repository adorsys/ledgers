package de.adorsys.ledgers.postings.db.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.postings.db.domain.AccountStmt;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.StmtStatus;

public interface AccountStmtRepository extends PagingAndSortingRepository<AccountStmt, String> {
	
	/**
	 * Select the latest statement for the given reference time.
	 * 
	 * @param account ledger account
	 * @param stmtStatus statement status
	 * @param refTime reference time
	 * @return account statement wrapped with Optional
	 */
	Optional<AccountStmt> findFirstByAccountAndStmtStatusAndPstTimeLessThanOrderByPstTimeDescStmtSeqNbrDesc(
			LedgerAccount account, StmtStatus stmtStatus, LocalDateTime refTime);
	
	
	Optional<AccountStmt> findFirstByAccountAndStmtStatusAndPstTimeGreaterThanEqual(
			LedgerAccount account, StmtStatus stmtStatus, LocalDateTime refTime);

}
