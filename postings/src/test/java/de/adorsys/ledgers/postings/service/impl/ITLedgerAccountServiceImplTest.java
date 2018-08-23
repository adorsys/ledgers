package de.adorsys.ledgers.postings.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import de.adorsys.ledgers.postings.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountTypeRepository;
import de.adorsys.ledgers.postings.repository.LedgerRepository;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.tests.PostingsApplication;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLedgerAccountServiceImplTest-db-entries.xml")
@DatabaseTearDown(value={"ITLedgerAccountServiceImplTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)

public class ITLedgerAccountServiceImplTest {
    @Autowired
    private LedgerAccountRepository ledgerAccountRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private LedgerAccountTypeRepository ledgerAccountTypeRepository;

    @Test
    public void test_find_by_ledger_and_name_ok() {
        Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        Assume.assumeNotNull(ledger);
        List<LedgerAccount> found = ledgerAccountRepository.findByLedgerAndName(ledger, "BS");
        assertEquals(1, found.size());
    }

    @Test
    public void test_find_by_ledger_and_level_and_account_type_and_valid_from_before_and_valid_to_after(){
        Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        Assume.assumeNotNull(ledger);
        LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findById("805UO1hITPHxQq16OuGvw_BS").orElse(null);
        Assume.assumeNotNull(ledgerAccountType);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime validFrom = LocalDateTime.parse("2018-08-07 23:50:41.231", formatter);
        LocalDateTime validTo = LocalDateTime.parse("2199-01-01 00:00:00.000", formatter);

        List<LedgerAccount> found = ledgerAccountRepository
                .findByLedgerAndLevelAndAccountTypeAndValidFromBeforeAndValidToAfter(ledger, 0, ledgerAccountType, validFrom, validTo);
        assertEquals(1, found.size());

    }
}
