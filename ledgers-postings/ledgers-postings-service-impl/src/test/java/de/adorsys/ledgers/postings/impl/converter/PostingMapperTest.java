package de.adorsys.ledgers.postings.impl.converter;

import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.domain.PostingStatusBO;
import de.adorsys.ledgers.postings.api.domain.PostingTypeBO;
import de.adorsys.ledgers.postings.db.domain.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PostingMapperTest {
    private static final String OP_ID = "oprId";
    private static final LocalDateTime DATE_TIME = LocalDateTime.of(2018, 12, 20, 12, 12);
    @InjectMocks
    PostingMapper mapper = Mappers.getMapper(PostingMapper.class);

    @Test
    public void toPosting() {
        PostingBO postingBO = getPostingBO();
        Posting result = mapper.toPosting(postingBO);

        assertThat(result.getId()).isEqualTo(postingBO.getId());
        assertThat(result.getRecordUser()).isEqualTo(postingBO.getRecordUser());
        assertThat(result.getRecordTime()).isEqualTo(postingBO.getRecordTime());
        assertThat(result.getOprId()).isEqualTo(postingBO.getOprId());
        assertThat(result.getOprTime()).isEqualTo(postingBO.getOprTime());
        assertThat(result.getOprType()).isEqualTo(postingBO.getOprType());
        assertThat(result.getOprDetails().getOpDetails()).isEqualTo(postingBO.getOprDetails());
        assertThat(result.getOprSrc()).isEqualTo(postingBO.getOprSrc());
        assertThat(result.getPstTime()).isEqualTo(postingBO.getPstTime());
        assertThat(result.getPstType().name()).isEqualTo(postingBO.getPstType().name());
        assertThat(result.getPstStatus().name()).isEqualTo(postingBO.getPstStatus().name());
        assertThat(result.getLedger()).isEqualToComparingFieldByFieldRecursively(postingBO.getLedger());
        assertThat(result.getValTime()).isEqualTo(postingBO.getValTime());
        assertThat(result.getLines()).isEqualTo(postingBO.getLines());
        assertThat(result.getDiscardedId()).isEqualTo(postingBO.getDiscardedId());
        assertThat(result.getDiscardedTime()).isEqualTo(postingBO.getDiscardedTime());
        assertThat(result.getDiscardingId()).isEqualTo(postingBO.getDiscardingId());
    }

    @Test
    public void toOprDetailsBO() {
        Posting posting = getPosting();
        PostingBO result = mapper.toPostingBO(posting);

        assertThat(result.getId()).isEqualTo(posting.getId());
        assertThat(result.getRecordUser()).isEqualTo(posting.getRecordUser());
        assertThat(result.getRecordTime()).isEqualTo(posting.getRecordTime());
        assertThat(result.getOprId()).isEqualTo(posting.getOprId());
        assertThat(result.getOprTime()).isEqualTo(posting.getOprTime());
        assertThat(result.getOprType()).isEqualTo(posting.getOprType());
        assertThat(result.getOprDetails()).isEqualTo(posting.getOprDetails().getOpDetails());
        assertThat(result.getOprSrc()).isEqualTo(posting.getOprSrc());
        assertThat(result.getPstTime()).isEqualTo(posting.getPstTime());
        assertThat(result.getPstType().name()).isEqualTo(posting.getPstType().name());
        assertThat(result.getPstStatus().name()).isEqualTo(posting.getPstStatus().name());
        assertThat(result.getLedger()).isEqualToComparingFieldByFieldRecursively(posting.getLedger());
        assertThat(result.getValTime()).isEqualTo(posting.getValTime());
        assertThat(result.getLines()).isEqualTo(posting.getLines());
        assertThat(result.getDiscardedId()).isEqualTo(posting.getDiscardedId());
        assertThat(result.getDiscardedTime()).isEqualTo(posting.getDiscardedTime());
        assertThat(result.getDiscardingId()).isEqualTo(posting.getDiscardingId());
    }

    @Test
    public void toOperationDetails() {
        OperationDetails details = mapper.toOperationDetails("Operation Details");

        assertThat(details.getId()).isNotNull();
        assertThat(details.getOpDetails()).isEqualTo("Operation Details");
    }

    private Posting getPosting() {
        Posting p = new Posting();
        p.setRecordUser("Record User");
        p.setAntecedentHash("Antecedent HASH");
        p.setAntecedentId("AntecedentId");
        p.setOprId(OP_ID);
        p.setOprDetails(new OperationDetails("Operation details"));
        p.setPstTime(DATE_TIME);
        p.setRecordTime(DATE_TIME);
        p.setPstType(PostingType.ADJ_TX);
        p.setPstStatus(PostingStatus.OTHER);
        p.setLines(Collections.emptyList());
        p.setLedger(new Ledger());
        p.setOprType("Some type");
        return p;
    }

    private PostingBO getPostingBO() {
        PostingBO p = new PostingBO();
        p.setRecordUser("Record User");
        p.setAntecedentHash("Antecedent HASH");
        p.setAntecedentId("AntecedentId");
        p.setOprId(OP_ID);
        p.setOprDetails("Operation details");
        p.setPstTime(DATE_TIME);
        p.setRecordTime(DATE_TIME);
        p.setPstType(PostingTypeBO.ADJ_TX);
        p.setPstStatus(PostingStatusBO.OTHER);
        p.setLines(Collections.emptyList());
        p.setLedger(new LedgerBO());
        p.setOprType("Some type");
        return p;
    }
}