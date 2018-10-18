package de.adorsys.ledgers.postings.service.impl;

import de.adorsys.ledgers.postings.converter.PostingMapper;
import de.adorsys.ledgers.postings.domain.*;
import de.adorsys.ledgers.postings.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.service.PostingService;
import de.adorsys.ledgers.postings.utils.DoubleEntryBookKeeping;
import de.adorsys.ledgers.postings.utils.LedgerPolicies;
import de.adorsys.ledgers.util.CloneUtils;
import de.adorsys.ledgers.util.Ids;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        LedgerAccount ledgerAccount = ledgerAccountMapper.toLedgerAccount(ledgerAccountBO);
        PostingLine baseLine = postingLineRepository
                                       .findFirstByAccountAndPstTypeAndPstStatusAndPstTimeLessThanEqualOrderByPstTimeDesc(
                                               ledgerAccount, PostingType.LDG_CLSNG, PostingStatus.POSTED, refTime);

        // Look for the youngest posting with the type PostingType.LDG_CLSNG
        List<PostingType> txTypes = Arrays.asList(PostingType.BUSI_TX, PostingType.ADJ_TX);
        List<BigDecimal> balance = postingLineRepository.computeBalance(ledgerAccount, txTypes, PostingStatus.POSTED, baseLine.getPstTime(), refTime);

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
                                  .lines(Collections.singletonList(postingLine))
                                  .build();

        return newPostingBOInternal(posting);
    }

    private PostingBO newPostingBOInternal(Posting posting) throws LedgerAccountNotFoundException, LedgerNotFoundException {
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
                            .recordAntecedentHash(antecedent.getHash())
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

        return postingMapper.toPostingBO(saved);

    }

}
