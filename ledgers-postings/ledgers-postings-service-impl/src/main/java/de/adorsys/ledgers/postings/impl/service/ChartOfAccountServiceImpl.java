/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.api.service.ChartOfAccountService;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.impl.converter.ChartOfAccountMapper;
import de.adorsys.ledgers.util.Ids;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ChartOfAccountServiceImpl extends AbstractServiceImpl implements ChartOfAccountService {
    private final ChartOfAccountMapper chartOfAccountMapper = Mappers.getMapper(ChartOfAccountMapper.class);

    public ChartOfAccountServiceImpl(LedgerAccountRepository ledgerAccountRepository, ChartOfAccountRepository chartOfAccountRepo, LedgerRepository ledgerRepository) {
        super(ledgerAccountRepository, chartOfAccountRepo, ledgerRepository);
    }

    /**
     * Create a new chart of account.
     * <p>
     * Generate a new id
     * Sets the creation time
     * Set the creating user from user principal.
     */
    @Override
    public ChartOfAccountBO newChartOfAccount(ChartOfAccountBO coa) {
        LocalDateTime created = LocalDateTime.now();
        // Save new coa
        ChartOfAccount chartOfAccount = new ChartOfAccount(Ids.id(), created, coa.getUserDetails(), coa.getShortDesc(), coa.getLongDesc(), coa.getName());

        // Return clone.
        return chartOfAccountMapper.toChartOfAccountBO(chartOfAccountRepo.save(chartOfAccount));
    }

    @Override
    public Optional<ChartOfAccountBO> findChartOfAccountsById(String id) {
        return chartOfAccountRepo.findById(id)
                       .map(chartOfAccountMapper::toChartOfAccountBO);
    }

    @Override
    public Optional<ChartOfAccountBO> findChartOfAccountsByName(String name) {
        return chartOfAccountRepo.findOptionalByName(name)
                       .map(chartOfAccountMapper::toChartOfAccountBO);
    }
}
