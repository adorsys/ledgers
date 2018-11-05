package de.adorsys.ledgers.postings.impl.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.DoubleEntryAccountingException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.postings.db.domain.AccountStmt;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.Posting;
import de.adorsys.ledgers.postings.db.domain.PostingLine;
import de.adorsys.ledgers.postings.db.domain.StmtStatus;
import de.adorsys.ledgers.postings.db.exception.PostingRepositoryException;
import de.adorsys.ledgers.postings.db.repository.AccountStmtRepository;
import de.adorsys.ledgers.postings.db.repository.PostingRepository;
import de.adorsys.ledgers.postings.impl.converter.PostingMapper;
import de.adorsys.ledgers.util.CloneUtils;
import de.adorsys.ledgers.util.Ids;

@Service
@Transactional
public class PostingServiceImpl extends AbstractServiceImpl implements PostingService {

    @Autowired
    private PostingRepository postingRepository;
    
    @Autowired
    private AccountStmtRepository accountStmtRepository;
	
    private final PostingMapper postingMapper;
    
    public PostingServiceImpl(PostingMapper postingMapper) {
        this.postingMapper = postingMapper;
    }

    @Override
    @SuppressWarnings("PMD.IdenticalCatchBranches")
	public PostingBO newPosting(PostingBO postingBO) throws LedgerNotFoundException, LedgerAccountNotFoundException, BaseLineException, DoubleEntryAccountingException {
        Posting posting = postingMapper.toPosting(postingBO);
        posting = newPosting(posting);
        return postingMapper.toPostingBO(posting);
    }

    @Override
    public List<PostingBO> findPostingsByOperationId(String oprId) {
        return CloneUtils.cloneList(postingRepository.findByOprId(oprId), PostingBO.class);
    }
    

	private Posting newPosting(Posting posting) throws DoubleEntryAccountingException, BaseLineException, LedgerNotFoundException, LedgerAccountNotFoundException {
		LocalDateTime now = LocalDateTime.now();
		// check posting time is not before a closing.
		//		validatePostingTime(posting);
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
		
		Ledger ledger = loadLedger(posting.getLedger());
		p.setLedger(ledger);
		


		// Load original posting. If the sequence number > 0, it means that there is a
		// predecessor posting available.
		//		p.setDiscardedId(discardedId);
		//		p.setDiscardedTime(discardedTime);
		//		p.setDiscardingId(discardingId);
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
	
	private void discardPosting(final Posting discarded, final Posting discarding) {
		discarded.setDiscardedTime(discarding.getRecordTime());
		discarded.setDiscardingId(discarding.getId());
		discarded.synchLines();
		postingRepository.save(discarded);
		discarding.setDiscardedId(discarded.getId());
	}
	
	/*
	 * Process Posting lines withoug sting the posting.
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
	 * @param ledger
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
	
	private void postingTimeNotNull(Posting posting)  {
		if (posting.getPstTime() == null) {
			throw new IllegalArgumentException("Missing posting time");
		}
	}
	
	private Optional<Posting> loadPredecessor(Posting current) {
		return postingRepository.findByOprIdAndDiscardingIdIsNull(current.getOprId());
	}


}
