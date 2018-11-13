package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.postings.api.exception.*;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.postings.db.domain.*;
import de.adorsys.ledgers.postings.db.exception.PostingRepositoryException;
import de.adorsys.ledgers.postings.db.repository.*;
import de.adorsys.ledgers.postings.impl.converter.PostingLineMapper;
import de.adorsys.ledgers.postings.impl.converter.PostingMapper;
import de.adorsys.ledgers.util.CloneUtils;
import de.adorsys.ledgers.util.Ids;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostingServiceImpl extends AbstractServiceImpl implements PostingService {

    private final PostingRepository postingRepository;
    private final AccountStmtRepository accountStmtRepository;
    private final PostingMapper postingMapper;
    private final PostingLineRepository postingLineRepository;
    private final PostingLineMapper postingLineMapper;

    public PostingServiceImpl(LedgerAccountRepository ledgerAccountRepository,
                              ChartOfAccountRepository chartOfAccountRepo, Principal principal, LedgerRepository ledgerRepository,
                              PostingRepository postingRepository, AccountStmtRepository accountStmtRepository,
                              PostingMapper postingMapper, PostingLineRepository postingLineRepository,
                              PostingLineMapper postingLineMapper) {
        super(ledgerAccountRepository, chartOfAccountRepo, principal, ledgerRepository);
        this.postingRepository = postingRepository;
        this.accountStmtRepository = accountStmtRepository;
        this.postingMapper = postingMapper;
        this.postingLineRepository = postingLineRepository;
        this.postingLineMapper = postingLineMapper;
    }

    @Override
    public PostingBO newPosting(PostingBO postingBO) throws LedgerNotFoundException, LedgerAccountNotFoundException, BaseLineException, DoubleEntryAccountingException {
        Posting posting = postingMapper.toPosting(postingBO);
        posting = newPosting(posting);
        return postingMapper.toPostingBO(posting);
    }

    @Override
    public List<PostingBO> findPostingsByOperationId(String oprId) {
        return CloneUtils.cloneList(postingRepository.findByOprId(oprId), PostingBO.class);
    }

    @Override
    public List<PostingLineBO> findPostingsByDates(LedgerAccountBO ledgerAccount, LocalDateTime timeFrom, LocalDateTime timeTo) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        LedgerAccount account = loadLedgerAccount(ledgerAccount);
        return postingLineRepository.findByAccountAndPstTimeGreaterThanAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByPstTimeDesc(account, timeFrom, timeTo)
                       .stream()
                       .map(postingLineMapper::toPostingLineBO)
                       .collect(Collectors.toList());
    }

    @Override
    public PostingLineBO findPostingById(LedgerAccountBO ledgerAccount, String sourceId) throws LedgerAccountNotFoundException, LedgerNotFoundException, PostingNotFoundException {
        LedgerAccount account = loadLedgerAccount(ledgerAccount);
        return postingLineRepository.findFirstByAccountAndOprSrc(account, sourceId)
                       .map(postingLineMapper::toPostingLineBO)
                       .orElseThrow(() -> new PostingNotFoundException(String.format("Posting with %s could not be found", sourceId)));
    }

    private Posting newPosting(Posting posting) throws DoubleEntryAccountingException, BaseLineException, LedgerNotFoundException, LedgerAccountNotFoundException {
        LocalDateTime now = LocalDateTime.now();
        // check posting time is not before a closing.
        //		validatePostingTime(posting);
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
//		p.setHash(hash);
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
        p.setRecordUser(principal.getName());
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
    private void processPostingLine(Posting p, PostingLine model) throws LedgerAccountNotFoundException, LedgerNotFoundException, BaseLineException {
        PostingLine l = new PostingLine();
        l.setId(Ids.id());
        LedgerAccount account = loadLedgerAccount(model.getAccount());
        l.setAccount(account);
        String baseLine = validatePostingTime(p, account).orElse(new AccountStmt()).getId();
        l.setBaseLine(baseLine);
        l.setCreditAmount(model.getCreditAmount());
        l.setDebitAmount(model.getDebitAmount());
        l.setDetails(model.getDetails());
//		l.setPosting(p);
        l.setSrcAccount(model.getSrcAccount());
        p.getLines().add(l);

    }

    /**
     * Validate Double Entry Accounting. Make sure both total of debit an credit of the
     * given posting lines are equal.
     *
     * @param posting
     * @throws DoubleEntryAccountingException
     */
    private void validateDoubleEntryAccounting(Posting posting) throws DoubleEntryAccountingException {
        List<PostingLine> lines = posting.getLines();
        BigDecimal sumDebit = BigDecimal.ZERO;
        BigDecimal sumCredit = BigDecimal.ZERO;
        for (PostingLine line : lines) {
            sumDebit = sumDebit.add(line.getDebitAmount());
            sumCredit = sumCredit.add(line.getCreditAmount());
        }

        if (!sumDebit.equals(sumCredit)) {
            throw new DoubleEntryAccountingException(String.format("Debit summs up to %s while credit sums up to %s", sumDebit, sumCredit));
        }
    }

    /**
     * Checks if this account has a released financial statement by the given posting date.
     *
     * @param posting
     * @param ledgerAccount
     * @return
     * @throws BaseLineException
     * @throws PostingRepositoryException
     */
    private Optional<AccountStmt> validatePostingTime(Posting posting, LedgerAccount ledgerAccount) throws BaseLineException, PostingRepositoryException {
        // check posting time not null
        postingTimeNotNull(posting);

        // If any account statement closed after posting date, strike
        Optional<AccountStmt> stmtOpt = accountStmtRepository
                                                .findFirstByAccountAndStmtStatusAndPstTimeGreaterThanEqual(
                                                        ledgerAccount, StmtStatus.CLOSED, posting.getPstTime());

        if (stmtOpt.isPresent()) {
            throw new BaseLineException(String.format("posting time %s is before the last ledger closing %s", posting.getPstTime(), stmtOpt.get().getPstTime()));
        }

        return accountStmtRepository
                       .findFirstByAccountAndStmtStatusAndPstTimeLessThanOrderByPstTimeDescStmtSeqNbrDesc(
                               ledgerAccount, StmtStatus.CLOSED, posting.getPstTime());
    }

    private void postingTimeNotNull(Posting posting) {
        if (posting.getPstTime() == null) {
            throw new IllegalArgumentException("Missing posting time");
        }
    }

    private Optional<Posting> loadPredecessor(Posting current) {
        return postingRepository.findByOprIdAndDiscardingIdIsNull(current.getOprId());
    }


}
