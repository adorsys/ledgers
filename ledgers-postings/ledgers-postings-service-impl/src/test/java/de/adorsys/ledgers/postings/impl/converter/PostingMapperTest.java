/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.converter;

import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.db.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class PostingMapperTest {
    private static final String OP_ID = "oprId";
    private static final LocalDateTime DATE_TIME = LocalDateTime.of(2018, 12, 20, 12, 12);
    @InjectMocks
    PostingMapper mapper = Mappers.getMapper(PostingMapper.class);

    @Test
    void toPosting() {
        // Given
        PostingBO postingBO = getPostingBO();

        // When
        Posting result = mapper.toPosting(postingBO);

        // Then
        assertEquals(postingBO.getId(), result.getId());
        assertEquals(postingBO.getRecordUser(), result.getRecordUser());
        assertEquals(postingBO.getRecordTime(), result.getRecordTime());
        assertEquals(postingBO.getOprId(), result.getOprId());
        assertEquals(postingBO.getOprTime(), result.getOprTime());
        assertEquals(postingBO.getOprType(), result.getOprType());
        assertEquals(postingBO.getOprDetails(), result.getOprDetails().getOpDetails());
        assertEquals(postingBO.getOprSrc(), result.getOprSrc());
        assertEquals(postingBO.getPstTime(), result.getPstTime());
        assertEquals(postingBO.getPstType().name(), result.getPstType().name());
        assertEquals(postingBO.getPstStatus().name(), result.getPstStatus().name());
        assertEquals(postingBO.getValTime(), result.getValTime());
        assertEquals(postingBO.getLines(), result.getLines());
        assertEquals(postingBO.getDiscardedId(), result.getDiscardedId());
        assertEquals(postingBO.getDiscardedTime(), result.getDiscardedTime());
        assertEquals(postingBO.getDiscardingId(), result.getDiscardingId());

        //assert Coa mapped
        ChartOfAccountBO coa = postingBO.getLedger().getCoa();
        ChartOfAccount resultCoa = result.getLedger().getCoa();
        if (coa != null && resultCoa != null) {
            assertEquals(coa.getId(), resultCoa.getId());
            assertEquals(coa.getCreated(), resultCoa.getCreated());
            assertEquals(coa.getName(), resultCoa.getName());
            assertEquals(coa.getLongDesc(), resultCoa.getLongDesc());
            assertEquals(coa.getShortDesc(), resultCoa.getShortDesc());
            assertEquals(coa.getUserDetails(), resultCoa.getUserDetails());
        }
    }

    @Test
    void toOprDetailsBO() {
        // Given
        Posting posting = getPosting();

        // When
        PostingBO result = mapper.toPostingBO(posting);

        // Then
        assertEquals(posting.getId(), result.getId());
        assertEquals(posting.getRecordUser(), result.getRecordUser());
        assertEquals(posting.getRecordTime(), result.getRecordTime());
        assertEquals(posting.getOprId(), result.getOprId());
        assertEquals(posting.getOprTime(), result.getOprTime());
        assertEquals(posting.getOprType(), result.getOprType());
        assertEquals(posting.getOprSrc(), result.getOprSrc());
        assertEquals(posting.getPstTime(), result.getPstTime());
        assertEquals(posting.getPstType().name(), result.getPstType().name());
        assertEquals(posting.getPstStatus().name(), result.getPstStatus().name());
        assertEquals(posting.getValTime(), result.getValTime());
        assertEquals(posting.getLines(), result.getLines());
        assertEquals(posting.getDiscardedId(), result.getDiscardedId());
        assertEquals(posting.getDiscardedTime(), result.getDiscardedTime());
        assertEquals(posting.getDiscardingId(), result.getDiscardingId());

        //assert Coa mapped
        ChartOfAccount coa = posting.getLedger().getCoa();
        ChartOfAccountBO resultCoa = result.getLedger().getCoa();
        if (coa != null && resultCoa != null) {
            assertEquals(coa.getId(), resultCoa.getId());
            assertEquals(coa.getCreated(), resultCoa.getCreated());
            assertEquals(coa.getName(), resultCoa.getName());
            assertEquals(coa.getLongDesc(), resultCoa.getLongDesc());
            assertEquals(coa.getShortDesc(), resultCoa.getShortDesc());
            assertEquals(coa.getUserDetails(), resultCoa.getUserDetails());
        }
    }

    @Test
    void toOperationDetails() {
        // When
        OperationDetails details = mapper.toOperationDetails("Operation Details");

        // Then
        assertNotNull(details.getId());
        assertEquals("Operation Details", details.getOpDetails());
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