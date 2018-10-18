package de.adorsys.ledgers.postings.service.impl;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
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

import de.adorsys.ledgers.postings.domain.AccountCategory;
import de.adorsys.ledgers.postings.domain.BalanceSide;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.tests.PostingsApplication;
import lombok.Data;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLoadCoaBankingTest-db-entries.xml")
@DatabaseTearDown(value={"ITLoadCoaBankingTest-db-delete.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITLoadCoaBankingTest {
	
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
    private LedgerService ledgerService;

	@Before
	public void before(){
        final YAMLFactory ymlFactory = new YAMLFactory();
        mapper = new ObjectMapper(ymlFactory);
	}
	
	@Test
	public void test_load_coa_ok() throws IOException, NotFoundException{
        Ledger ledger = ledgerService.findLedgerById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        Assume.assumeNotNull(ledger);

        InputStream inputStream = LoadLedgerAccountYMLTest.class.getResourceAsStream("ITLoadCoaBankingTest-coa.yml");
        LegAccYamlModel[] ledgerAccounts = mapper.readValue(inputStream, LegAccYamlModel[].class);
		for (LegAccYamlModel model : ledgerAccounts) {
			try {
				LedgerAccountBO parent = model.getParent()==null?null:LedgerAccountBO.builder().ledger(ledger).name(model.getParent()).build();
				LedgerAccountBO ledgerAccount = LedgerAccountBO.builder()
						.shortDesc(model.getShortDesc())
						.name(model.getName())
						.balanceSide(model.getBalanceSide())
						.category(model.getCategory())
						.ledger(ledger)
						.parent(parent)
						.build();
				ledgerService.newLedgerAccount(ledgerAccount);
			} catch (RuntimeException | NotFoundException e) {
				throw e;
			}
		}
		
		LedgerAccountBO la = ledgerService.findLedgerAccount(ledger, "1003").orElse(null);
		Assume.assumeNotNull(la);
		Assert.assertEquals("Cash in transit",la.getShortDesc());
		Assert.assertEquals(AccountCategory.AS, la.getCategory());
		Assert.assertEquals(BalanceSide.Dr, la.getBalanceSide());
	}
	
	@Data
	static class LegAccYamlModel {
		private String shortDesc;
		private String name;
		private AccountCategory category;
		private BalanceSide balanceSide;
		private String parent;
	}

}
