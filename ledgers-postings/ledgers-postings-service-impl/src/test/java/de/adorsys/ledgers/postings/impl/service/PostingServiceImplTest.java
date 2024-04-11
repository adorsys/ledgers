/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.db.domain.*;
import de.adorsys.ledgers.postings.db.repository.*;
import de.adorsys.ledgers.util.exception.PostingErrorCode;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostingServiceImplTest {
    private static final String LEDGER_ID = "Ledger Id";
    private static final LocalDateTime DATE_TIME = LocalDateTime.now();
    private static final String NAME = "Mr. Jones";
    private static final String OP_ID = "OP_ID";

    private final LedgerAccountBO account = readYml(LedgerAccountBO.class, "LedgerAccount.yml");

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
    @Mock
    private AccountStmtRepository stmtRepository;

    @Test
    void newPosting() {
        // Given
        when(postingRepository.findFirstByLedgerOrderByRecordTimeDesc(any()))
                .thenReturn(Optional.of(new Posting()));
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(getLedger()));
        when(postingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(postingRepository.findByOprIdAndDiscardingIdIsNull(any())).thenReturn(Optional.of(new Posting()));
        when(ledgerAccountRepository.findById(any())).thenReturn(Optional.of(new LedgerAccount()));

        // When
        PostingBO result = postingService.newPosting(getPostingBO(true));

        // Then
        assertNotNull(result);
    }

    @Test
    void newPosting_line_balance_error() {
        // Given
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(getLedger()));
        when(postingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(postingRepository.findByOprIdAndDiscardingIdIsNull(any())).thenReturn(Optional.of(new Posting()));

        // When
        PostingBO postingBO = getPostingBO(false);
        PostingModuleException exception = assertThrows(PostingModuleException.class, () -> postingService.newPosting(postingBO));

        // Then
        assertSame(PostingErrorCode.DOBLE_ENTRY_ERROR, exception.getErrorCode());
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

        // When
        List<PostingLineBO> result = postingService.findPostingsByDates(account, LocalDateTime.of(2018, 12, 12, 0, 0), LocalDateTime.of(2018, 12, 20, 0, 0));

        // Then
        assertTrue(CollectionUtils.isNotEmpty(result));
    }

    @Test
    void findPostingLineById() {
        when(ledgerAccountRepository.findById(any())).thenReturn(Optional.of(new LedgerAccount()));
        when(postingLineRepository.findFirstByIdAndAccount(any(), any())).thenReturn(Optional.of(new PostingLine()));
        PostingLineBO result = postingService.findPostingLineById(account, "tr1");
        assertNotNull(result);
    }

    @Test
    void findPostingLineById_posting_nf() {
        when(ledgerAccountRepository.findById(any())).thenReturn(Optional.of(new LedgerAccount()));
        when(postingLineRepository.findFirstByIdAndAccount(any(), any())).thenReturn(Optional.empty());
        PostingModuleException exception = assertThrows(PostingModuleException.class, () -> postingService.findPostingLineById(account, "tr1"));
        assertSame(PostingErrorCode.POSTING_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void findPostingsByDatesPaged() {
        when(ledgerAccountRepository.findById(any())).thenReturn(Optional.of(new LedgerAccount()));
        when(postingLineRepository.findPostingsByAccountAndDates(any(), any(), any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(new PostingLine())));
        Page<PostingLineBO> result = postingService.findPostingsByDatesPaged(account, LocalDateTime.now().minusDays(1), LocalDateTime.now(), PageRequest.of(0, 1));
        assertTrue(result.isFirst());
        assertFalse(result.isEmpty());
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

    private PostingBO getPostingBO(boolean validLines) {
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
        p.setLines(List.of(getLine(true, true), getLine(false, validLines)));
        p.setLedger(getLedgerBO());
        p.setOprType("Some type");
        return p;
    }

    private PostingLineBO getLine(boolean isDebitLine, boolean isValid) {
        PostingLineBO line = new PostingLineBO();
        line.setAccount(account);
        line.setDebitAmount(isDebitLine && isValid ? BigDecimal.TEN : BigDecimal.ONE);
        line.setCreditAmount(!isDebitLine && isValid ? BigDecimal.TEN : BigDecimal.ONE);
        line.setPstTime(LocalDateTime.now().minusHours(1));
        return line;
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
