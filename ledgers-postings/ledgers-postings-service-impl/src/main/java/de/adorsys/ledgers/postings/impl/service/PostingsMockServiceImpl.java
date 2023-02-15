/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import de.adorsys.ledgers.postings.api.service.PostingMockService;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.Posting;
import de.adorsys.ledgers.postings.db.domain.PostingLine;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.db.repository.PostingRepository;
import de.adorsys.ledgers.postings.impl.converter.PostingMapper;
import de.adorsys.ledgers.util.Ids;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static de.adorsys.ledgers.util.exception.PostingErrorCode.DOBLE_ENTRY_ERROR;

@Slf4j
@Service
public class PostingsMockServiceImpl extends AbstractServiceImpl implements PostingMockService {
    private static final String DOBLE_ENTRY_ERROR_MSG = "Debit sums up to %s while credit sums up to %s";
    private static final int NANO_TO_SECOND = 1000000000;
    private final PostingMapper postingMapper = Mappers.getMapper(PostingMapper.class);
    private final PostingRepository postingRepository;

    public PostingsMockServiceImpl(LedgerAccountRepository ledgerAccountRepository, ChartOfAccountRepository chartOfAccountRepo, LedgerRepository ledgerRepository, PostingRepository postingRepository) {
        super(ledgerAccountRepository, chartOfAccountRepo, ledgerRepository);
        this.postingRepository = postingRepository;
    }

    @Override
    public void addPostingsAsBatch(List<PostingBO> postingsBO) {
        long start = System.nanoTime();
        List<Posting> postings = postingMapper.toPostingList(postingsBO);
        Ledger ledger = loadLedger(postings.get(0).getLedger());
        Map<String, LedgerAccount> accountMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        postings.forEach(p -> performPostingUpdate(ledger, accountMap, now, p));
        log.info("Reformatting postings in Posting Service in {} seconds.", (double) (System.nanoTime() - start) / NANO_TO_SECOND);
        CompletableFuture.runAsync(() -> postingRepository.saveAll(postings));
    }

    private void performPostingUpdate(Ledger ledger, Map<String, LedgerAccount> accountMap, LocalDateTime now, Posting posting) {
        posting.setId(Ids.id());
        posting.setRecordTime(now);
        posting.setLedger(ledger);
        validateDoubleEntryAccounting(posting);
        posting.setAntecedentHash("NO HASH - MOCKED TRANSACTION");
        posting.setAntecedentId("NO ID - MOCKED TRANSACTION");
        posting.getLines().forEach(l -> l.setAccount(resolveAccount(accountMap, l)));
        posting.hash();
        posting.synchLines();
    }

    @NotNull
    private LedgerAccount resolveAccount(Map<String, LedgerAccount> accountMap, PostingLine line) {
        return Optional.ofNullable(accountMap.get(line.getAccount().getId()))
                       .orElseGet(() -> {
                           LedgerAccount account = loadLedgerAccount(line.getAccount());
                           accountMap.put(account.getId(), account);
                           return account;
                       });
    }


    private void validateDoubleEntryAccounting(Posting posting) {
        List<PostingLine> lines = posting.getLines();
        BigDecimal sumDebit = BigDecimal.ZERO;
        BigDecimal sumCredit = BigDecimal.ZERO;
        for (PostingLine line : lines) {
            sumDebit = sumDebit.add(line.getDebitAmount());
            sumCredit = sumCredit.add(line.getCreditAmount());
        }

        if (!sumDebit.equals(sumCredit)) {
            throw PostingModuleException.builder()
                          .errorCode(DOBLE_ENTRY_ERROR)
                          .devMsg(String.format(DOBLE_ENTRY_ERROR_MSG, sumDebit, sumCredit))
                          .build();
        }
    }
}
