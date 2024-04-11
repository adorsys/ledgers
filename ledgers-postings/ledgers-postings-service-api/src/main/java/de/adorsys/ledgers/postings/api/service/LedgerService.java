/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.api.service;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service implementing all ledger functionality.
 *
 * @author fpo
 */
public interface LedgerService {

    /**
     * Creates a new Ledger.
     *
     * @param ledger ledger
     * @return created ledger
     */
    LedgerBO newLedger(LedgerBO ledger);

    Optional<LedgerBO> findLedgerById(String id);

    /**
     * List all ledgers with the given name. These are generally different versions of the same ledger.
     *
     * @param name ledger name
     * @return ledger
     */
    Optional<LedgerBO> findLedgerByName(String name);

    /**
     * Create a new Ledger account.
     * While creating a ledger account, the parent has to be specified.
     *
     * @param ledgerAccount new ledger account
     * @return created ledger account
     */
    LedgerAccountBO newLedgerAccount(LedgerAccountBO ledgerAccount, String userName);

    LedgerAccountBO findLedgerAccountById(String id);

    /**
     * Find the ledger account with the given name
     *
     * @param name ledger account name
     * @return requested ledger account
     */
    LedgerAccountBO findLedgerAccount(LedgerBO ledger, String name);

    /**
     * @param ledger ledger
     * @param name   ledger account name
     * @return boolean representation of presence of requested ledger account
     */
    boolean checkIfLedgerAccountExist(LedgerBO ledger, String name);

    Map<String, LedgerAccountBO> finLedgerAccountsByIbans(Set<String> ibans, LedgerBO ledger);
}
