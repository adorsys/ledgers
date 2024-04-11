/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.postings.db.domain.*;
import de.adorsys.ledgers.postings.db.repository.*;
import de.adorsys.ledgers.postings.impl.converter.PostingLineMapper;
import de.adorsys.ledgers.postings.impl.converter.PostingMapper;
import de.adorsys.ledgers.util.CloneUtils;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.util.exception.PostingErrorCode.*;

@Slf4j
@Service
public class PostingServiceImpl extends AbstractServiceImpl implements PostingService {

    private static final String DOUBLE_ENTRY_ERROR_MSG = "Debit sums up to %s while credit sums up to %s";
    private static final String POSTING_NF_MSG = "Posting with account id %s  and transaction id %s could not be found";
    private static final String BASE_LINE_TIME_ERROR_MSG = "posting time %s is before the last ledger closing %s";

    private final PostingRepository postingRepository;
    private final AccountStmtRepository accountStmtRepository;
    private final PostingLineRepository postingLineRepository;
    private final PostingMapper postingMapper = Mappers.getMapper(PostingMapper.class);
    private final PostingLineMapper postingLineMapper = Mappers.getMapper(PostingLineMapper.class);

    public PostingServiceImpl(LedgerAccountRepository ledgerAccountRepository,
                              ChartOfAccountRepository chartOfAccountRepo, LedgerRepository ledgerRepository,
                              PostingRepository postingRepository, AccountStmtRepository accountStmtRepository,
                              PostingLineRepository postingLineRepository) {
        super(ledgerAccountRepository, chartOfAccountRepo, ledgerRepository);
        this.postingRepository = postingRepository;
        this.accountStmtRepository = accountStmtRepository;
        this.postingLineRepository = postingLineRepository;
    }

    @Override
    public PostingBO newPosting(PostingBO postingBO) {
        Posting posting = postingMapper.toPosting(postingBO);
        posting = newPosting(posting);
        return postingMapper.toPostingBO(posting);
    }

