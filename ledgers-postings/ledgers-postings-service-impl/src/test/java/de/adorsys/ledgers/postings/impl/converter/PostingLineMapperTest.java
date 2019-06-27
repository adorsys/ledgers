package de.adorsys.ledgers.postings.impl.converter;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.postings.api.domain.PostingStatusBO;
import de.adorsys.ledgers.postings.api.domain.PostingTypeBO;
import de.adorsys.ledgers.postings.db.domain.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PostingLineMapperTest {

    private PostingLineMapper postingLineMapper = Mappers.getMapper(PostingLineMapper.class);

    private LedgerAccountMapper ledgerAccountMapper = Mappers.getMapper(LedgerAccountMapper.class);

    @Test
    public void toPostingLineBO() {
        PostingLine postingLine = new PostingLine();
        postingLine.setId("id");
        postingLine.setBaseLine("baseLine");
        postingLine.setCreditAmount(BigDecimal.ONE);
        postingLine.setDebitAmount(BigDecimal.TEN);
        OperationDetails details = new OperationDetails();
        details.setOpDetails("opDetails");
        postingLine.setDetails(details);
        postingLine.setDiscardedTime(LocalDateTime.now());
        postingLine.setHash("hash");
        postingLine.setOprId("oprId");
        postingLine.setOprSrc("oprSrc");
        postingLine.setPstStatus(PostingStatus.POSTED);
        postingLine.setPstTime(LocalDateTime.now());
        postingLine.setPstType(PostingType.BUSI_TX);
        postingLine.setRecordTime(LocalDateTime.now());
        postingLine.setSrcAccount("srcAccount");
        postingLine.setSubOprSrcId("subOprSrcId");
        LedgerAccount account = new LedgerAccount();
        postingLine.setAccount(account);

        PostingLineBO postingLineBO = postingLineMapper.toPostingLineBO(postingLine);

        assertEquals(postingLine.getId(), postingLineBO.getId());
        assertEquals(postingLine.getBaseLine(), postingLineBO.getBaseLine());
        assertEquals(postingLine.getCreditAmount(), postingLineBO.getCreditAmount());
        assertEquals(postingLine.getDebitAmount(), postingLineBO.getDebitAmount());
        assertEquals(postingLine.getDetails().getOpDetails(), postingLineBO.getDetails());
        assertEquals(postingLine.getDiscardedTime(), postingLineBO.getDiscardedTime());
        assertEquals(postingLine.getHash(), postingLineBO.getHash());
        assertEquals(postingLine.getOprId(), postingLineBO.getOprId());
        assertEquals(postingLine.getOprSrc(), postingLineBO.getOprSrc());
        assertEquals(PostingStatusBO.POSTED, postingLineBO.getPstStatus());
        assertEquals(postingLine.getPstTime(), postingLineBO.getPstTime());
        assertEquals(PostingTypeBO.BUSI_TX, postingLineBO.getPstType());
        assertEquals(postingLine.getRecordTime(), postingLineBO.getRecordTime());
        assertEquals(postingLine.getSrcAccount(), postingLineBO.getSrcAccount());
        assertEquals(postingLine.getSubOprSrcId(), postingLineBO.getSubOprSrcId());
    }

    @Test
    public void toPostingLine() {
        PostingLineBO postingLineBO = new PostingLineBO();
        postingLineBO.setId("id");
        postingLineBO.setBaseLine("baseLine");
        postingLineBO.setCreditAmount(BigDecimal.TEN);
        postingLineBO.setDebitAmount(BigDecimal.ZERO);
        postingLineBO.setDetails("details");
        postingLineBO.setDiscardedTime(LocalDateTime.now());
        postingLineBO.setHash("hash");
        postingLineBO.setOprId("oprId");
        postingLineBO.setOprSrc("oprSrc");
        postingLineBO.setPstStatus(PostingStatusBO.SIMULATED);
        postingLineBO.setPstTime(LocalDateTime.now());
        postingLineBO.setPstType(PostingTypeBO.ADJ_TX);
        postingLineBO.setRecordTime(LocalDateTime.now());
        postingLineBO.setSrcAccount("srcAccount");
        postingLineBO.setSubOprSrcId("subOprSrcId");
        LedgerAccountBO account = new LedgerAccountBO();
        postingLineBO.setAccount(account);

        PostingLine postingLine = postingLineMapper.toPostingLine(postingLineBO);

        assertEquals(postingLineBO.getId(), postingLine.getId());
        assertEquals(postingLineBO.getBaseLine(), postingLine.getBaseLine());
        assertEquals(postingLineBO.getCreditAmount(), postingLine.getCreditAmount());
        assertEquals(postingLineBO.getDebitAmount(), postingLine.getDebitAmount());
        assertEquals(postingLineBO.getDetails(), postingLine.getDetails().getOpDetails());
        assertEquals(postingLineBO.getDiscardedTime(), postingLine.getDiscardedTime());
        assertEquals(postingLineBO.getHash(), postingLine.getHash());
        assertEquals(postingLineBO.getOprId(), postingLineBO.getOprId());
        assertEquals(postingLineBO.getOprSrc(), postingLine.getOprSrc());
        assertEquals(PostingStatus.SIMULATED, postingLine.getPstStatus());
        assertEquals(postingLineBO.getPstTime(), postingLine.getPstTime());
        assertEquals(PostingType.ADJ_TX, postingLine.getPstType());
        assertEquals(postingLineBO.getRecordTime(), postingLine.getRecordTime());
        assertEquals(postingLineBO.getSrcAccount(), postingLine.getSrcAccount());
        assertEquals(postingLineBO.getSubOprSrcId(), postingLine.getSubOprSrcId());
    }
}
