package de.adorsys.ledgers.postings.relationships;


import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerRepository;
import de.adorsys.ledgers.tests.PostingsApplication;
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

import java.util.Optional;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("TestManyToOneRelationshipBetweenLedgerAccountAndLedger-db-entries.xml")
@DatabaseTearDown(value = {"TestManyToOneRelationshipBetweenLedgerAccountAndLedger-db-entries.xml"}, type = DatabaseOperation.DELETE_ALL)

//TODO check if is necessary
public class TestManyToOneRelationshipBetweenLedgerAccountAndLedger {

    @Autowired
    LedgerAccountRepository ledgerAccountRepository;

    @Autowired
    LedgerRepository ledgerRepository;

    @Test
    public void test_2_ledger_accounts_same_ledger() {

        LedgerAccount ledgerAccount1 = ledgerAccountRepository.findById("xVgaTPMcRty9ik3BTQDh1Q_BS_1_0_0").orElse(null);
        Assert.assertNotNull(ledgerAccount1);

        LedgerAccount ledgerAccount2 = ledgerAccountRepository.findById("xVgaTPMcRty9ik3BTQDh1Q_BS_2_0_0").orElse(null);
        Assert.assertNotNull(ledgerAccount2);

        // Ledger of 2 LedgerAccounts is the same
        Assert.assertEquals(ledgerAccount1.getLedger().getId(), ledgerAccount2.getLedger().getId());
    }

    @Test
    public void test_ledger_account_has_one_ledger() {

        LedgerAccount ledgerAccount1 = ledgerAccountRepository.findById("xVgaTPMcRty9ik3BTQDh1Q_BS_1_0_0").orElse(null);
        Assert.assertNotNull(ledgerAccount1);

        Optional<Ledger> opt = ledgerRepository.findById(ledgerAccount1.getLedger().getId());
        Assert.assertTrue(opt.isPresent());

    }


}
