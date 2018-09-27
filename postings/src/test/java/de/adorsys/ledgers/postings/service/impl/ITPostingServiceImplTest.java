package de.adorsys.ledgers.postings.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.domain.PostingType;
import de.adorsys.ledgers.postings.repository.LedgerRepository;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.tests.PostingsApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,DbUnitTestExecutionListener.class})
@DatabaseSetup("ITPostingServiceImplTest-db-entries.xml")
@DatabaseTearDown(value={"ITPostingServiceImplTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITPostingServiceImplTest {

    @Autowired
    LedgerRepository ledgerRepository;

    @Autowired
    PostingServiceImpl postingService;

    @Test
    public void test_newPosting_ok() {
        Optional<Ledger> ledgerOption = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg");
        Assume.assumeTrue(ledgerOption.isPresent());
        Posting posting = Posting.builder()
                .id(Ids.id())
                .recordUser("recUser")
                .oprId("oprId")
                .oprDetails("oprDetails")
                .pstTime(LocalDateTime.now())
                .pstType(PostingType.BAL_STMT)
                .ledger(ledgerOption.get())
                .lastClosing(LocalDateTime.of(2018, 1, 1, 0, 0))
                .build();
        Posting saved = postingService.newPosting(posting);
        Assume.assumeNotNull(saved);
    }
}
