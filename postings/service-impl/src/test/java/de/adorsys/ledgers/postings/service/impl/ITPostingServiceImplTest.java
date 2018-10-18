package de.adorsys.ledgers.postings.service.impl;

import de.adorsys.ledgers.postings.domain.*;
import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerRepository;
import de.adorsys.ledgers.postings.repository.PostingLineRepository;
import de.adorsys.ledgers.postings.repository.PostingRepository;
import de.adorsys.ledgers.postings.service.PostingService;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ITPostingServiceImplTest {
    private static final String LEDGER_ID = "Ledger Id";
    private static final LocalDateTime DATE_TIME = LocalDateTime.now();
    private static final String NAME = "Mr. Jones";
    private static final String OP_ID = "OP_ID";

    @InjectMocks
    private PostingService postingService = new PostingServiceImpl();

    @Mock
    private LedgerRepository ledgerRepository;
    @Mock
    private PostingRepository postingRepository;
    @Mock
    private Principal principal;
    @Mock
    private PostingLineRepository postingLineRepository;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Test
    public void newPosting() throws NotFoundException {
        when(postingRepository.findFirstOptionalByLedgerOrderByRecordTimeDesc(any())).thenReturn(Optional.of(getPosting()));
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(getLedger()));
        when(principal.getName()).thenReturn(NAME);
        when(postingRepository.save(any())).thenReturn(getPosting());
        when(postingRepository.findById(any())).thenReturn(Optional.of(getPosting()));
        //When
        Posting result = postingService.newPosting(getPosting());
        //Then
        assertThat(result).isNotNull();
    }

    @Test
    public void findPostingsByOperationId() {
        when(postingRepository.findByOprId(any())).thenReturn(Collections.singletonList(getPosting()));
        //When
        List<Posting> result = postingService.findPostingsByOperationId(OP_ID);
        //then
        assertThat(CollectionUtils.isNotEmpty(result)).isTrue();
    }

    @Test
    public void balanceTx() throws NotFoundException {
        when(postingLineRepository.findFirstByAccountAndPstTypeAndPstStatusAndPstTimeLessThanEqualOrderByPstTimeDesc(any(), any(), any(), any()))
                .thenReturn(getPostingLine());
        when(postingLineRepository.computeBalance(any(), any(), any(), any(), any())).thenReturn(Arrays.asList(BigDecimal.TEN,BigDecimal.TEN));
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(getLedger()));
        when(postingRepository.findFirstOptionalByLedgerOrderByRecordTimeDesc(any())).thenReturn(Optional.empty());
        when(ledgerAccountRepository.findById(any())).thenReturn(Optional.of(getLedgerAccount()));
        when(postingRepository.save(any())).thenReturn(getPosting());
        when(postingRepository.findById(any())).thenReturn(Optional.of(getPosting()));
        //When
        Posting result = postingService.balanceTx(getLedgerAccount(), DATE_TIME);
        //Then
        assertThat(result).isNotNull();
    }

    private PostingLine getPostingLine() {
        return new PostingLine(OP_ID, getPosting(), getLedgerAccount(), BigDecimal.valueOf(100), BigDecimal.valueOf(100),
                "Some details", "scrAccount", DATE_TIME, OP_ID, 10, DATE_TIME,
                PostingType.ADJ_TX, PostingStatus.OTHER, getLedger(), NAME, DATE_TIME);
    }

    private LedgerAccountBO getLedgerAccount() {
        return new LedgerAccountBO(LEDGER_ID, DATE_TIME, "User", "Some short description",
                "Some long description", NAME, getLedger(), null, getChartOfAccount(),
                BalanceSide.Cr, AccountCategory.AS);
    }

    private Posting getPosting() {
        return new Posting("Record User", "AntecedentId",
                "Antecedent HASH", OP_ID, 1, DATE_TIME,
                "Some type", "Operation details", DATE_TIME, PostingType.ADJ_TX,
                PostingStatus.OTHER, getLedger(), DATE_TIME, Collections.emptyList());
    }

    private Ledger getLedger() {
        return new Ledger(LEDGER_ID, DATE_TIME, "User", "Some short description",
                "Some long description", NAME, getChartOfAccount(), DATE_TIME.minusDays(1));
    }

    private ChartOfAccount getChartOfAccount() {
        return new ChartOfAccount("id", DATE_TIME, NAME, "Some short description",
                "Some long description", NAME);
    }
}
