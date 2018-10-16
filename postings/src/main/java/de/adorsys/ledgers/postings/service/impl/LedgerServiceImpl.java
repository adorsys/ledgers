package de.adorsys.ledgers.postings.service.impl;

import de.adorsys.ledgers.postings.domain.AccountCategory;
import de.adorsys.ledgers.postings.domain.BalanceSide;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.util.CloneUtils;
import de.adorsys.ledgers.util.Ids;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class LedgerServiceImpl extends AbstractServiceImpl implements LedgerService {

    /**
     * Creates a new ledger.
     *
     * @throws NotFoundException
     */
    @Override
    public Ledger newLedger(Ledger ledger) throws NotFoundException {
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

        return CloneUtils.cloneObject(savedLedger, Ledger.class);
    }

    @Override
    public Optional<Ledger> findLedgerById(String id) {
        return ledgerRepository.findById(id)
                       .map(l -> CloneUtils.cloneObject(l, Ledger.class));
    }

    @Override
    public Optional<Ledger> findLedgerByName(String name) {
        return ledgerRepository.findOptionalByName(name)
                       .map(l -> CloneUtils.cloneObject(l, Ledger.class));
    }

    @Override
    public LedgerAccount newLedgerAccount(LedgerAccount ledgerAccount) throws NotFoundException {
        // Validations
        if (StringUtils.isBlank(ledgerAccount.getName())) {
            throw new IllegalArgumentException("Missing model name.");
        }

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

        return CloneUtils.cloneObject(ledgerAccountRepository.save(newLedgerAccount), LedgerAccount.class);
    }

    private LedgerAccount getParentAccount(LedgerAccount ledgerAccount) throws NotFoundException {
        return ledgerAccount.getParent() != null
                       ? loadLedgerAccount(ledgerAccount.getParent())
                       : null;
    }

    private Ledger getLedger(LedgerAccount ledgerAccount, LedgerAccount parentAccount) throws NotFoundException {
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

    @Override
    public Optional<LedgerAccount> findLedgerAccountById(String id) {
        return ledgerAccountRepository.findById(id);
    }

    @Override
    public Optional<LedgerAccount> findLedgerAccount(Ledger ledger, String name) {
        return ledgerAccountRepository
                       .findOptionalByLedgerAndName(ledger, name)
                       .map(l -> CloneUtils.cloneObject(l, LedgerAccount.class));
    }
}
