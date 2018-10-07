package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;
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

import de.adorsys.ledgers.postings.domain.AccountCategory;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.tests.PostingsApplication;
import de.adorsys.ledgers.utils.Ids;

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

	@Test
	public void test_create_ledger_account_ok() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		
		LedgerAccount parentAccount = ledgerAccountRepository.findOptionalByLedgerAndName(ledger, "3.0.0").orElse(null);
		Assume.assumeNotNull(parentAccount);
		
		LedgerAccount ledgerAccount = LedgerAccount.builder()
				.id(Ids.id())
				.created(LocalDateTime.now())
				.user("Sample User")
				.shortDesc("Long lasting liability")
				.longDesc("Long lasting liability (from 1 year to 3 years)")
				.name("Long lasting liability")
				.ledger(ledger)
				.coa(ledger.getCoa())
				.category(AccountCategory.LI)
				.balanceSide(AccountCategory.LI.getDefaultBs())
				.parent(parentAccount)
				.build();
		ledgerAccountRepository.save(ledgerAccount);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_no_ledger() {
		LedgerAccount ledgerAccount = LedgerAccount.builder().id(Ids.id())
				.name("Sample Ledger Account")
				.user("Sample User")
				.build();
		ledgerAccountRepository.save(ledgerAccount);
	}
	
	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_unique_constrain_violation_ledger_name_validFrom() {
		LedgerAccount ledgerAccount = ledgerAccountRepository.findById("xVgaTPMcRty9ik3BTQDh1Q_BS_3_0_0").orElse(null);
		Assume.assumeNotNull(ledgerAccount);
		LedgerAccount ledgerAccount2 = LedgerAccount.builder()
				.id(Ids.id())
				.name(ledgerAccount.getName())
				.user("Sample User")
				.parent(ledgerAccount.getParent())
				.ledger(ledgerAccount.getLedger())
				.shortDesc("Long lasting liability")
				.longDesc("Long lasting liability (from 1 year to 3 years)")
				.coa(ledgerAccount.getLedger().getCoa())
				.category(AccountCategory.LI)
				.balanceSide(AccountCategory.LI.getDefaultBs())
				.build();

		ledgerAccountRepository.save(ledgerAccount2);
	}

	@Test
	public void test_find_by_ledger_and_name_ok() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		Optional<LedgerAccount> found = ledgerAccountRepository.findOptionalByLedgerAndName(ledger, "1.0.0");
		Assert.assertTrue(found.isPresent());
	}
}
