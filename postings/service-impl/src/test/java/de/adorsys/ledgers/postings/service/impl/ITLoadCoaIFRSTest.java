package de.adorsys.ledgers.postings.service.impl;

import java.io.IOException;
import java.io.InputStream;

import de.adorsys.ledgers.postings.converter.LedgerAccountMapper;
import de.adorsys.ledgers.postings.converter.LedgerMapper;
import de.adorsys.ledgers.postings.domain.*;
import de.adorsys.ledgers.postings.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerNotFoundException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
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
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.tests.PostingsApplication;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLoadCoaIFRSTest-db-entries.xml")
@DatabaseTearDown(value={"ITLoadCoaIFRSTest-db-delete.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITLoadCoaIFRSTest {
	//TODO Ask @fpo of this test
	private ObjectMapper mapper = new ObjectMapper();
	private static final LedgerMapper ledgerMapper = Mappers.getMapper(LedgerMapper.class);
    private static final LedgerAccountMapper ledgerAccountMapper = Mappers.getMapper(LedgerAccountMapper.class);

    @Autowired
    private LedgerService ledgerService;

	@Before
	public void before(){
        final YAMLFactory ymlFactory = new YAMLFactory();
        mapper = new ObjectMapper(ymlFactory);
	}
	
	@Test
	public void test_load_coa_ok() throws IOException, LedgerAccountNotFoundException, LedgerNotFoundException {
        LedgerBO ledgerBO = ledgerService.findLedgerById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        Assume.assumeNotNull(ledgerBO);
		Ledger ledger =ledgerMapper.toLedger(ledgerBO);
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
			ledgerService.newLedgerAccount(ledgerAccountMapper.toLedgerAccountBO(ledgerAccount));
		}
		
		LedgerAccountBO la = ledgerService.findLedgerAccount(ledgerMapper.toLedgerBO(ledger), "4.2").orElse(null);
		Assume.assumeNotNull(la);
		Assert.assertEquals("Services",la.getShortDesc());
		Assert.assertEquals(AccountCategoryBO.RE, la.getCategory());
		Assert.assertEquals(BalanceSideBO.Cr, la.getBalanceSide());
	}

}
