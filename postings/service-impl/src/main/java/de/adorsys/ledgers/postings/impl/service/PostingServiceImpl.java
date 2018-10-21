package de.adorsys.ledgers.postings.impl.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.Posting;
import de.adorsys.ledgers.postings.db.domain.PostingLine;
import de.adorsys.ledgers.postings.db.domain.PostingStatus;
import de.adorsys.ledgers.postings.db.domain.PostingType;
import de.adorsys.ledgers.postings.impl.converter.PostingMapper;
import de.adorsys.ledgers.postings.impl.utils.DoubleEntryBookKeeping;
import de.adorsys.ledgers.postings.impl.utils.LedgerPolicies;
import de.adorsys.ledgers.util.CloneUtils;
import de.adorsys.ledgers.util.Ids;

@Service
@Transactional
public class PostingServiceImpl extends AbstractServiceImpl implements PostingService {

    private final PostingMapper postingMapper;

    public PostingServiceImpl(PostingMapper postingMapper) {
        this.postingMapper = postingMapper;
    }

    @Override
	public PostingBO newPosting(PostingBO postingBO) throws LedgerNotFoundException, LedgerAccountNotFoundException {
        Posting posting = postingMapper.toPosting(postingBO);
        return newPostingBOInternal(posting);
    }

    @Override
    public List<PostingBO> findPostingsByOperationId(String oprId) {
        return CloneUtils.cloneList(postingRepository.findByOprId(oprId), PostingBO.class);
    }

    @Override
	public PostingBO balanceTx(LedgerAccountBO ledgerAccountBO, LocalDateTime refTime)
            throws LedgerAccountNotFoundException, LedgerNotFoundException {
    	LedgerAccount ledgerAccount = loadLedgerAccount(ledgerAccountBO);
        
        PostingLine baseLine = postingLineRepository
                                       .findFirstByAccountAndPstTypeAndPstStatusAndPstTimeLessThanEqualOrderByPstTimeDesc(
                                               ledgerAccount, PostingType.LDG_CLSNG, PostingStatus.POSTED, refTime);

        // Look for the youngest posting with the type PostingType.LDG_CLSNG
        List<PostingType> txTypes = Arrays.asList(PostingType.BUSI_TX, PostingType.ADJ_TX);
        List<BigDecimal> balance = postingLineRepository.computeBalance(ledgerAccount, txTypes, PostingStatus.POSTED, baseLine.getPstTime(), refTime);

		LedgerAccount account = ledgerAccount;
		BigDecimal debitAmount = baseLine.getDebitAmount().add(balance.get(0));
		BigDecimal creditAmount = baseLine.getCreditAmount().add(balance.get(1));
		String details = baseLine.getDetails();
		String srcAccount = null;
		PostingLine postingLine = new PostingLine(null, null, account, debitAmount, creditAmount, details, srcAccount);

		Posting posting = new Posting(null, null, null, Ids.id(), 0, refTime, null, null, refTime, PostingType.BAL_STMT, PostingStatus.POSTED, ledgerAccount.getLedger(), refTime, Collections.singletonList(postingLine));

        return newPostingBOInternal(posting);
    }

    private PostingBO newPostingBOInternal(Posting posting) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        // Check ledger not null
    	if(posting.getLedger()==null) { 
    		throw insufficientInfo(posting);
    	}
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
        Posting p = new Posting(principal.getName(), antecedent.getId(), antecedent.getHash(), posting.getOprId(), posting.getOprSeqNbr(), 
        		posting.getOprTime(), posting.getOprType(), posting.getOprDetails(), posting.getPstTime(), posting.getPstType(), PostingStatus.POSTED, ledger, posting.getValTime(), null);

        Posting saved = postingRepository.save(p);

        // Validate existence of accounts and make sure they are all in the same ledger.
        for (PostingLine line : postingLines) {
            LedgerAccount ledgerAccount = loadLedgerAccount(line.getAccount());
            // Check account belongs to ledger.
            ledgerPolicies.validateProperAccount(ledgerAccount);

            PostingLine pl =new PostingLine(null, saved, ledgerAccount, line.getDebitAmount(), line.getCreditAmount(), line.getDetails(), line.getSrcAccount()); 
            postingLineRepository.save(pl);
        }

        String postingId = saved.getId();
        saved = postingRepository.findById(postingId)
                        .orElseThrow(() -> new IllegalStateException(postingId));

        saved = postingRepository.save(saved.hash());

        return postingMapper.toPostingBO(saved);

    }

}
