package de.adorsys.ledgers.postings.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.domain.AccountCategory;
import de.adorsys.ledgers.postings.domain.BalanceSide;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.util.CloneUtils;
import de.adorsys.ledgers.util.Ids;

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
        ChartOfAccount coa = loadCoa(ledger.getCoa());
        LocalDateTime created = LocalDateTime.now();
        String user = principal.getName();
        Ledger newLedger = new Ledger(Ids.id(), created, user, ledger.getShortDesc(), ledger.getLongDesc(), ledger.getName(), coa, ledger.getLastClosing());
        Ledger savedLedger = ledgerRepository.save(newLedger);

        return CloneUtils.cloneObject(savedLedger, Ledger.class);
    }

    @Override
    public Optional<Ledger> findLedgerById(String id) {
        return Optional.ofNullable(CloneUtils.cloneObject(ledgerRepository.findById(id).orElse(null), Ledger.class));
    }

    @Override
    public Optional<Ledger> findLedgerByName(String name) {
        return Optional.ofNullable(CloneUtils.cloneObject(ledgerRepository.findOptionalByName(name).orElse(null), Ledger.class));
    }

    @Override
    public LedgerAccount newLedgerAccount(LedgerAccount ledgerAccount) throws NotFoundException {
        // Validations
        if (ledgerAccount.getName() == null) {
            throw new IllegalArgumentException("Missing model name.");
        }

        // User
        String user = principal.getName();

        LedgerAccount parentAccount = getParentAccount(ledgerAccount);

        Ledger ledger = getLedger(ledgerAccount, parentAccount);

        AccountCategory category = getAccountCategory(ledgerAccount, parentAccount);

        BalanceSide balanceSide = getBalanceSide(ledgerAccount, parentAccount, category);

        LedgerAccount newLedgerAccount = LedgerAccount.builder()
                                   .id(Ids.id())
                                   .created(LocalDateTime.now())
                                   .user(user)
                                   .shortDesc(ledgerAccount.getShortDesc())
                                   .longDesc(ledgerAccount.getLongDesc())
                                   .name(ledgerAccount.getName())
                                   .ledger(ledger)
                                   .coa(ledger.getCoa())
                                   .parent(parentAccount)
                                   .category(category)
                                   .balanceSide(balanceSide)
                                   .build();

        return CloneUtils.cloneObject(ledgerAccountRepository.save(newLedgerAccount), LedgerAccount.class);
    }

    private LedgerAccount getParentAccount(LedgerAccount ledgerAccount) throws NotFoundException {
        return ledgerAccount.getParent() != null ? loadLedgerAccount(ledgerAccount.getParent()) : null;
    }

    private Ledger getLedger(LedgerAccount ledgerAccount, LedgerAccount parentAccount) throws NotFoundException {
        Ledger ledger;
        if (ledgerAccount.getParent() != null) {
            ledger = parentAccount.getLedger();
        } else {
            ledger = loadLedger(ledgerAccount.getLedger());
        }
        return ledger;
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

        AccountCategory category;

        if (ledgerAccount.getCategory() != null) {
            category = ledgerAccount.getCategory();
        } else if (parentAccount != null) {
            category = parentAccount.getCategory();
        } else {
            throw new IllegalArgumentException("Missing category for: " + ledgerAccount.getShortDesc());
        }
        return category;
    }

    @Override
    public Optional<LedgerAccount> findLedgerAccountById(String id) {
        return ledgerAccountRepository.findById(id);
    }

    @Override
    public Optional<LedgerAccount> findLedgerAccount(Ledger ledger, String name) {
        LedgerAccount ledgerAccount = ledgerAccountRepository
                                              .findOptionalByLedgerAndName(ledger, name).orElse(null);
        return Optional.ofNullable(CloneUtils.cloneObject(ledgerAccount, LedgerAccount.class));
    }
}
