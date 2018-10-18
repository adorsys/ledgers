package de.adorsys.ledgers.postings.service.impl;

import de.adorsys.ledgers.postings.converter.LedgerMapper;
import de.adorsys.ledgers.postings.domain.*;
import de.adorsys.ledgers.postings.exception.ChartOfAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.util.Ids;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

public class LedgerServiceImpl extends AbstractServiceImpl implements LedgerService {

    private final LedgerMapper ledgerMapper;

    public LedgerServiceImpl(LedgerMapper ledgerMapper) {
        this.ledgerMapper = ledgerMapper;
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
                loadCoa(ledger.getCoa()),
                ledger.getLastClosing());
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
    public LedgerAccountBO newLedgerAccount(LedgerAccountBO ledgerAccountBO) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        // Validations
        if (StringUtils.isBlank(ledgerAccountBO.getName())) {
            throw new IllegalArgumentException("Missing model name.");
        }
        LedgerAccount ledgerAccount = ledgerAccountMapper.toLedgerAccount(ledgerAccountBO);

        // User
        LedgerAccount parentAccount = getParentAccount(ledgerAccount);
        Ledger ledger = getLedger(ledgerAccount, parentAccount);
        AccountCategory category = getAccountCategory(ledgerAccount, parentAccount);
        LedgerAccount newLedgerAccount = LedgerAccount.builder()
                                                 .id(Ids.id())
                                                 .created(LocalDateTime.now())
                                                 .user(principal.getName())
                                                 .shortDesc(ledgerAccount.getShortDesc())
                                                 .longDesc(ledgerAccount.getLongDesc())
                                                 .name(ledgerAccount.getName())
                                                 .ledger(ledger)
                                                 .coa(ledger.getCoa())
                                                 .parent(parentAccount)
                                                 .category(category)
                                                 .balanceSide(getBalanceSide(ledgerAccount, parentAccount, category))
                                                 .build();

        return ledgerAccountMapper.toLedgerAccountBO(ledgerAccountRepository.save(newLedgerAccount));
    }

    @Override
    public Optional<LedgerAccountBO> findLedgerAccountById(String id) {
        return ledgerAccountRepository.findById(id)
                       .map(ledgerAccountMapper::toLedgerAccountBO);
    }

    @Override
    public Optional<LedgerAccountBO> findLedgerAccount(LedgerBO ledgerBO, String name) throws LedgerNotFoundException {
        Ledger ledger = ledgerMapper.toLedger(ledgerBO);
        return ledgerAccountRepository
                       .findOptionalByLedgerAndName(loadLedger(ledger), name)
                       .map(ledgerAccountMapper::toLedgerAccountBO);
    }

    private LedgerAccount getParentAccount(LedgerAccount ledgerAccount) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        return ledgerAccount.getParent() != null
                       ? loadLedgerAccount(ledgerAccount.getParent())
                       : null;
    }

    private Ledger getLedger(LedgerAccount ledgerAccount, LedgerAccount parentAccount) throws LedgerNotFoundException {
        return ledgerAccount.getParent() != null
                       ? parentAccount.getLedger()
                       : loadLedger(ledgerAccount.getLedger());
    }

    private BalanceSide getBalanceSide(LedgerAccount ledgerAccount, LedgerAccount parentAccount, AccountCategory category) {
        BalanceSide balanceSide;
        if (ledgerAccount.getBalanceSide() != null) {
            balanceSide = ledgerAccount.getBalanceSide();
        } else if (parentAccount != null) {
            balanceSide = parentAccount.getBalanceSide();
        } else if (category != null) {
            balanceSide = category.getDefaultBs();
        } else {
            throw new IllegalArgumentException("Missing category for: " + ledgerAccount.getShortDesc());
        }
        return balanceSide;
    }

    private AccountCategory getAccountCategory(LedgerAccount ledgerAccount, LedgerAccount parentAccount) {
        return Optional.ofNullable(ledgerAccount.getCategory())
                       .orElseGet(() -> getAccountCategoryFromParent(parentAccount, ledgerAccount.getShortDesc()));
    }

    private AccountCategory getAccountCategoryFromParent(LedgerAccount parentAccount, String shortDescription) {
        return Optional.ofNullable(parentAccount)
                       .map(LedgerAccount::getCategory)
                       .orElseThrow(() -> new IllegalArgumentException("Missing category for: " + shortDescription));
    }
}
