package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.tests.PostingsApplication;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLedgerAccountRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"ITLedgerAccountRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITLedgerAccountRepositoryTest {
	
	@Autowired
	private LedgerAccountRepository ledgerAccountRepository;
	
	@Autowired
	private LedgerRepository ledgerRepository;

	@Autowired
	private LedgerAccountTypeRepository ledgerAccountTypeRepository;
	
	@Test
	public void test_create_ledger_account_ok() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		// Type of Balance sheet account
		LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findById("805UO1hITPHxQq16OuGvw_BS").orElse(null);
		Assume.assumeNotNull(ledgerAccountType);
		
		LedgerAccount ledgerAccount = LedgerAccount.builder()
				.id(Ids.id())
				.created(LocalDateTime.now())
				.user("Sample User")
				.shortDesc("Long lasting liability")
				.longDesc("Long lasting liability (from 1 year to 3 years)")
				.name("Long lasting liability")
				.validFrom(LocalDateTime.now())
				.validTo(LocalDateTime.now())
				.ledger(ledger)
				.accountType(ledgerAccountType)
				.parent("Sample Ledger Account")

				.build();
		ledgerAccountRepository.save(ledgerAccount);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_no_ledger() {
		LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findById("805UO1hITPHxQq16OuGvw_BS").orElse(null);
		Assume.assumeNotNull(ledgerAccountType);
		LedgerAccount ledgerAccount = LedgerAccount.builder().id(Ids.id())
				.name("Sample Ledger Account")
				.user("Sample User")
				.parent("Sample Ledger Account")
				.accountType(ledgerAccountType)
				.validFrom(LocalDateTime.now()).build();
		ledgerAccountRepository.save(ledgerAccount);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_no_type() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		LedgerAccount ledgerAccount = LedgerAccount.builder().id(Ids.id())
				.name("Sample Ledger Account")
				.user("Sample User")
				.parent("Sample Ledger Account")
				.ledger(ledger)
				.validFrom(LocalDateTime.now()).build();
		ledgerAccountRepository.save(ledgerAccount);
	}
	
	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_unique_constrain_violation_ledger_name_validFrom() {
		LedgerAccount ledgerAccount = ledgerAccountRepository.findById("xVgaTPMcRty9ik3BTQDh1Q_BS").orElse(null);
		Assume.assumeNotNull(ledgerAccount);
		LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findById("805UO1hITPHxQq16OuGvw_BS").orElse(null);
		Assume.assumeNotNull(ledgerAccountType);
		LedgerAccount ledgerAccount2 = LedgerAccount.builder().id(Ids.id())
				.name(ledgerAccount.getName())
				.user("Sample User")
				.validFrom(ledgerAccount.getValidFrom())
				.parent(ledgerAccount.getName())
				.accountType(ledgerAccountType)
				.ledger(ledgerAccount.getLedger()).build();
		ledgerAccountRepository.save(ledgerAccount2);
	}

	@Test
	public void test_find_by_ledger_and_name_ok() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		List<LedgerAccount> found = ledgerAccountRepository.findByLedgerAndName(ledger, "BS");
		assertEquals(1, found.size());
	}

	@Test
	public void test_find_by_ledger_and_level_and_account_type_and_valid_from_before_and_valid_to_after(){
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findById("805UO1hITPHxQq16OuGvw_BS").orElse(null);
		Assume.assumeNotNull(ledgerAccountType);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		LocalDateTime validFrom = LocalDateTime.parse("2018-08-07 23:50:41.231", formatter);
		LocalDateTime validTo = LocalDateTime.parse("2199-01-01 00:00:00.000", formatter);

		List<LedgerAccount> found = ledgerAccountRepository
				.findByLedgerAndLevelAndAccountTypeAndValidFromBeforeAndValidToAfter(ledger, 0, ledgerAccountType, validFrom, validTo);
		assertEquals(1, found.size());

	}
}
