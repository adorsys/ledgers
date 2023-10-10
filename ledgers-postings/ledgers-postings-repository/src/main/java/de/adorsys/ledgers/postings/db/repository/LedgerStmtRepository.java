/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerStmt;
import de.adorsys.ledgers.postings.db.domain.StmtStatus;

public interface LedgerStmtRepository extends PagingAndSortingRepository<LedgerStmt, String>, CrudRepository<LedgerStmt, String> {
	
	/**
	 * Select the latest statement for the given reference time.
	 * 
	 * @param ledger ledger
	 * @param stmtStatus statement status
	 * @param refTime reference time
	 * @return the latest statement for the given reference time
	 */
	Optional<LedgerStmt> findFirstByLedgerAndStmtStatusAndPstTimeLessThanEqualOrderByPstTimeDescStmtSeqNbrDesc(
			Ledger ledger, StmtStatus stmtStatus, LocalDateTime refTime);
}
