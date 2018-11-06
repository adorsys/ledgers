package de.adorsys.ledgers.postings.api.service;

import java.time.LocalDateTime;

import de.adorsys.ledgers.postings.api.domain.AccountStmtBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;

public interface AccountStmtService {
	
	/**
	 * Read a balance statement. Eventualy compute one without storing. 
	 * 
	 * @param ledgerAccount
	 * @param refTime
	 * @return
	 * @throws LedgerAccountNotFoundException
	 * @throws LedgerNotFoundException
	 * @throws BaseLineException
	 */
    AccountStmtBO readStmt(LedgerAccountBO ledgerAccount, LocalDateTime refTime) throws LedgerAccountNotFoundException, LedgerNotFoundException, BaseLineException;	

	/**
	 * Prepare a balance statement for the given account at the given reference time and store it.
	 * 
	 * @param ledgerAccount
	 * @param refTime
	 * @return
	 * @throws LedgerAccountNotFoundException
	 * @throws LedgerNotFoundException
	 * @throws BaseLineException
	 */
    AccountStmtBO createStmt(LedgerAccountBO ledgerAccount, LocalDateTime refTime) throws LedgerAccountNotFoundException, LedgerNotFoundException, BaseLineException;

    /**
     * Take a trial statement and mark turn it into a closed statement. This is independent on weather this is
     * an account or a balance sheet statement.
     * 
     * @param stmt
     * @return
     */
    AccountStmtBO closeStmt(AccountStmtBO stmt);
    
}
