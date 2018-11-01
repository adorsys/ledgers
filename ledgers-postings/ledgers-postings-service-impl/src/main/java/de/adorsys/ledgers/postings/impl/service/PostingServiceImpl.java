package de.adorsys.ledgers.postings.impl.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.DoubleEntryAccountingException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.postings.db.adapter.PostingRepositoryAdapter;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.Posting;
import de.adorsys.ledgers.postings.db.domain.PostingLine;
import de.adorsys.ledgers.postings.db.domain.PostingStatus;
import de.adorsys.ledgers.postings.db.domain.PostingType;
import de.adorsys.ledgers.postings.db.repository.PostingLineRepository;
import de.adorsys.ledgers.postings.db.repository.PostingRepository;
import de.adorsys.ledgers.postings.impl.converter.PostingLineMapper;
import de.adorsys.ledgers.postings.impl.converter.PostingMapper;
import de.adorsys.ledgers.util.CloneUtils;
import de.adorsys.ledgers.util.Ids;

@Service
@Transactional
public class PostingServiceImpl extends AbstractServiceImpl implements PostingService {

    @Autowired
    protected PostingLineRepository postingLineRepository;
    
    @Autowired
    protected PostingRepositoryAdapter postingRepositoryAdapter;

    @Autowired
    protected PostingRepository postingRepository;
	
    private final PostingMapper postingMapper;
    private final PostingLineMapper postingLineMapper;
    
    public PostingServiceImpl(PostingMapper postingMapper, PostingLineMapper postingLineMapper) {
        this.postingMapper = postingMapper;
        this.postingLineMapper = postingLineMapper;
    }

    @Override
    @SuppressWarnings("PMD.IdenticalCatchBranches")
	public PostingBO newPosting(PostingBO postingBO) throws LedgerNotFoundException, LedgerAccountNotFoundException, BaseLineException, DoubleEntryAccountingException {
        Posting posting = postingMapper.toPosting(postingBO);
    	if(posting.getLedger()==null) { 
    		throw insufficientInfo(posting);
    	}
        Ledger ledger = loadLedger(posting.getLedger());
        try {
			posting = postingRepositoryAdapter.newPosting(ledger, posting);
		} catch (de.adorsys.ledgers.postings.db.exception.DoubleEntryAccountingException e) {
			throw new DoubleEntryAccountingException(e.getMessage());
		} catch (de.adorsys.ledgers.postings.db.exception.BaseLineException e) {
			throw new BaseLineException(e.getMessage());
		}

        return postingMapper.toPostingBO(posting);
    }

    @Override
    public List<PostingBO> findPostingsByOperationId(String oprId) {
        return CloneUtils.cloneList(postingRepository.findByOprId(oprId), PostingBO.class);
    }

    @Override
    @SuppressWarnings("PMD.IdenticalCatchBranches")
	public PostingBO balanceTx(LedgerAccountBO ledgerAccountBO, LocalDateTime refTime)
            throws LedgerAccountNotFoundException, LedgerNotFoundException, BaseLineException{
    	LedgerAccount ledgerAccount = loadLedgerAccount(ledgerAccountBO);
    	PostingLine computedBalance = postingRepositoryAdapter.computeBalance(ledgerAccount, refTime);
		Posting posting = new Posting(null, null, null, Ids.id(), 0, refTime, null, null, refTime, PostingType.BAL_STMT, PostingStatus.POSTED, ledgerAccount.getLedger(), refTime, Collections.singletonList(computedBalance));

		try {
			posting = postingRepositoryAdapter.newPosting(ledgerAccount.getLedger(), posting);
		} catch (de.adorsys.ledgers.postings.db.exception.DoubleEntryAccountingException e) {
			throw new IllegalStateException(e.getMessage());// Shall not happe
		} catch (de.adorsys.ledgers.postings.db.exception.BaseLineException e) {
			throw new BaseLineException(e.getMessage());
		}

        return postingMapper.toPostingBO(posting);
    }

    @Override
	public PostingLineBO computeBalance(LedgerAccountBO ledgerAccountBO, LocalDateTime refTime)
			throws LedgerAccountNotFoundException, LedgerNotFoundException {
    	LedgerAccount ledgerAccount = loadLedgerAccount(ledgerAccountBO);
    	PostingLine computedBalance = postingRepositoryAdapter.computeBalance(ledgerAccount, refTime);
    	return postingLineMapper.toPostingLineBO(computedBalance);
	}
}
