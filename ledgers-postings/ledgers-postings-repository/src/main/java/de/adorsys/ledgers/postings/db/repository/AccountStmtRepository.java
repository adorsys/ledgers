/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.repository;

import de.adorsys.ledgers.postings.db.domain.AccountStmt;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.StmtStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AccountStmtRepository extends PagingAndSortingRepository<AccountStmt, String>, CrudRepository<AccountStmt, String> {
	
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
