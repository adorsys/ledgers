package de.adorsys.ledgers.postings.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.domain.PostingLine;
import de.adorsys.ledgers.postings.service.PostingService;
import de.adorsys.ledgers.postings.utils.CloneUtils;
import de.adorsys.ledgers.postings.utils.DoubleEntryBookKeeping;
import de.adorsys.ledgers.postings.utils.Ids;

@Service
@Transactional
public class PostingServiceImpl extends AbstractServiceImpl implements PostingService {
	
	@Override
	public Posting newPosting(Posting posting) {
		// TODO
		// check posting time is not before a closing.
		
		// Check the ledger
		DoubleEntryBookKeeping.validate(posting);
				
		// Reference date
		LocalDateTime pstTime = posting.getPstTime();
		
		// Check ledger not null
		Ledger ledger = loadLedger(posting.getLedger());
		
		// Validate existence of accounts and make sure they are all in the same ledger.
		List<PostingLine> lines = posting.getLines();
		for (PostingLine line : lines) {
			Optional<LedgerAccount> accountOptions = ledgerAccountRepository
					.findFirstOptionalByLedgerAndNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(
							ledger, line.getAccount(), pstTime, pstTime);
			
			if(!accountOptions.isPresent()){
				// How do we proceed with missing accounts.
			}
			
			LedgerAccount ledgerAccount = accountOptions.get();

			// Check account belongs to ledger.
			if(ledgerAccount.getLedger().equals(posting.getLedger())){
				// How do we proceed with invalide accounts.
			}
		}
		
		// find last record.
		Posting antecedent = postingRepository.findFirstOptionalByLedgerOrderByRecordTimeDesc(
				posting.getLedger()).orElse(new Posting());
		

		List<PostingLine> postingLines = CloneUtils.cloneList(posting.getLines(), PostingLine.class);
		Posting p = Posting.builder()
			.id(Ids.id())
			.ledger(posting.getLedger())
			.lines(postingLines)
			.oprDetails(posting.getOprDetails())
			.oprId(posting.getOprId())
			.oprTime(posting.getOprTime())
			.oprType(posting.getOprType())
			.pstTime(posting.getPstTime())
			.pstType(posting.getPstType())
			.recordAntecedentHash(antecedent.getRecordHash())
			.recordAntecedentId(antecedent.getId())
			.recordTime(posting.getRecordTime())
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
