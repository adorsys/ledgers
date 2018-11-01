package de.adorsys.ledgers.postings.db.repository;

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

import de.adorsys.ledgers.postings.db.domain.AccountCategory;
import de.adorsys.ledgers.postings.db.domain.BalanceSide;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.tests.PostingRepositoryApplication;
import de.adorsys.ledgers.util.Ids;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingRepositoryApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("LedgerAccountRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"LedgerAccountRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class LedgerAccountRepositoryIT {
	
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
		
		LedgerAccount ledgerAccount = new LedgerAccount(Ids.id(), LocalDateTime.now(), "Sample User", "Long lasting liability", 
				"Long lasting liability (from 1 year to 3 years)", "Long lasting liability", ledger, parentAccount, ledger.getCoa(), AccountCategory.LI.getDefaultBs(), AccountCategory.LI);
		ledgerAccountRepository.save(ledgerAccount);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_no_ledger() {
		String id = Ids.id();
		String name = "Sample Ledger Account";
		String user = "Sample User";
		LocalDateTime created = null;
		LedgerAccount parent=null;
		Ledger ledger = null;
		String shortDesc = null;
		String longDesc = null;
		ChartOfAccount coa = null;
		AccountCategory category = null;
		BalanceSide balanceSide = null;
		LedgerAccount ledgerAccount = new LedgerAccount(id, created, user, shortDesc, longDesc, name, ledger, parent, coa, balanceSide, category);

		ledgerAccountRepository.save(ledgerAccount);
	}
	
	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_unique_constrain_violation_ledger_name_validFrom() {
		LedgerAccount ledgerAccount = ledgerAccountRepository.findById("xVgaTPMcRty9ik3BTQDh1Q_BS_3_0_0").orElse(null);
		Assume.assumeNotNull(ledgerAccount);
		
		String id = Ids.id();
		LocalDateTime created = null;
		String name = ledgerAccount.getName();
		String user = "Sample User";
		LedgerAccount parent=ledgerAccount.getParent();
		Ledger ledger = ledgerAccount.getLedger();
		String shortDesc = "Long lasting liability";
		String longDesc = "Long lasting liability (from 1 year to 3 years)";
		ChartOfAccount coa = ledgerAccount.getLedger().getCoa();
		AccountCategory category = AccountCategory.LI;
		BalanceSide balanceSide = AccountCategory.LI.getDefaultBs();
		LedgerAccount ledgerAccount2 = new LedgerAccount(id, created, user, shortDesc, longDesc, name, ledger, parent, coa, balanceSide, category);

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
