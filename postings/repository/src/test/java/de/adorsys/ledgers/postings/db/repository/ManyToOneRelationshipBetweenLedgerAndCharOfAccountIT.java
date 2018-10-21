package de.adorsys.ledgers.postings.db.repository;


import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.db.tests.PostingsApplication;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("ManyToOneRelationshipBetweenLedgerAndCharOfAccountTest-db-entries.xml")
@DatabaseTearDown(value={"ManyToOneRelationshipBetweenLedgerAndCharOfAccountTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ManyToOneRelationshipBetweenLedgerAndCharOfAccountIT {
//TODO check if necessary
    @Autowired
    ChartOfAccountRepository chartOfAccountRepository;

    @Autowired
    LedgerRepository ledgerRepository;


    @Test
    public void test_2_ledgers_same_coa() {
        ChartOfAccount coa = chartOfAccountRepository.findOptionalByName("IFRS").orElse(null);
        Assert.assertNotNull(coa);

        Ledger ledger1 = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        Assert.assertNotNull(ledger1);

        Ledger ledger2 = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg1").orElse(null);
        Assert.assertNotNull(ledger2);

        // CoA of 2 ledgers is the same
        Assert.assertEquals(ledger1.getCoa().getId(), ledger2.getCoa().getId());
    }
}
