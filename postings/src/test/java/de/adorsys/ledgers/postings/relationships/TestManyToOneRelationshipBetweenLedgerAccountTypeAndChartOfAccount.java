package de.adorsys.ledgers.postings.relationships;


import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.Ledger;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("TestManyToOneRelationshipBetweenLedgerAccountTypeAndChartOfAccount-db-entries.xml")
@DatabaseTearDown(value={"TestManyToOneRelationshipBetweenLedgerAccountTypeAndChartOfAccount-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)


public class TestManyToOneRelationshipBetweenLedgerAccountTypeAndChartOfAccount {

    @Autowired
    ChartOfAccountRepository chartOfAccountRepository;

    @Autowired
    LedgerAccountTypeRepository ledgerAccountTypeRepository;

    @Test
    public void test_2_ledger_account_type_have_same_chart_of_account() {

        ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8PDcTrCsi-F3sT3i-g").orElse(null);
        Assert.assertNotNull(coa);

        LedgerAccountType ledgerAccountType1 = ledgerAccountTypeRepository.findOptionalByCoaAndName(coa, "1.0.0").orElse(null);
        Assert.assertNotNull(ledgerAccountType1);

        LedgerAccountType ledgerAccountType2 = ledgerAccountTypeRepository.findOptionalByCoaAndName(coa, "2.0.0").orElse(null);
        Assert.assertNotNull(ledgerAccountType2);

        Assert.assertEquals(ledgerAccountType1.getCoa().getId(), ledgerAccountType2.getCoa().getId());


    }

    @Test
    public void test_ledger_account_type_has_one_chart_of_account() {

        ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8PDcTrCsi-F3sT3i-g").orElse(null);
        Assert.assertNotNull(coa);

        LedgerAccountType ledgerAccountType1 = ledgerAccountTypeRepository.findOptionalByCoaAndName(coa, "1.0.0").orElse(null);
        Assert.assertNotNull(ledgerAccountType1);

        Assert.assertEquals(ledgerAccountType1.getCoa().getId(), coa.getId());


    }


}
