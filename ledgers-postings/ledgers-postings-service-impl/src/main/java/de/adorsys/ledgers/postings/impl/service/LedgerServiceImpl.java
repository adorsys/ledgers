package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.ChartOfAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.db.domain.*;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.impl.converter.LedgerAccountMapper;
import de.adorsys.ledgers.postings.impl.converter.LedgerMapper;
import de.adorsys.ledgers.util.Ids;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LedgerServiceImpl extends AbstractServiceImpl implements LedgerService {

    private final LedgerMapper ledgerMapper;
    private final LedgerAccountMapper ledgerAccountMapper;

    public LedgerServiceImpl(LedgerAccountRepository ledgerAccountRepository, ChartOfAccountRepository chartOfAccountRepo, Principal principal, LedgerRepository ledgerRepository, LedgerMapper ledgerMapper, LedgerAccountMapper ledgerAccountMapper) {
        super(ledgerAccountRepository, chartOfAccountRepo, principal, ledgerRepository);
        this.ledgerMapper = ledgerMapper;
        this.ledgerAccountMapper = ledgerAccountMapper;
    }

    @Override
    public LedgerBO newLedger(LedgerBO ledger) throws ChartOfAccountNotFoundException {
        Ledger newLedger = new Ledger(
                Ids.id(),
                LocalDateTime.now(),
                principal.getName(),
                ledger.getShortDesc(),
                ledger.getLongDesc(),
                ledger.getName(),
                loadCoa(ledger.getCoa()));
        Ledger savedLedger = ledgerRepository.save(newLedger);

        return ledgerMapper.toLedgerBO(savedLedger);
    }

    @Override
    public Optional<LedgerBO> findLedgerById(String id) {
        return ledgerRepository.findById(id)
                       .map(ledgerMapper::toLedgerBO);
    }

    @Override
    public Optional<LedgerBO> findLedgerByName(String name) {
        return ledgerRepository.findOptionalByName(name)
                       .map(ledgerMapper::toLedgerBO);
    }

    @Override
    public LedgerAccountBO newLedgerAccount(LedgerAccountBO ledgerAccount) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        // Validations
        if (StringUtils.isBlank(ledgerAccount.getName())) {
            throw new IllegalArgumentException("Missing model name.");
        }

        // User
        LedgerAccount parentAccount = getParentAccount(ledgerAccount);
        Ledger ledger = parentAccount != null
                                ? parentAccount.getLedger()
                                : loadLedger(ledgerAccount.getLedger());

        AccountCategory category = ledgerAccount.getCategory() != null
                                           ? AccountCategory.valueOf(ledgerAccount.getCategory().name())
                                           : getAccountCategoryFromParent(parentAccount, ledgerAccount.getShortDesc());

        BalanceSide balanceSide = ledgerAccount.getBalanceSide() != null
                                          ? BalanceSide.valueOf(ledgerAccount.getBalanceSide().name())
                                          : getBalanceSide(parentAccount, category, ledgerAccount.getShortDesc());

        String id = Ids.id();
        LocalDateTime created = LocalDateTime.now();
        String user = principal.getName();
        String shortDesc = ledgerAccount.getShortDesc();
        String longDesc = ledgerAccount.getLongDesc();
        String name = ledgerAccount.getName();
        ChartOfAccount coa = ledger.getCoa();
        LedgerAccount newLedgerAccount = new LedgerAccount(id, created, user, shortDesc, longDesc, name, ledger, parentAccount, coa, balanceSide, category);
        return ledgerAccountMapper.toLedgerAccountBO(ledgerAccountRepository.save(newLedgerAccount));
    }

    @Override
    public Optional<LedgerAccountBO> findLedgerAccountById(String id) {
        return ledgerAccountRepository.findById(id)
                       .map(ledgerAccountMapper::toLedgerAccountBO);
    }

    @Override
    public LedgerAccountBO findLedgerAccount(LedgerBO ledgerBO, String name) throws LedgerNotFoundException, LedgerAccountNotFoundException {
        Ledger ledger = ledgerMapper.toLedger(ledgerBO);
        return ledgerAccountRepository
                       .findOptionalByLedgerAndName(loadLedger(ledger), name)
                       .map(ledgerAccountMapper::toLedgerAccountBO)
                       .orElseThrow(() -> new LedgerAccountNotFoundException(name));
    }

    private LedgerAccount getParentAccount(LedgerAccountBO ledgerAccount) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        return ledgerAccount.getParent() != null
                       ? loadLedgerAccount(ledgerAccount.getParent())
                       : null;
    }

    private BalanceSide getBalanceSide(LedgerAccount parentAccount, AccountCategory category, String shortDesc) {
        BalanceSide balanceSide;
        if (parentAccount != null) {
            balanceSide = parentAccount.getBalanceSide();
        } else if (category != null) {
            balanceSide = category.getDefaultBs();
        } else {
            throw new IllegalArgumentException("Missing category for: " + shortDesc);
        }
        return balanceSide;
    }

    private AccountCategory getAccountCategoryFromParent(LedgerAccount parentAccount, String shortDescription) {
        return Optional.ofNullable(parentAccount)
                       .map(LedgerAccount::getCategory)
                       .orElseThrow(() -> new IllegalArgumentException("Missing category for: " + shortDescription));
    }
}
