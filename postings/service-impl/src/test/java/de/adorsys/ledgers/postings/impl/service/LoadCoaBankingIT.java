package de.adorsys.ledgers.postings.impl.service;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.api.domain.AccountCategoryBO;
import de.adorsys.ledgers.postings.api.domain.BalanceSideBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.impl.test.PostingsApplication;
import lombok.Data;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class})
@DatabaseSetup("LoadCoaBankingIT-db-entries.xml")
@DatabaseTearDown(value = {"LoadCoaBankingIT-db-delete.xml"}, type = DatabaseOperation.DELETE_ALL)
public class LoadCoaBankingIT {

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private LedgerService ledgerService;

    @Before
    public void before() {
        final YAMLFactory ymlFactory = new YAMLFactory();
        mapper = new ObjectMapper(ymlFactory);
    }

    @Test
    public void test_load_coa_ok() throws IOException, LedgerAccountNotFoundException, LedgerNotFoundException {
        LedgerBO ledger = ledgerService.findLedgerById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        Assume.assumeNotNull(ledger);

        InputStream inputStream = LoadLedgerAccountYMLTest.class.getResourceAsStream("LoadCoaBankingIT-coa.yml");
        LegAccYamlModel[] ledgerAccounts = mapper.readValue(inputStream, LegAccYamlModel[].class);
        for (LegAccYamlModel model : ledgerAccounts) {
        	LedgerAccountBO parent = null;
            if(model.getParent()!=null) {
            	parent = new LedgerAccountBO();
            	parent.setLedger(ledger);
            	parent.setName(model.getParent());
            }
            LedgerAccountBO l = new LedgerAccountBO(); 
            l.setShortDesc(model.getShortDesc());
            l.setName(model.getName());
            l.setBalanceSide(model.getBalanceSide());
            l.setCategory(model.getCategory());
            l.setLedger(ledger);
            l.setParent(parent);
            ledgerService.newLedgerAccount(l);
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
        private AccountCategoryBO category;
        private BalanceSideBO balanceSide;
        private String parent;
    }

}
