package de.adorsys.ledgers.postings.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.domain.PostingLine;
import de.adorsys.ledgers.postings.service.PostingService;
import de.adorsys.ledgers.postings.utils.CloneUtils;
import de.adorsys.ledgers.postings.utils.DoubleEntryBookKeeping;
import de.adorsys.ledgers.postings.utils.LedgerPolicies;
import de.adorsys.ledgers.postings.exception.NotFoundException;

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
		// Validate existence of accounts and make sure they are all in the same ledger.
		for (PostingLine line : postingLines) {
			LedgerAccount ledgerAccount = loadLedgerAccount(line.getAccount());
			// Check account belongs to ledger.
			ledgerPolicies.validateProperAccount(ledgerAccount);
			line.setAccount(ledgerAccount);
		}

		Posting p = Posting.builder()
			.ledger(ledger)
			.lines(postingLines)
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
		return CloneUtils.cloneObject(saved, Posting.class);
	}

	@Override
	public List<Posting> findPostingsByOperationId(String oprId) {
		return CloneUtils.cloneList(postingRepository.findByOprId(oprId), Posting.class);
	}
	
}
