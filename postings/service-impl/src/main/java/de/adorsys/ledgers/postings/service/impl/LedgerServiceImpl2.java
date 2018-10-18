package de.adorsys.ledgers.postings.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import de.adorsys.ledgers.postings.domain.AccountCategory;
import de.adorsys.ledgers.postings.domain.BalanceSide;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.LedgerBO;
import de.adorsys.ledgers.postings.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.util.CloneUtils;
import de.adorsys.ledgers.util.Ids;

public class LedgerServiceImpl2 extends AbstractServiceImpl implements LedgerService {

	@Override
	public LedgerBO newLedger(LedgerBO ledger) throws LedgerNotFoundException {
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

        return CloneUtils.cloneObject(savedLedger, LedgerBO.class);
	}

	@Override
	public Optional<LedgerBO> findLedgerById(String id) {
        return ledgerRepository.findById(id)
                .map(l -> CloneUtils.cloneObject(l, LedgerBO.class));
	}

	@Override
	public Optional<LedgerBO> findLedgerByName(String name) {
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

        return CloneUtils.cloneObject(ledgerAccountRepository.save(newLedgerAccount), LedgerBO.class);
	}

	@Override
	public LedgerAccount newLedgerAccount(LedgerAccount ledgerAccount) throws LedgerAccountNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<LedgerAccount> findLedgerAccountById(String id) {
        return ledgerAccountRepository.findById(id);
	}

	@Override
	public Optional<LedgerAccount> findLedgerAccount(LedgerBO ledger, String name) {
        return ledgerAccountRepository
                .findOptionalByLedgerAndName(ledger, name)
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
}
