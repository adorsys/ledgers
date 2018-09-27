package de.adorsys.ledgers.postings.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

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
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.tests.PostingsApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLedgerServiceImplTest-db-entries.xml")
@DatabaseTearDown(value={"ITLedgerServiceImplTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITLedgerServiceImplTest {
    @Autowired
    private ChartOfAccountRepository chartOfAccountRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerAccountRepository ledgerAccountRepository;

    @Autowired
    private  LedgerAccountTypeRepository ledgerAccountTypeRepository;

    @Test
    public void test_new_ledger() {
        ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8PDcTrCsi-F3sT3i-g").orElse(null);
        Ledger ledger = Ledger.builder()
        		.id(Ids.id()).name("Sample Ledger-2").user("Sample User")
        		.coa(coa).lastClosing(LocalDateTime.now()).build();

        Ledger ledger2 = ledgerService.newLedger(ledger);
        ledgerRepository.save(ledger2);

    }

    @Test
    public void test_find_ledger_by_id() {
        Optional<Ledger> opt = ledgerService.findLedgerById("Zd0ND5YwSzGwIfZilhumPg");
        Assert.assertTrue(opt.isPresent());

    }
    @Test
    public void test_find_ledger_by_name() {
        Optional<Ledger> opt = ledgerService.findLedgerByName("Sample Ledger-0");
        Assert.assertTrue(opt.isPresent());
    }
    @Test
    public void test_new_ledger_account() {
        Ledger ledger = ledgerService.findLedgerById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        Assert.assertNotNull(ledger);

        LedgerAccount parentAccount= ledgerAccountRepository.findById("xVgaTPMcRty9ik3BTQDh1Q_BS").orElse(null);
        Assert.assertNotNull(parentAccount);

        LedgerAccountType accountType= ledgerAccountTypeRepository.findById("805UO1hITPHxQq16OuGvw_BS").orElse(null);
        Assert.assertNotNull(accountType);

        LocalDateTime created = LocalDateTime.now();

        LedgerAccount newLedgerAccount = LedgerAccount.builder()
                                            .id(Ids.id())
                                            .created(created)
                                            .user("Sample user")
                                            .ledger(ledger)
                                            .parent(parentAccount)
                                            .accountType(accountType)
                                            .level(parentAccount.getLevel() + 1)
                                            .name("LedgerAccountNameTest")
                                            .shortDesc("short description")
                                            .build();

        ledgerService.newLedgerAccount(newLedgerAccount);
    }

    @Test
    public void test_find_ledger_account_by_id() {
        Optional<LedgerAccount> opt = ledgerService.findLedgerAccountById("xVgaTPMcRty9ik3BTQDh1Q_BS");
        Assert.assertTrue(opt.isPresent());
    }
    @Test
    public void test_find_ledger_account() {
        ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8PDcTrCsi-F3sT3i-g").orElse(null);
        Assert.assertNotNull(coa);
        Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        Assert.assertNotNull(ledger);
        LedgerAccount ledgerAccount= ledgerService.findLedgerAccountById("xVgaTPMcRty9ik3BTQDh1Q_BS").orElse(null);
        Assert.assertNotNull(ledgerAccount);

        Optional<LedgerAccount> opt = ledgerService.findLedgerAccount(ledgerAccount.getLedger(), ledgerAccount.getName());
        Assert.assertTrue(opt.isPresent());
    }
    @Test
    public void test_find_n_ledger_accounts() {

    		Optional<LedgerAccount> laOption= ledgerService.findLedgerAccountById("xVgaTPMcRty9ik3BTQDh1Q_BS");
        Assert.assertTrue(laOption.isPresent());

        LedgerAccount ledgerAccount = laOption.get();
        laOption= ledgerService.findLedgerAccount(ledgerAccount.getLedger(), ledgerAccount.getName());
        Assert.assertTrue(laOption.isPresent());
    }
}
