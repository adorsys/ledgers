package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.util.Ids;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PostingMapperTest {
    private PostingMapper mapper = Mappers.getMapper(PostingMapper.class);
    private static final LocalDateTime PST_TIME = LocalDateTime.now();
    private static final String PAYMENT_ID = "123";
    private static final String OPR_ID = "OPR_1";
    private static final String LINE_ID = "LINE_1";
    private static final String OPR_DETAILS = "details here";
    private static final String USR_NAME = "Test";

    @Test
    void buildPosting() {
        // When
        PostingBO result = mapper.buildPosting(PST_TIME, PAYMENT_ID, OPR_DETAILS, getLedger(), USR_NAME);

        // Then
        assertNotNull(result.getOprId());
        assertThat(result).isEqualToIgnoringGivenFields(getTestPosting(), "oprId");
    }

    @Test
    void buildPostingLine() {
        // When
        PostingLineBO result = mapper.buildPostingLine(OPR_DETAILS, getAccount(), BigDecimal.TEN, BigDecimal.TEN, OPR_ID, LINE_ID);

        // Then
        assertThat(result).isEqualTo(getLine());
    }

    private PostingLineBO getLine() {
        PostingLineBO l = new PostingLineBO();
        l.setId(LINE_ID);
        l.setDetails(OPR_DETAILS);
        l.setAccount(getAccount());
        l.setDebitAmount(BigDecimal.TEN);
        l.setCreditAmount(BigDecimal.TEN);
        l.setSubOprSrcId(OPR_ID);
        return l;
    }

    private PostingBO getTestPosting() {
        PostingBO p = new PostingBO();
        p.setOprId(Ids.id());
        p.setRecordUser(USR_NAME);
        p.setOprTime(PST_TIME);
        p.setOprSrc(PAYMENT_ID);
        p.setOprDetails(OPR_DETAILS);
        p.setPstTime(PST_TIME);
        p.setPstType(PostingTypeBO.BUSI_TX);
        p.setPstStatus(PostingStatusBO.POSTED);
        p.setLedger(getLedger());
        p.setValTime(PST_TIME);
        return p;
    }

    private LedgerBO getLedger() {
        return new LedgerBO();
    }

    private LedgerAccountBO getAccount() {
        return new LedgerAccountBO();
    }
}
