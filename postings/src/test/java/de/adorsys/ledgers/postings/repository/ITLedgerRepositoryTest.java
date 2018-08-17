package de.adorsys.ledgers.postings.repository;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
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
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.tests.PostingsApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLedgerRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"ITLedgerRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITLedgerRepositoryTest {

	@Autowired
	private ChartOfAccountRepository chartOfAccountRepository;
	
	@Autowired
	private LedgerRepository ledgerRepository;
	
	@Test
	public void test_create_ledger_ok() {
		ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8zskTrCsi-F3sT3i-g").orElse(null);
		Ledger ledger = Ledger.builder().id(Ids.id()).name("Sample Ledger-2").user("Sample User").coa(coa).build();
		ledgerRepository.save(ledger);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_unique_constrain_violation_name_validFrom() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		ChartOfAccount coa = chartOfAccountRepository.findById(ledger.getCoa().getId()).orElse(null);
		Assume.assumeNotNull(coa);
		Ledger ledger2 = Ledger.builder().id(Ids.id()).name("Sample Ledger-0").user("Sample User").coa(coa).build();
		ledgerRepository.save(ledger2);
	}
}
