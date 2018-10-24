package de.adorsys.ledgers.postings.api.service;

import java.util.Optional;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;

/**
 * Service implementing all chart of account functionalities.
 *
 * @author fpo
 */
public interface ChartOfAccountService {

    /**
     * @param chartOfAccount
     * @return
     */
    ChartOfAccountBO newChartOfAccount(ChartOfAccountBO chartOfAccount);

    /**
     * List all chart of accounts with the given name. These are generally different versions of the same chart of account.
     *
     * @param name
     * @return an empty list if no chart of account with the given name.
     */
    Optional<ChartOfAccountBO> findChartOfAccountsByName(String name);

    Optional<ChartOfAccountBO> findChartOfAccountsById(String id);
}
