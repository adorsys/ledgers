package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.db.repository.PostingRepository;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.util.exception.PostingErrorCode.DOBLE_ENTRY_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostingsMockServiceImplTest {

    private static final String OP_ID = "1";
    private static final LocalDateTime DATE_TIME = LocalDateTime.now();
    private static final String NAME = "ledgerName";
    private static final String LEDGER_ID = "ledgerId";
    @InjectMocks
    private PostingsMockServiceImpl service;

    @Mock
    private PostingRepository postingRepository;

    @Mock
    private LedgerRepository ledgerRepository;
    @Mock
    protected LedgerAccountRepository ledgerAccountRepository;

    @Test
    void addPostingsAsBatch() {
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(getLedger()));
        when(ledgerAccountRepository.findById(any())).thenReturn(Optional.of(new LedgerAccount()));
        List<PostingBO> postings = getPostings(true);
        service.addPostingsAsBatch(postings);

        verify(postingRepository, timeout(1000).times(1)).saveAll(any());
    }

    @Test
    void addPostingsAsBatch_failure() {
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(getLedger()));
        List<PostingBO> postings = getPostings(false);
        PostingModuleException exception = assertThrows(PostingModuleException.class, () -> service.addPostingsAsBatch(postings));
        assertEquals(DOBLE_ENTRY_ERROR, exception.getErrorCode());
    }

    private List<PostingBO> getPostings(boolean valid) {
        PostingBO posting = getPostingBO(valid);
        return List.of(posting);
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
        LedgerAccountBO account = new LedgerAccountBO();
        account.setId("id");
        line.setAccount(account);
        line.setDebitAmount(isDebitLine && isValid ? BigDecimal.TEN : BigDecimal.ONE);
        line.setCreditAmount(!isDebitLine && isValid ? BigDecimal.TEN : BigDecimal.ONE);
        line.setPstTime(LocalDateTime.now().minusHours(1));
        return line;
    }

    private LedgerBO getLedgerBO() {
        return new LedgerBO(NAME, LEDGER_ID, DATE_TIME, "User", "Some short description",
                            "Some long description", getChartOfAccountBO());
    }

    private ChartOfAccountBO getChartOfAccountBO() {
        return new ChartOfAccountBO(NAME, "id", DATE_TIME, "Some short description",
                                    "Some long description", NAME);
    }

    private Ledger getLedger() {
        return new Ledger(LEDGER_ID, DATE_TIME, "User", "Some short description",
                          "Some long description", NAME, getChartOfAccount());
    }

    private ChartOfAccount getChartOfAccount() {
        return new ChartOfAccount("id", DATE_TIME, "User details", "Some short description",
                                  "Some long description", NAME);
    }
}