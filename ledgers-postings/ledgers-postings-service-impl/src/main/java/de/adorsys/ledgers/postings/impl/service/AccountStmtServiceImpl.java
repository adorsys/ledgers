package de.adorsys.ledgers.postings.impl.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.postings.api.domain.AccountStmtBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.AccountStmtService;
import de.adorsys.ledgers.postings.db.domain.AccountStmt;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.PostingLine;
import de.adorsys.ledgers.postings.db.domain.PostingTrace;
import de.adorsys.ledgers.postings.db.domain.StmtStatus;
import de.adorsys.ledgers.postings.db.repository.AccountStmtRepository;
import de.adorsys.ledgers.postings.db.repository.PostingLineRepository;
import de.adorsys.ledgers.postings.impl.converter.AccountStmtMapper;
import de.adorsys.ledgers.util.Ids;

@Service
public class AccountStmtServiceImpl extends AbstractServiceImpl implements AccountStmtService {

	@Autowired
	private AccountStmtRepository accountStmtRepository;

	@Autowired
	private PostingLineRepository postingLineRepository;

	@Autowired
	private AccountStmtMapper accountStmtMapper;

	@Override
	public AccountStmtBO readStmt(LedgerAccountBO ledgerAccount, LocalDateTime refTime)
			throws LedgerAccountNotFoundException, LedgerNotFoundException, BaseLineException {
		return stmt(ledgerAccount, refTime, false);
	}

	@Override
	public AccountStmtBO createStmt(LedgerAccountBO ledgerAccount, LocalDateTime refTime)
			throws LedgerAccountNotFoundException, LedgerNotFoundException, BaseLineException {
		return stmt(ledgerAccount, refTime, true);
	}

	@Override
	public AccountStmtBO closeStmt(AccountStmtBO stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	public AccountStmtBO stmt(LedgerAccountBO ledgerAccount, LocalDateTime refTime, boolean store)
			throws LedgerAccountNotFoundException, LedgerNotFoundException, BaseLineException {
		LedgerAccount account = loadLedgerAccount(ledgerAccount);
		AccountStmt accStmt = accountStmtRepository
				.findFirstByAccountAndStmtStatusAndPstTimeLessThanOrderByPstTimeDescStmtSeqNbrDesc(account,
						StmtStatus.CLOSED, refTime)
				.orElse(null);

		// Load all posting associated with this base line.
		List<PostingLine> postingLines = accStmt == null || accStmt.getPosting() == null
				? postingLineRepository.findByAccountAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByRecordTimeDesc(account, refTime)
				: postingLineRepository.findByBaseLineAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByRecordTimeDesc(accStmt.getId(), refTime);
		if(accStmt==null) {
			accStmt = newStmtObj(refTime, account);
		}

		if (!postingLines.isEmpty()) {
			computeBalance(accStmt, postingLines);
			if(store) {
				// Create posting
				accountStmtRepository.save(accStmt);
			}
		}

		return toBo(accStmt);
	}

	private AccountStmt newStmtObj(LocalDateTime refTime, LedgerAccount account) {
		AccountStmt accStmt = new AccountStmt();
		accStmt.setAccount(account);
		accStmt.setPstTime(refTime);
		accStmt.setStmtSeqNbr(0);
		accStmt.setStmtStatus(StmtStatus.SIMULATED);
		accStmt.setTotalCredit(BigDecimal.ZERO);
		accStmt.setTotalDebit(BigDecimal.ZERO);
		return accStmt;
	}

	private AccountStmtBO toBo(AccountStmt accStmt) {
		return accountStmtMapper.toAccountStmtBO(accStmt);
	}

	private void computeBalance(final AccountStmt stmt, List<PostingLine> lines) {
		for (PostingLine line : lines) {
			addPostingLine(stmt, line);
		}
	}

	private void addPostingLine(final AccountStmt stmt, PostingLine line) {
		PostingTrace p = new PostingTrace();
		p.setAccount(stmt.getAccount());
		p.setCreditAmount(line.getCreditAmount());
		p.setDebitAmount(line.getDebitAmount());
		p.setId(Ids.id());
		p.setSrcOprId(line.getOprId());
		p.setSrcPstHash(line.getHash());
		p.setSrcPstTime(line.getPstTime());
		p.setTgtPstId(stmt.getId());// Match statement and corresponding posting.

		if (stmt.getYoungestPst() == null || stmt.getYoungestPst().getSrcPstTime().isBefore(p.getSrcPstTime())) {
			stmt.setYoungestPst(p);
		}
		stmt.setLatestPst(p);
		stmt.setTotalDebit(stmt.getTotalDebit().add(line.getDebitAmount()));
		stmt.setTotalCredit(stmt.getTotalCredit().add(line.getCreditAmount()));
	}
}
