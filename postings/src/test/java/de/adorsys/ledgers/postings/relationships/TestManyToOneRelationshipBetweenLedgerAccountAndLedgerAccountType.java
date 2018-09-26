package de.adorsys.ledgers.postings.relationships;


import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountTypeRepository;
import de.adorsys.ledgers.postings.repository.LedgerRepository;
import de.adorsys.ledgers.tests.PostingsApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.Optional;

import static org.junit.Assume.assumeNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("TestManyToOneRelationshipBetweenLedgerAccountAndLedgerAccountType-db-entries.xml")
@DatabaseTearDown(value={"TestManyToOneRelationshipBetweenLedgerAccountAndLedgerAccountType-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)

public class TestManyToOneRelationshipBetweenLedgerAccountAndLedgerAccountType {

    @Autowired
    ChartOfAccountRepository chartOfAccountRepository;

    @Autowired
    LedgerRepository ledgerRepository;

    @Autowired
    LedgerAccountTypeRepository ledgerAccountTypeRepository;

    @Autowired
    LedgerAccountRepository ledgerAccountRepository;




    @Test
    public void test_ledger_account_has_one_ledger_account_type() {

        ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8PDcTrCsi-F3sT3i-g").orElse(null);
        assumeNotNull(coa);

        LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findOptionalByCoaAndName(coa, "1.0.0").orElse(null);


        LedgerAccount ledgerAccount = ledgerAccountRepository.findById("xVgaTPMcRty9ik3BTQDh1Q_BS_1_0_0").orElse(null);
        Assert.assertNotNull(ledgerAccount);

        Assert.assertEquals(ledgerAccountType.getId(), ledgerAccount.getAccountType().getId());



    }


}
