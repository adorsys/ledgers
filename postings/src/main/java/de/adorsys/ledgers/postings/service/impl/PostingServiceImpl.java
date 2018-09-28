package de.adorsys.ledgers.postings.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.domain.PostingLine;
import de.adorsys.ledgers.postings.domain.PostingStatus;
import de.adorsys.ledgers.postings.domain.PostingType;
import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.service.PostingService;
import de.adorsys.ledgers.postings.utils.CloneUtils;
import de.adorsys.ledgers.postings.utils.DoubleEntryBookKeeping;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.postings.utils.LedgerPolicies;

@Service
@Transactional
public class PostingServiceImpl extends AbstractServiceImpl implements PostingService {
	
	@Override
	public Posting newPosting(Posting posting) throws NotFoundException {
		// Check ledger not null
		Ledger ledger = loadLedger(posting.getLedger());
		LedgerPolicies ledgerPolicies = new LedgerPolicies(ledger);

		// check posting time is not before a closing.
		ledgerPolicies.validatePostingTime(posting);
		
		// Check the ledger
		DoubleEntryBookKeeping.validate(posting);
		
		// find last record.
		Posting antecedent = postingRepository.findFirstOptionalByLedgerOrderByRecordTimeDesc(
				posting.getLedger()).orElse(new Posting());
		
		List<PostingLine> postingLines = CloneUtils.cloneList(posting.getLines(), PostingLine.class);

		Posting p = Posting.builder()
			.ledger(ledger)
			.oprDetails(posting.getOprDetails())
			.oprId(posting.getOprId())
			.oprSeqNbr(posting.getOprSeqNbr())
			.oprTime(posting.getOprTime())
			.oprType(posting.getOprType())
			.pstTime(posting.getPstTime())
			.pstType(posting.getPstType())
			.recordAntecedentHash(antecedent.getRecordHash())
			.recordAntecedentId(antecedent.getId())
			.recordUser(principal.getName())
			.valTime(posting.getValTime())
			.build();
		
		Posting saved = postingRepository.save(p);
		
		// Validate existence of accounts and make sure they are all in the same ledger.
		for (PostingLine line : postingLines) {
			LedgerAccount ledgerAccount = loadLedgerAccount(line.getAccount());
			// Check account belongs to ledger.
			ledgerPolicies.validateProperAccount(ledgerAccount);
			
			PostingLine pl = PostingLine.builder()
				.account(ledgerAccount)
				.debitAmount(line.getDebitAmount())
				.creditAmount(line.getCreditAmount())
				.details(line.getDetails())
				.srcAccount(line.getSrcAccount())
				.posting(saved).build();
			postingLineRepository.save(pl);
		}
		
		String postingId = saved.getId();
		saved = postingRepository.findById(postingId)
				.orElseThrow(() -> new IllegalStateException(postingId));
		
		saved = postingRepository.save(saved.hash());
				
		return CloneUtils.cloneObject(saved, Posting.class);
	}

	@Override
	public List<Posting> findPostingsByOperationId(String oprId) {
		return CloneUtils.cloneList(postingRepository.findByOprId(oprId), Posting.class);
	}

	@Override
	public Posting balanceTx(LedgerAccount ledgerAccount, LocalDateTime refTime) throws NotFoundException {
		PostingLine baseLine = postingLineRepository
				.findFirstByAccountAndPstTypeAndPstStatusAndPstTimeLessThanEqualOrderByPstTimeDesc(
						ledgerAccount,PostingType.LDG_CLSNG,PostingStatus.POSTED,refTime);

		// Look for the youngest posting with the type PostingType.LDG_CLSNG
		List<PostingType> txTypes = java.util.Arrays.asList(PostingType.BUSI_TX, PostingType.ADJ_TX);
		List<BigDecimal> balance = postingLineRepository.computeBalance(ledgerAccount, txTypes, PostingStatus.POSTED, baseLine.getPstTime(),refTime);
		
		Posting bp = baseLine.getPosting();
					
		PostingLine postingLine = PostingLine.builder()
			.account(ledgerAccount)
			.debitAmount(baseLine.getDebitAmount().add(balance.get(0)))
			.creditAmount(baseLine.getCreditAmount().add(balance.get(1)))
			.details(baseLine.getDetails())
			.build();
		
		Posting posting = Posting.builder()
			.ledger(bp.getLedger())
			.oprId(Ids.id())
			.oprTime(refTime)
			.pstStatus(PostingStatus.POSTED)
			.pstTime(refTime)
			.pstType(PostingType.BAL_STMT)
			.lines(Arrays.asList(postingLine))
			.build();
		
		return newPosting(posting);
	}
	
}
