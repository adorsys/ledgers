package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.domain.NamedBO;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.db.domain.*;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.impl.converter.LedgerMapper;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.exception.PostingErrorCode;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.util.exception.PostingErrorCode.NO_CATEGORY;

@Service
public class LedgerServiceImpl extends AbstractServiceImpl implements LedgerService {
    private final LedgerMapper ledgerMapper = Mappers.getMapper(LedgerMapper.class);

    public LedgerServiceImpl(LedgerAccountRepository ledgerAccountRepository, ChartOfAccountRepository chartOfAccountRepo, LedgerRepository ledgerRepository) {
        super(ledgerAccountRepository, chartOfAccountRepo, ledgerRepository);
    }

    @Override
    public LedgerBO newLedger(LedgerBO ledger) {
        Ledger newLedger = new Ledger(
                Ids.id(),
                LocalDateTime.now(),
                ledger.getName(),
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
    public LedgerAccountBO newLedgerAccount(LedgerAccountBO ledgerAccount, String userName) {
        // Validations
        if (StringUtils.isBlank(ledgerAccount.getName())) {
            throw PostingModuleException.builder()
                          .errorCode(PostingErrorCode.NOT_ENOUGH_INFO)
                          .devMsg("Missing model name.")
                          .build();
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
        String shortDesc = ledgerAccount.getShortDesc();
        String longDesc = ledgerAccount.getLongDesc();
        String name = ledgerAccount.getName();
        ChartOfAccount coa = ledger.getCoa();
        LedgerAccount newLedgerAccount = new LedgerAccount(id, created, userName, shortDesc, longDesc, name, ledger, parentAccount, coa, balanceSide, category);
        return ledgerAccountMapper.toLedgerAccountBO(ledgerAccountRepository.save(newLedgerAccount));
    }

    @Override
    public LedgerAccountBO findLedgerAccountById(String id) {
        return ledgerAccountRepository.findById(id)
                       .map(ledgerAccountMapper::toLedgerAccountBO)
                       .orElseThrow(() -> PostingModuleException.builder()
                                                  .errorCode(PostingErrorCode.LEDGER_ACCOUNT_NOT_FOUND)
                                                  .devMsg(String.format(LA_NF_BY_NAME_MSG, id))
                                                  .build());
    }

    @Override
    public LedgerAccountBO findLedgerAccount(LedgerBO ledgerBO, String name) {
        Ledger ledger = ledgerMapper.toLedger(ledgerBO);
        return ledgerAccountRepository
                       .findOptionalByLedgerAndName(loadLedger(ledger), name)
                       .map(ledgerAccountMapper::toLedgerAccountBO)
                       .orElseThrow(() -> PostingModuleException.builder()
                                                  .errorCode(PostingErrorCode.LEDGER_ACCOUNT_NOT_FOUND)
                                                  .devMsg(String.format(LA_NF_BY_NAME_MSG, name))
                                                  .build());
    }

    @Override
    public boolean checkIfLedgerAccountExist(LedgerBO ledgerBO, String name) {
        try {
            Ledger ledger = loadLedger(ledgerBO);
            return ledgerAccountRepository
                           .findOptionalByLedgerAndName(ledger, name)
                           .isPresent();
        } catch (PostingModuleException e) {
            return false;
        }
    }

    @Override
    public Map<String, LedgerAccountBO> finLedgerAccountsByIbans(Set<String> ibans, LedgerBO ledgerBO) {
        Ledger ledger = loadLedger(ledgerBO);
        return ledgerAccountMapper.toLedgerAccountsBO(ledgerAccountRepository.getAccountsByIbans(ibans, ledger)).stream()
                       .collect(Collectors.toMap(NamedBO::getName, Function.identity()));
    }

    private LedgerAccount getParentAccount(LedgerAccountBO ledgerAccount) {
        return ledgerAccount.getParent() != null
                       ? loadLedgerAccountBO(ledgerAccount.getParent())
                       : null;
    }

    private BalanceSide getBalanceSide(LedgerAccount parentAccount, AccountCategory category, String shortDesc) {
        BalanceSide balanceSide;
        if (parentAccount != null) {
            balanceSide = parentAccount.getBalanceSide();
        } else if (category != null) {
            balanceSide = category.getDefaultBs();
        } else {
            throw getNoCategoryException(shortDesc);
        }
        return balanceSide;
    }

    private AccountCategory getAccountCategoryFromParent(LedgerAccount parentAccount, String shortDescription) {
        return Optional.ofNullable(parentAccount)
                       .map(LedgerAccount::getCategory)
                       .orElseThrow(() -> getNoCategoryException(shortDescription));
    }

    private PostingModuleException getNoCategoryException(String variable) {
        return PostingModuleException.builder()
                       .errorCode(NO_CATEGORY)
                       .devMsg(String.format("Missing category for: %s", variable))
                       .build();
    }
}
