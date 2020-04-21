package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.db.domain.*;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.db.repository.PostingLineRepository;
import de.adorsys.ledgers.postings.db.repository.PostingRepository;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostingServiceImplTest {
    private static final String LEDGER_ID = "Ledger Id";
    private static final LocalDateTime DATE_TIME = LocalDateTime.now();
    private static final String NAME = "Mr. Jones";
    private static final String OP_ID = "OP_ID";

    @InjectMocks
    private PostingServiceImpl postingService;
    @Mock
    private LedgerRepository ledgerRepository;
    @Mock
    private PostingRepository postingRepository;
    @Mock
    private PostingLineRepository postingLineRepository;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Test
    void newPosting() {
        // Given
        when(postingRepository.findFirstByLedgerOrderByRecordTimeDesc(any()))
                .thenReturn(Optional.of(new Posting()));
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(getLedger()));
        when(postingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When
        PostingBO result = postingService.newPosting(getPostingBO());

        // Then
        assertNotNull(result);
    }

    @Test
    void findPostingsByOperationId() {
        // Given
        when(postingRepository.findByOprId(any())).thenReturn(Collections.singletonList(getPosting()));

        // When
        List<PostingBO> result = postingService.findPostingsByOperationId(OP_ID);

        // then
        assertTrue(CollectionUtils.isNotEmpty(result));
    }

    @Test
    void findPostingsByDates() {
        // Given
        when(postingLineRepository.findByAccountAndPstTimeGreaterThanAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByPstTimeDesc(any(), any(), any()))
                .thenReturn(Collections.singletonList(readYml(PostingLine.class, "PostingLine.yml")));
        when(ledgerAccountRepository.findById(any())).thenReturn(Optional.of(new LedgerAccount()));
        LedgerAccountBO readYml = readYml(LedgerAccountBO.class, "LedgerAccount.yml");

        // When
        List<PostingLineBO> result = postingService.findPostingsByDates(readYml, LocalDateTime.of(2018, 12, 12, 0, 0), LocalDateTime.of(2018, 12, 20, 0, 0));

        // Then
        assertTrue(CollectionUtils.isNotEmpty(result));
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
        p.setLedger(getLedger());
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
        p.setLedger(getLedgerBO());
        p.setOprType("Some type");
        return p;
    }

    private static <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(PostingServiceImpl.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Ledger getLedger() {
        return new Ledger(LEDGER_ID, DATE_TIME, "User", "Some short description",
                          "Some long description", NAME, getChartOfAccount());
    }

    private LedgerBO getLedgerBO() {
        return new LedgerBO(NAME, LEDGER_ID, DATE_TIME, "User", "Some short description",
                            "Some long description", getChartOfAccountBO());
    }

    private ChartOfAccount getChartOfAccount() {
        return new ChartOfAccount("id", DATE_TIME, NAME, "Some short description",
                                  "Some long description", NAME);
    }

    private ChartOfAccountBO getChartOfAccountBO() {
        return new ChartOfAccountBO(NAME, "id", DATE_TIME, "Some short description",
                                    "Some long description", NAME);
    }
}
