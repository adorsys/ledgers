package de.adorsys.ledgers.postings.service.impl;

import de.adorsys.ledgers.postings.converter.ChartOfAccountMapper;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.service.ChartOfAccountService;
import de.adorsys.ledgers.util.Ids;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static de.adorsys.ledgers.util.CloneUtils.cloneObject;

@Service
@Transactional
public class ChartOfAccountServiceImpl extends AbstractServiceImpl implements ChartOfAccountService {
    private final ChartOfAccountMapper chartOfAccountMapper;

    public ChartOfAccountServiceImpl(ChartOfAccountMapper chartOfAccountMapper) {
        this.chartOfAccountMapper = chartOfAccountMapper;
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
        String user = principal.getName();
        // Save new coa
        ChartOfAccount chartOfAccount = new ChartOfAccount(Ids.id(), created, user, coa.getShortDesc(), coa.getLongDesc(), coa.getName());

        // Return clone.
        return chartOfAccountMapper.toChartOfAccountBO(chartOfAccountRepo.save(chartOfAccount));
    }

    @Override
    public Optional<ChartOfAccountBO> findChartOfAccountsById(String id) {
        return chartOfAccountRepo.findById(id)
                       .map(chartOfAccountRepo::save)
                       .map(chartOfAccountMapper::toChartOfAccountBO);
    }

    @Override
    public Optional<ChartOfAccountBO> findChartOfAccountsByName(String name) {
        return chartOfAccountRepo.findOptionalByName(name)
                       .map(chartOfAccountMapper::toChartOfAccountBO);
    }
}
