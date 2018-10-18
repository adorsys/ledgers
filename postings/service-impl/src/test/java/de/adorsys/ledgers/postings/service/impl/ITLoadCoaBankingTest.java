package de.adorsys.ledgers.postings.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.postings.converter.LedgerAccountMapper;
import de.adorsys.ledgers.postings.converter.LedgerMapper;
import de.adorsys.ledgers.postings.domain.*;
import de.adorsys.ledgers.postings.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.tests.PostingsApplication;
import lombok.Data;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.io.IOException;
import java.io.InputStream;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLoadCoaBankingTest-db-entries.xml")
@DatabaseTearDown(value = {"ITLoadCoaBankingTest-db-delete.xml"}, type = DatabaseOperation.DELETE_ALL)
public class ITLoadCoaBankingTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerMapper ledgerMapper;
    @Autowired
    private LedgerAccountMapper ledgerAccountMapper;

    @Before
    public void before() {
        final YAMLFactory ymlFactory = new YAMLFactory();
        mapper = new ObjectMapper(ymlFactory);
    }

    @Test
    public void test_load_coa_ok() throws IOException, LedgerAccountNotFoundException, LedgerNotFoundException {
        LedgerBO ledger = ledgerService.findLedgerById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        Assume.assumeNotNull(ledger);

        InputStream inputStream = LoadLedgerAccountYMLTest.class.getResourceAsStream("ITLoadCoaBankingTest-coa.yml");
        LegAccYamlModel[] ledgerAccounts = mapper.readValue(inputStream, LegAccYamlModel[].class);
        for (LegAccYamlModel model : ledgerAccounts) {
            LedgerAccount parent = model.getParent() == null
                                           ? null
                                           : LedgerAccount.builder()
                                                     .ledger(ledgerMapper.toLedger(ledger))
                                                     .name(model.getParent())
                                                     .build();
            LedgerAccountBO ledgerAccount = ledgerAccountMapper.toLedgerAccountBO(LedgerAccount.builder()
                                                                                          .shortDesc(model.getShortDesc())
                                                                                          .name(model.getName())
                                                                                          .balanceSide(model.getBalanceSide())
                                                                                          .category(model.getCategory())
                                                                                          .ledger(ledgerMapper.toLedger(ledger))
                                                                                          .parent(parent)
                                                                                          .build());
            ledgerService.newLedgerAccount(ledgerAccount);

        }

        LedgerAccountBO la = ledgerService.findLedgerAccount(ledger, "1003").orElse(null);
        Assume.assumeNotNull(la);
        Assert.assertEquals("Cash in transit", la.getShortDesc());
        Assert.assertEquals(AccountCategoryBO.AS, la.getCategory());
        Assert.assertEquals(BalanceSideBO.Dr, la.getBalanceSide());
    }

    @Data
    private static class LegAccYamlModel {
        private String shortDesc;
        private String name;
        private AccountCategory category;
        private BalanceSide balanceSide;
        private String parent;
    }

}
