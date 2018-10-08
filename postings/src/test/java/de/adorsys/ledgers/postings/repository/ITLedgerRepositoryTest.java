package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
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
import de.adorsys.ledgers.tests.PostingsApplication;
import de.adorsys.ledgers.util.Ids;

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
		ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8PDcTrCsi-F3sT3i-g").orElse(null);
		Assume.assumeNotNull(coa);
		Ledger ledger = Ledger.builder().id(Ids.id()).name("Sample Ledger-2")
				.user("Sample User").coa(coa).lastClosing(LocalDateTime.now()).build();
		ledgerRepository.save(ledger);
	}
	
	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_no_coa() {
		Ledger ledger = Ledger.builder().id(Ids.id()).name("Sample Ledger-2").user("Sample User").build();
		ledgerRepository.save(ledger);

	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_no_name() {
        ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8zskTrCsi-F3sT3i-g").orElse(null);
        Ledger ledger = Ledger.builder().id(Ids.id()).user("Sample User").coa(coa).build();
        ledgerRepository.save(ledger);
	}	

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_unique_constrain_violation_name() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		ChartOfAccount coa = chartOfAccountRepository.findById(ledger.getCoa().getId()).orElse(null);
		Assume.assumeNotNull(coa);
		Ledger ledger2 = Ledger.builder().id(Ids.id()).name("Sample Ledger-0").user("Sample User").coa(coa).build();
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
