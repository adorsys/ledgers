package de.adorsys.ledgers.postings.api.service;

import java.time.LocalDateTime;

import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.domain.LedgerStmtBO;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;

public interface LedgerStmtService {
	/**
	 * Read a balance statement. Eventually compute one without storing. 
	 * 
	 * @param ledger
	 * @param refTime
	 * @return
	 * @throws LedgerNotFoundException
	 * @throws BaseLineException
	 */
    LedgerStmtBO readStmt(LedgerBO ledger, LocalDateTime refTime) throws LedgerNotFoundException, BaseLineException;	

	/**
	 * Prepare a balance statement for the given account at the given reference time and store it.
	 * 
	 * @param ledger
	 * @param refTime
	 * @return
	 * @throws LedgerNotFoundException
	 * @throws BaseLineException
	 */
    LedgerStmtBO createStmt(LedgerBO ledger, LocalDateTime refTime) throws LedgerNotFoundException, BaseLineException;

    /**
     * Take a trial statement and mark turn it into a closed statement. This is independent on weather this is
     * an account or a balance sheet statement.
     * 
     * @param stmt
     * @return
     */
    LedgerStmtBO closeStmt(LedgerStmtBO stmt);
}