    @Override
    public List<PostingBO> findPostingsByOperationId(String oprId) {
        return CloneUtils.cloneList(postingRepository.findByOprId(oprId), PostingBO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostingLineBO> findPostingsByDates(LedgerAccountBO ledgerAccount, LocalDateTime timeFrom, LocalDateTime timeTo) {
        LedgerAccount account = loadLedgerAccountBO(ledgerAccount);
        return postingLineRepository.findByAccountAndPstTimeGreaterThanAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByPstTimeDesc(account, timeFrom, timeTo)
                       .stream()
                       .map(postingLineMapper::toPostingLineBO)
                       .collect(Collectors.toList());
    }

    @Override
    public Page<PostingLineBO> findPostingsByDatesPaged(LedgerAccountBO ledgerAccount, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        LedgerAccount account = loadLedgerAccountBO(ledgerAccount);
        return postingLineRepository.findPostingsByAccountAndDates(account, dateFrom, dateTo, pageable)
                       .map(postingLineMapper::toPostingLineBO);
    }

    @Override
    public PostingLineBO findPostingLineById(LedgerAccountBO ledgerAccount, String transactionId) {
        LedgerAccount account = loadLedgerAccountBO(ledgerAccount);
        return postingLineRepository.findFirstByIdAndAccount(transactionId, account)
                       .map(postingLineMapper::toPostingLineBO)
                       .orElseThrow(() -> PostingModuleException.builder()
                                                  .errorCode(POSTING_NOT_FOUND)
                                                  .devMsg(String.format(POSTING_NF_MSG, account.getId(), transactionId))
                                                  .build());
    }

    private Posting newPosting(Posting posting) {
        LocalDateTime now = LocalDateTime.now();
        // check posting time is not before a closing.
        //NOSONAR		validatePostingTime(posting);
        Posting p = createPostingObj(posting, now);
        Ledger ledger = loadLedger(posting.getLedger());
        p.setLedger(ledger);


        // Load original posting. If the sequence number > 0, it means that there is a
        // predecessor posting available.
        loadPredecessor(p).ifPresent(discarded -> discardPosting(discarded, p));

        // Check double entry accounting
        validateDoubleEntryAccounting(posting);

        // find last record for hash
        Posting antecedent = postingRepository.findFirstByLedgerOrderByRecordTimeDesc(posting.getLedger()).orElse(new Posting());
        p.setAntecedentHash(antecedent.getHash());
        p.setAntecedentId(antecedent.getId());

        // Process posting line without setting posting.
        for (PostingLine pl : posting.getLines()) {
            processPostingLine(p, pl);
        }

        // compute hash.
        p.hash();

        p.synchLines();
        return postingRepository.save(p);
    }

    private Posting createPostingObj(Posting posting, LocalDateTime now) {
        Posting p = new Posting();
//NOSONAR		p.setHash(hash);
        p.setId(Ids.id());
        p.setOprDetails(posting.getOprDetails());
        p.setOprId(posting.getOprId());
        p.setOprSrc(posting.getOprSrc());
        p.setOprTime(posting.getOprTime());
        p.setOprType(posting.getOprType());
        p.setPstStatus(posting.getPstStatus());
        p.setPstTime(posting.getPstTime());
        p.setPstType(posting.getPstType());
        p.setRecordTime(now);
        p.setRecordUser(posting.getRecordUser());
        p.setValTime(posting.getValTime());
        return p;
    }

    private void discardPosting(final Posting discarded, final Posting discarding) {
        discarded.setDiscardedTime(discarding.getRecordTime());
        discarded.setDiscardingId(discarding.getId());
        discarded.synchLines();
        postingRepository.save(discarded);
        discarding.setDiscardedId(discarded.getId());
    }

    /*
     * Process Posting lines without sting the posting.
     */
    private void processPostingLine(Posting p, PostingLine postingLine) {
        PostingLine l = new PostingLine();
        l.setId(postingLine.getId());
        LedgerAccount account = loadLedgerAccount(postingLine.getAccount());
        l.setAccount(account);
        String baseLine = validatePostingTime(p, account).orElse(new AccountStmt()).getId();
        l.setBaseLine(baseLine);
        l.setCreditAmount(postingLine.getCreditAmount());
        l.setDebitAmount(postingLine.getDebitAmount());
        l.setDetails(postingLine.getDetails());
        l.setSrcAccount(postingLine.getSrcAccount());
        l.setSubOprSrcId(postingLine.getSubOprSrcId());
        p.getLines().add(l);
    }

    /**
     * Validate Double Entry Accounting. Make sure both total of debit an credit of the
     * given posting lines are equal.
     *
     * @param posting posting
     */
    private void validateDoubleEntryAccounting(Posting posting) {
        List<PostingLine> lines = posting.getLines();
        BigDecimal sumDebit = BigDecimal.ZERO;
        BigDecimal sumCredit = BigDecimal.ZERO;
        for (PostingLine line : lines) {
            sumDebit = sumDebit.add(line.getDebitAmount());
            sumCredit = sumCredit.add(line.getCreditAmount());
        }

        if (!sumDebit.equals(sumCredit)) {
            log.error(String.format(DOUBLE_ENTRY_ERROR_MSG, sumDebit, sumCredit));
            throw PostingModuleException.builder()
                          .errorCode(DOBLE_ENTRY_ERROR)
                          .devMsg(String.format(DOUBLE_ENTRY_ERROR_MSG, sumDebit, sumCredit))
                          .build();
        }
    }

    /**
     * Checks if this account has a released financial statement by the given posting date.
     *
     * @param posting posting
     * @param ledgerAccount ledger account
     * @return Optional of AccountStatement
     */
    private Optional<AccountStmt> validatePostingTime(Posting posting, LedgerAccount ledgerAccount) {
        // check posting time not null
        postingTimeNotNull(posting);

        // If any account statement closed after posting date, strike
        Optional<AccountStmt> stmtOpt = accountStmtRepository
                                                .findFirstByAccountAndStmtStatusAndPstTimeGreaterThanEqual(
                                                        ledgerAccount, StmtStatus.CLOSED, posting.getPstTime());

        if (stmtOpt.isPresent()) {
            throw PostingModuleException.builder()
                          .errorCode(BASE_LINE_TIME_ERROR)
                          .devMsg(String.format(BASE_LINE_TIME_ERROR_MSG, posting.getPstTime(), stmtOpt.get().getPstTime()))
                          .build();
        }

        return accountStmtRepository
                       .findFirstByAccountAndStmtStatusAndPstTimeLessThanOrderByPstTimeDescStmtSeqNbrDesc(
                               ledgerAccount, StmtStatus.CLOSED, posting.getPstTime());
    }

    private void postingTimeNotNull(Posting posting) {
        if (posting.getPstTime() == null) {
            throw PostingModuleException.builder()
                          .errorCode(POSTING_TIME_MISSING)
                          .devMsg("Missing posting time")
                          .build();
        }
    }

    private Optional<Posting> loadPredecessor(Posting current) {
        return postingRepository.findByOprIdAndDiscardingIdIsNull(current.getOprId());
    }
}
