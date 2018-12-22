package de.adorsys.ledgers.postings.api.service;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.ChartOfAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;

import java.util.Optional;

/**
 * Service implementing all ledger functionalities.
 *
 * @author fpo
 */
public interface LedgerService {

    /**
     * Creates a new Ledger.
     *
     * @param ledger
     * @return
     * @throws LedgerNotFoundException
     */
    LedgerBO newLedger(LedgerBO ledger) throws LedgerNotFoundException, ChartOfAccountNotFoundException;

    Optional<LedgerBO> findLedgerById(String id);

    /**
     * List all ledgers with the given name. These are generally different versions of the same ledger.
     *
     * @param name
     * @return
     */
    Optional<LedgerBO> findLedgerByName(String name);

    /**
     * Create a new Ledger account.
     * <p>
     * While creating a ledger account, the parent hat to be specified.
     *
     * @param ledgerAccount
     * @return
     * @throws LedgerAccountNotFoundException
     */
    LedgerAccountBO newLedgerAccount(LedgerAccountBO ledgerAccount, String userName) throws LedgerAccountNotFoundException, LedgerNotFoundException;

    Optional<LedgerAccountBO> findLedgerAccountById(String id);

    /**
     * Find the ledger account with the given name
     *
     * @param name
     * @return
     */
    LedgerAccountBO findLedgerAccount(LedgerBO ledger, String name) throws LedgerNotFoundException, LedgerAccountNotFoundException;
}
