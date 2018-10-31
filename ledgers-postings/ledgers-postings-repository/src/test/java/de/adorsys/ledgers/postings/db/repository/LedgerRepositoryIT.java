package de.adorsys.ledgers.postings.db.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
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

import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.tests.PostingRepositoryApplication;
import de.adorsys.ledgers.util.Ids;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingRepositoryApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("LedgerRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"LedgerRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class LedgerRepositoryIT {

	@Autowired
	private ChartOfAccountRepository chartOfAccountRepository;
	
	@Autowired
	private LedgerRepository ledgerRepository;
	
	@Test
	public void test_create_ledger_ok() {
		ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8PDcTrCsi-F3sT3i-g").orElse(null);
		Assume.assumeNotNull(coa);
		String id = Ids.id();
		LocalDateTime created = LocalDateTime.now();
		String user = "Sample User";
		String shortDesc = null;
		String longDesc = null;
		String name = "Sample Ledger-2";
		Ledger ledger = new Ledger(id, created, user, shortDesc, longDesc, name, coa); 
		ledgerRepository.save(ledger);
	}
	
	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_no_coa() {
		ChartOfAccount coa = null; 
		String id = Ids.id();
		LocalDateTime created = null;
		String user = "Sample User";
		String shortDesc = null;
		String longDesc = null;
		String name = "Sample Ledger-2";
		Ledger ledger = new Ledger(id, created, user, shortDesc, longDesc, name, coa); 
		ledgerRepository.save(ledger);

	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_no_name() {
        ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8zskTrCsi-F3sT3i-g").orElse(null);
		String id = Ids.id();
		LocalDateTime created = null;
		String user = "Sample User";
		String shortDesc = null;
		String longDesc = null;
		String name = null;
		Ledger ledger = new Ledger(id, created, user, shortDesc, longDesc, name, coa); 
        ledgerRepository.save(ledger);
	}	

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_unique_constrain_violation_name() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		ChartOfAccount coa = chartOfAccountRepository.findById(ledger.getCoa().getId()).orElse(null);
		Assume.assumeNotNull(coa);
		String id = Ids.id();
		LocalDateTime created = null;
		String user = "Sample User";
		String shortDesc = null;
		String longDesc = null;
		String name = ledger.getName();
		Ledger ledger2 = new Ledger(id, created, user, shortDesc, longDesc, name, coa); 
		ledgerRepository.save(ledger2);
	}

	@Test
	public void test_find_by_coa_returns_n_records(){
        Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        Assume.assumeNotNull(ledger);
        ChartOfAccount coa = chartOfAccountRepository.findById(ledger.getCoa().getId()).orElse(null);
        List<Ledger> list = ledgerRepository.findByCoa(coa);
        Assert.assertEquals(2, list.size());
	}
	
	@Test
	public void test_find_by_name_returns_one_record(){
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		Optional<Ledger> opt = ledgerRepository.findOptionalByName(ledger.getName());
		Assert.assertTrue(opt.isPresent());

	}

	@Test
	public void test_find_by_name_not_found(){
		Optional<Ledger> opt = ledgerRepository.findOptionalByName("wrongName");
		Assert.assertFalse(opt.isPresent());	}
}
