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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.domain.AccountCategory;
import de.adorsys.ledgers.postings.domain.BalanceSide;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.tests.PostingsApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLoadCoaIFRSTest-db-entries.xml")
@DatabaseTearDown(value={"ITLoadCoaIFRSTest-db-delete.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITLoadCoaIFRSTest {
	//TODO Ask @fpo of this test
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

        InputStream inputStream = LoadLedgerAccountYMLTest.class.getResourceAsStream("ITLoadCoaIFRSTest-coa.yml");
		LedgerAccount[] ledgerAccounts = mapper.readValue(inputStream, LedgerAccount[].class);
		for (LedgerAccount ledgerAccount : ledgerAccounts) {
			
			if(ledgerAccount.getName()==null)Assert.fail("Missing account name for "+ ledgerAccount.getShortDesc());
			String name = ledgerAccount.getName();

			LedgerAccount parent = null;
			if(name.contains(".")){
				String parentName = name.substring(0, name.lastIndexOf('.'));
				parent = LedgerAccount.builder()
						.ledger(ledger)
						.name(parentName).build();
			}
			ledgerAccount = LedgerAccount.builder()
					.shortDesc(ledgerAccount.getShortDesc())
					.name(ledgerAccount.getName())
					.balanceSide(ledgerAccount.getBalanceSide())
					.category(ledgerAccount.getCategory())
					.ledger(ledger)
					.parent(parent)
					.build();
			ledgerService.newLedgerAccount(ledgerAccount);
		}
		
		LedgerAccount la = ledgerService.findLedgerAccount(ledger, "4.2").orElse(null);
		Assume.assumeNotNull(la);
		Assert.assertEquals("Services",la.getShortDesc());
		Assert.assertEquals(AccountCategory.RE, la.getCategory());
		Assert.assertEquals(BalanceSide.Cr, la.getBalanceSide());
	}

}
