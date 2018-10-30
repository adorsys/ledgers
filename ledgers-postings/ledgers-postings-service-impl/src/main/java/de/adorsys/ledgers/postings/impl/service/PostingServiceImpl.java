package de.adorsys.ledgers.postings.impl.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    	PostingLine computedBalance = repoFctn.computeBalance(ledgerAccount, refTime);
//        
//        PostingLine baseLine = postingLineRepository
//                                       .findFirstByAccountAndPstTypeAndPstStatusAndPstTimeLessThanEqualOrderByPstTimeDesc(
//                                               ledgerAccount, PostingType.LDG_CLSNG, PostingStatus.POSTED, refTime);

        // Look for the youngest posting with the type PostingType.LDG_CLSNG
//        List<PostingType> txTypes = Arrays.asList(PostingType.BUSI_TX, PostingType.ADJ_TX);
//        List<BigDecimal> balance = repoFctn.computeBalance(ledgerAccount, txTypes, PostingStatus.POSTED, baseLine.getPstTime(), refTime);
//
//		LedgerAccount account = ledgerAccount;
//		BigDecimal debitAmount = baseLine.getDebitAmount().add(balance.get(0));
//		BigDecimal creditAmount = baseLine.getCreditAmount().add(balance.get(1));
//		String details = baseLine.getDetails();
//		String srcAccount = null;
//		PostingLine postingLine = new PostingLine(null, null, account, debitAmount, creditAmount, details, srcAccount);

		Posting posting = new Posting(null, null, null, Ids.id(), 0, refTime, null, null, refTime, PostingType.BAL_STMT, PostingStatus.POSTED, ledgerAccount.getLedger(), refTime, Collections.singletonList(computedBalance));

        return newPostingBOInternal(posting);
    }

    /*
     * Creating a new posting. While creating a new posting, we must watch over following:
     * - If the posting sequence number is higher than zero, it means that this posting is overriding another posting.
     * In this case, we must make sure that all account listed in the original posting are also available in the new posting.
     * Generally we will use the operation id and the (sequence number -1) to compute the id of the original posting. WE will
     * then load that original posting and carry any account not found in the new posting to the new posting with a debit and
     * credit value of zero.
     * - This approach will simplify computation of balances.
     * 
     */
    private PostingBO newPostingBOInternal(Posting posting) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        // Check ledger not null
    	if(posting.getLedger()==null) { 
    		throw insufficientInfo(posting);
    	}
        Ledger ledger = loadLedger(posting.getLedger());

        // check posting time is not before a closing.
        repoFctn.validatePostingTime(ledger, posting);

        // Load original posting. If the sequence number > 0, it means that there is a 
        // predecessor posting available.
        loadPredecessor(posting).ifPresent(posting::union);

        // Check the ledger
        DoubleEntryBookKeeping.validate(posting);

        // find last record.
        Posting antecedent = postingRepository.findFirstOptionalByLedgerOrderByRecordTimeDesc(posting.getLedger()).orElse(new Posting());

        List<PostingLine> postingLines = CloneUtils.cloneList(posting.getLines(), PostingLine.class);
        Posting p = new Posting(principal.getName(), antecedent.getId(), antecedent.getHash(), posting.getOprId(), posting.getOprSeqNbr(), 
        		posting.getOprTime(), posting.getOprType(), posting.getOprDetails(), posting.getPstTime(), posting.getPstType(), PostingStatus.POSTED, ledger, posting.getValTime(), null);

        Posting saved = postingRepository.save(p);

        // Validate existence of accounts and make sure they are all in the same ledger.
        for (PostingLine line : postingLines) {
            LedgerAccount ledgerAccount = loadLedgerAccount(line.getAccount());
            // Check account belongs to ledger.
            String baseLineId = repoFctn.validatePostingTime(ledger, posting, ledgerAccount).orElse(new PostingLine()).getId();

            PostingLine pl =new PostingLine(null, saved, ledgerAccount, line.getDebitAmount(), line.getCreditAmount(), line.getDetails(), line.getSrcAccount(), baseLineId); 
            postingLineRepository.save(pl);
        }

        String postingId = saved.getId();
        saved = postingRepository.findById(postingId)
                        .orElseThrow(() -> new IllegalStateException(postingId));

        saved = postingRepository.save(saved.hash());

        return postingMapper.toPostingBO(saved);

    }
    
    private Optional<Posting> loadPredecessor(Posting current){
    	return current.clonedId()
    			.map(postingRepository::findById)
    			.orElse(Optional.empty());
    }
}
