package de.adorsys.ledgers.postings.api.service;

import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.domain.LedgerStmtBO;

import java.time.LocalDateTime;

//TODO unused INTERFACE to be removed https://git.adorsys.de/adorsys/xs2a/psd2-dynamic-sandbox/issues/195
public interface LedgerStmtService {
	/**
	 * Read a balance statement. Eventually compute one without storing. 
	 * 
	 * @param ledger
	 * @param refTime
	 * @return
	 */
    LedgerStmtBO readStmt(LedgerBO ledger, LocalDateTime refTime);

	/**
	 * Prepare a balance statement for the given account at the given reference time and store it.
	 * 
	 * @param ledger
	 * @param refTime
	 * @return
	 */
    LedgerStmtBO createStmt(LedgerBO ledger, LocalDateTime refTime);

    /**
     * Take a trial statement and mark turn it into a closed statement. This is independent on weather this is
     * an account or a balance sheet statement.
     * 
     * @param stmt
     * @return
     */
    LedgerStmtBO closeStmt(LedgerStmtBO stmt);
}
