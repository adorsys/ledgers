package de.adorsys.ledgers.postings.db.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.PostingTrace;
import de.adorsys.ledgers.postings.db.tests.PostingRepositoryApplication;
import de.adorsys.ledgers.util.Ids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PostingRepositoryApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("PostingTraceIT-db-entries.xml")
@DatabaseTearDown(value = {"PostingTraceIT-db-entries.xml"}, type = DatabaseOperation.DELETE_ALL)
class PostingTraceIT {
    @Autowired
    private LedgerAccountRepository ledgerAccountRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private PostingTraceRepository postingTraceRepository;

    @Test
    void test_create_posting_trace_ok() {
        // Given
        Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        assumeTrue(ledger != null);

        LedgerAccount account = ledgerAccountRepository.findOptionalByLedgerAndName(ledger, "2.0.0").orElse(null);
        assumeTrue(account != null);

        String id = Ids.id();
        String oprId = "Zd0ND5YwSzGwIfZilhumPg_POSTING";
        String pstHash = "6b1672f7edf8c371860136271f89b41f22dc122a";
        LocalDateTime pstTime = LocalDateTime.of(2017, Month.DECEMBER, 31, 23, 59);

        String tgtPstId = "Zd0ND5YwSzGwIfZilhumPg_POSTING2";

        PostingTrace p = new PostingTrace();
        p.setAccount(account);
        p.setCreditAmount(BigDecimal.ZERO);
        p.setDebitAmount(BigDecimal.ZERO);
        p.setId(id);
        p.setSrcOprId(oprId);
        p.setSrcPstHash(pstHash);
        p.setSrcPstTime(pstTime);
        p.setTgtPstId(tgtPstId);
        p.setSrcPstId(id);

        // When
        PostingTrace saved = postingTraceRepository.save(p);
        assertNotNull(saved);
    }

    @Test
    void test_load_posting_trace_ok() {
        Optional<PostingTrace> postingTrace = postingTraceRepository.findById("Zd0ND5YwSzGwIfZilhumPg_POSTING_TRACE");
        assertTrue(postingTrace.isPresent());
    }
}
