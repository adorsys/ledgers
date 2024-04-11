/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.api.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * A financial statement will help draw time lines on ledgers and accounts.
 * 
 * A trial balance is also a financial statement on the balance sheet that is
 * not closed.
 * 
 * You can continuously modify trial balances by adding posting and recomputing
 * some balances.
 * 
 * No changes are allowed in a ledger when the ledger has closed with a
 * financial statement.
 * 
 * @author fpo
 *
 */
@Data
public abstract class FinancialStmtBO {

    private String id;

    /*
     * The corresponding posting.
     * 
     */
	private PostingBO posting;
    
	/* Documents the time of the posting. */
	private LocalDateTime pstTime;

	private StmtStatusBO stmtStatus;

	/*
	 * Identifier of the latest processed posting. We use this to 
	 * perform batch processing. The latest posting process will allways be 
	 * held here.
	 * 
	 */
	private PostingTraceBO latestPst;
	
	/*
	 * The sequence number of the operation processed by this posting.
	 *
	 * A single statement can be overridden many times as long as the enclosing
	 * ledger is not closed. These overriding happens synchronously. Each single one
	 * increasing the sequence number of the former posting.
	 *
	 */
	private int stmtSeqNbr = 0;

}
