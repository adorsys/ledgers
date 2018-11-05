package de.adorsys.ledgers.postings.impl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.DoubleEntryAccountingException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.exception.PostingNotFoundException;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.postings.db.domain.AccountCategory;
import de.adorsys.ledgers.postings.db.domain.BalanceSide;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.Posting;
import de.adorsys.ledgers.postings.db.domain.PostingLine;
import de.adorsys.ledgers.postings.db.domain.PostingStatus;
import de.adorsys.ledgers.postings.db.domain.PostingType;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.db.repository.PostingLineRepository;
import de.adorsys.ledgers.postings.db.repository.PostingRepository;
import de.adorsys.ledgers.postings.impl.converter.LedgerAccountMapper;
import de.adorsys.ledgers.postings.impl.converter.PostingLineMapper;
import de.adorsys.ledgers.postings.impl.converter.PostingMapper;

@RunWith(MockitoJUnitRunner.class)
public class PostingServiceImplTest {
    private static final String LEDGER_ID = "Ledger Id";
    private static final LocalDateTime DATE_TIME = LocalDateTime.now();
    private static final String NAME = "Mr. Jones";
    private static final String OP_ID = "OP_ID";

    private static final PostingMapper POSTING_MAPPER = new PostingMapper();
    private static final PostingLineMapper POSTING_LINE_MAPPER = new PostingLineMapper();
    private static final LedgerAccountMapper LEDGER_ACCOUNT_MAPPER = new LedgerAccountMapper();

    @InjectMocks
    private PostingService postingService = new PostingServiceImpl(POSTING_MAPPER, POSTING_LINE_MAPPER);

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
	@Mock
    private AccountStmtServiceImpl postingRepositoryAdapter;

    @Test
    public void newPosting() throws PostingNotFoundException, LedgerAccountNotFoundException, LedgerNotFoundException, BaseLineException, DoubleEntryAccountingException, de.adorsys.ledgers.postings.db.exception.DoubleEntryAccountingException, de.adorsys.ledgers.postings.db.exception.BaseLineException {
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(getLedger()));
        when(postingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        //When
        PostingBO result = postingService.newPosting(POSTING_MAPPER.toPostingBO(getPosting()));
        //Then
        assertThat(result).isNotNull();
    }

    @Test
    public void findPostingsByOperationId() {
        when(postingRepository.findByOprId(any())).thenReturn(Collections.singletonList(getPosting()));
        //When
        List<PostingBO> result = postingService.findPostingsByOperationId(OP_ID);
        //then
        assertThat(CollectionUtils.isNotEmpty(result)).isTrue();
    }

    private PostingLine getPostingLine() {
    	PostingLine pl = new PostingLine();
    	pl.setId(OP_ID);
//    	pl.setPosting(getPosting());
    	pl.setAccount(getLedgerAccount());
    	pl.setDebitAmount(BigDecimal.valueOf(100));
    	pl.setCreditAmount(BigDecimal.valueOf(100));
    	pl.setDetails("Some details");
    	pl.setSrcAccount("scrAccount");
    	return pl;
    }

    private LedgerAccount getLedgerAccount() {
        return new LedgerAccount(LEDGER_ID, DATE_TIME, "User", "Some short description",
                "Some long description", NAME, getLedger(), null, getChartOfAccount(),
                BalanceSide.Cr, AccountCategory.AS);
    }

    private Posting getPosting() {
    	Posting p = new Posting();
    	p.setRecordUser("Record User");
    	p.setAntecedentHash("Antecedent HASH");
    	p.setAntecedentId("AntecedentId");
    	p.setOprId(OP_ID);
    	p.setOprDetails("Operation details");
    	p.setPstTime(DATE_TIME);
    	p.setRecordTime(DATE_TIME);
    	p.setPstType(PostingType.ADJ_TX);
    	p.setPstStatus(PostingStatus.OTHER);
    	p.setLines(Collections.emptyList());
    	p.setLedger(getLedger());
    	p.setOprType("Some type");
    	return p;
    }

    private Ledger getLedger() {
        return new Ledger(LEDGER_ID, DATE_TIME, "User", "Some short description",
                "Some long description", NAME, getChartOfAccount());
    }

    private ChartOfAccount getChartOfAccount() {
        return new ChartOfAccount("id", DATE_TIME, NAME, "Some short description",
                "Some long description", NAME);
    }
}
