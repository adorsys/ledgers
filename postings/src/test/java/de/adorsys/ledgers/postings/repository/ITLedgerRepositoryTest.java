package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;

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
import com.github.springtestdbunit.annotation.DatabaseSetup;

import de.adorsys.ledgers.postings.PostingsApplication;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.utils.Ids;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLedgerRepositoryTest-db-entries.xml")
public class ITLedgerRepositoryTest {

	@Autowired
	private ChartOfAccountRepository chartOfAccountRepository;
	
	@Autowired
	private LedgerRepository ledgerRepository;
	
	@Test
	public void test_create_ledger_ok() {
		ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8PDcTrCsi-F3sT3i-g").orElse(null);
		Ledger ledger = Ledger.builder().id(Ids.id()).name("Sample Ledger").user("Sample User").validFrom(LocalDateTime.now()).coa(coa.getName()).build();
		ledgerRepository.save(ledger);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_unique_constrain_violation_name_validFrom() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		Ledger ledger2 = Ledger.builder().id(Ids.id()).name("Sample Ledger").user("Sample User").validFrom(ledger.getValidFrom()).coa(ledger.getName()).build();
		ledgerRepository.save(ledger2);
	}
}
