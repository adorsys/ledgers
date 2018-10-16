package de.adorsys.ledgers.postings.service.impl;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
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

    /**
     * Create a new chart of account.
     * <p>
     * Generate a new id
     * Sets the creation time
     * Set the creating user from user principal.
     */
    @Override
    public ChartOfAccount newChartOfAccount(ChartOfAccount coa) {
        LocalDateTime created = LocalDateTime.now();
        String user = principal.getName();
        // Save new coa
        ChartOfAccount chartOfAccount = new ChartOfAccount(Ids.id(), created, user, coa.getShortDesc(), coa.getLongDesc(), coa.getName());

        // Return clone.
        return cloneObject(chartOfAccountRepo.save(chartOfAccount), ChartOfAccount.class);
    }

    @Override
    public Optional<ChartOfAccount> findChartOfAccountsById(String id) {
        return chartOfAccountRepo.findById(id)
                       .map(c -> cloneObject(chartOfAccountRepo.save(c), ChartOfAccount.class));
    }

    @Override
    public Optional<ChartOfAccount> findChartOfAccountsByName(String name) {
        return chartOfAccountRepo.findOptionalByName(name)
                       .map(c -> cloneObject(c, ChartOfAccount.class));
    }
}
