package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.AccountStmtBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.AccountStmtService;
import de.adorsys.ledgers.postings.db.domain.*;
import de.adorsys.ledgers.postings.db.repository.*;
import de.adorsys.ledgers.postings.impl.converter.AccountStmtMapper;
import de.adorsys.ledgers.util.Ids;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountStmtServiceImpl extends AbstractServiceImpl implements AccountStmtService {

    private AccountStmtRepository accountStmtRepository;

    private PostingLineRepository postingLineRepository;

    private AccountStmtMapper accountStmtMapper;

    public AccountStmtServiceImpl(LedgerAccountRepository ledgerAccountRepository, ChartOfAccountRepository chartOfAccountRepo, LedgerRepository ledgerRepository, AccountStmtRepository accountStmtRepository, PostingLineRepository postingLineRepository, AccountStmtMapper accountStmtMapper) {
        super(ledgerAccountRepository, chartOfAccountRepo, ledgerRepository);
        this.accountStmtRepository = accountStmtRepository;
        this.postingLineRepository = postingLineRepository;
        this.accountStmtMapper = accountStmtMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountStmtBO readStmt(LedgerAccountBO ledgerAccount, LocalDateTime refTime) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        AccountStmt stmt = stmt(ledgerAccount, refTime);
        return accountStmtMapper.toAccountStmtBO(stmt);
    }

    @Override
    public AccountStmtBO createStmt(LedgerAccountBO ledgerAccount, LocalDateTime refTime) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        AccountStmt stmt = stmt(ledgerAccount, refTime);
        stmt = accountStmtRepository.save(stmt);
        return accountStmtMapper.toAccountStmtBO(stmt);
    }

    @Override
    public AccountStmtBO closeStmt(AccountStmtBO stmt) {
        //TODO @fpo Auto-generated method stub
        return null;
    }

    private AccountStmt stmt(LedgerAccountBO ledgerAccount, LocalDateTime refTime) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        LedgerAccount account = loadLedgerAccount(ledgerAccount);
        AccountStmt accStmt = accountStmtRepository
                                      .findFirstByAccountAndStmtStatusAndPstTimeLessThanOrderByPstTimeDescStmtSeqNbrDesc(account, StmtStatus.CLOSED, refTime)
                                      .orElseGet(() -> newStmtObj(refTime, account));


        // Load all posting associated with this base line.
        List<PostingLine> postingLines = accStmt.getPosting() == null
                                                 ? postingLineRepository.findByAccountAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByRecordTimeDesc(account, refTime)
                                                 : postingLineRepository.findByBaseLineAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByRecordTimeDesc(accStmt.getId(), refTime);

        return computeBalance(accStmt, postingLines);
    }

    private AccountStmt newStmtObj(LocalDateTime refTime, LedgerAccount account) {
        AccountStmt accStmt = new AccountStmt();
        accStmt.setId(Ids.id());
        accStmt.setAccount(account);
        accStmt.setPstTime(refTime);
        accStmt.setStmtSeqNbr(0);
        accStmt.setStmtStatus(StmtStatus.SIMULATED);
        accStmt.setTotalCredit(BigDecimal.ZERO);
        accStmt.setTotalDebit(BigDecimal.ZERO);
        return accStmt;
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    private AccountStmt computeBalance(AccountStmt stmt, List<PostingLine> lines) {
        if (!lines.isEmpty()) {
            for (PostingLine line : lines) {
                stmt = refreshStatement(stmt, line);
            }
        }
        return stmt;
    }

    private AccountStmt refreshStatement(AccountStmt stmt, PostingLine line) {
        PostingTrace p = createPostingTrace(stmt, line);// Match statement and corresponding posting.

        if (stmt.getYoungestPst() == null
                    || stmt.getYoungestPst().getSrcPstTime().isBefore(p.getSrcPstTime())) {
            stmt.setYoungestPst(p);
        }
        stmt.setLatestPst(p);
        stmt.setTotalDebit(stmt.getTotalDebit().add(line.getDebitAmount()));
        stmt.setTotalCredit(stmt.getTotalCredit().add(line.getCreditAmount()));
        return stmt;
    }

    private PostingTrace createPostingTrace(AccountStmt stmt, PostingLine line) {
        PostingTrace p = new PostingTrace();
        p.setAccount(stmt.getAccount());
        p.setCreditAmount(line.getCreditAmount());
        p.setDebitAmount(line.getDebitAmount());
        p.setId(Ids.id());
        p.setSrcOprId(line.getOprId());
        p.setSrcPstHash(line.getHash());
        p.setSrcPstTime(line.getPstTime());
        p.setTgtPstId(stmt.getId());
        return p;
    }
}
