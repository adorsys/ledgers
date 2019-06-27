package de.adorsys.ledgers.postings.api.service;

import de.adorsys.ledgers.postings.api.domain.AccountStmtBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;

import java.time.LocalDateTime;

public interface AccountStmtService {

	/**
	 * Read a balance statement. Eventualy compute one without storing.
	 *
	 * @param ledgerAccount
	 * @param refTime
	 * @return
	 */
    AccountStmtBO readStmt(LedgerAccountBO ledgerAccount, LocalDateTime refTime);

	/**
	 * Prepare a balance statement for the given account at the given reference time and store it.
	 *
	 * @param ledgerAccount
	 * @param refTime
	 * @return
	 */
    AccountStmtBO createStmt(LedgerAccountBO ledgerAccount, LocalDateTime refTime);

    /**
     * Take a trial statement and mark turn it into a closed statement. This is independent on weather this is
     * an account or a balance sheet statement.
     *
     * @param stmt
     * @return
     */
    AccountStmtBO closeStmt(AccountStmtBO stmt);

}
