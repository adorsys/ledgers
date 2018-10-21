package de.adorsys.ledgers.postings.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.postings.converter.LedgerAccountMapper;
import de.adorsys.ledgers.postings.converter.LedgerMapper;
import de.adorsys.ledgers.postings.db.domain.AccountCategory;
import de.adorsys.ledgers.postings.db.domain.BalanceSide;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.AccountCategoryBO;
import de.adorsys.ledgers.postings.domain.BalanceSideBO;
import de.adorsys.ledgers.postings.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.domain.LedgerBO;
import de.adorsys.ledgers.postings.exception.ChartOfAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.util.Ids;

@Service
public class LedgerServiceImpl extends AbstractServiceImpl implements LedgerService {

    private final LedgerMapper ledgerMapper;
    private final LedgerAccountMapper ledgerAccountMapper;

    public LedgerServiceImpl(LedgerMapper ledgerMapper, LedgerAccountMapper ledgerAccountMapper) {
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
    public LedgerAccountBO newLedgerAccount(LedgerAccountBO ledgerAccount) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        // Validations
        if (StringUtils.isBlank(ledgerAccount.getName())) {
            throw new IllegalArgumentException("Missing model name.");
        }

        // User
        LedgerAccount parentAccount = getParentAccount(ledgerAccount);
        Ledger ledger =  parentAccount!=null
        		? parentAccount.getLedger()
        				: loadLedger(ledgerAccount.getLedger());
        		
        AccountCategory category = ledgerAccount.getCategory()!=null
        		? AccountCategory.valueOf(ledgerAccount.getCategory().name())
        				: getAccountCategoryFromParent(parentAccount, ledgerAccount.getShortDesc());
        
        BalanceSide balanceSide = ledgerAccount.getBalanceSide()!=null
        		? BalanceSide.valueOf(ledgerAccount.getBalanceSide().name())
        				: getBalanceSide(parentAccount, category, ledgerAccount.getShortDesc());
        		
        String id = Ids.id();
		LocalDateTime created = LocalDateTime.now();
		String user = principal.getName();
		String shortDesc = ledgerAccount.getShortDesc();
		String longDesc = ledgerAccount.getLongDesc();
		String name = ledgerAccount.getName();
		LedgerAccount parent = parentAccount;
		ChartOfAccount coa = ledger.getCoa();
		LedgerAccount newLedgerAccount = new LedgerAccount(id, created, user, shortDesc, longDesc, name, ledger, parent, coa, balanceSide, category);
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
