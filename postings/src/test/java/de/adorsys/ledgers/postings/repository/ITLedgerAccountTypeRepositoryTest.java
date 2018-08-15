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

import de.adorsys.ledgers.postings.PostingsApplication;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.utils.Ids;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLedgerAccountTypeRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"ITLedgerAccountTypeRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITLedgerAccountTypeRepositoryTest {

	@Autowired
	private ChartOfAccountRepository chartOfAccountRepository;
	
	@Autowired
	private LedgerAccountTypeRepository ledgerAccountTypeRepository;

	@Test
	public void test_create_ledger_account_type_ok() {
		ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8PDcTrCsi-F3sT3i-g").orElse(null);
		Assume.assumeNotNull(coa);
		LedgerAccountType ledgerAccountType = LedgerAccountType.builder().id(Ids.id())
				.name("Sample Ledger Account Type").user("Sample User")
				.coa(coa).parent("Sample Ledger Account Type").build();
		ledgerAccountTypeRepository.save(ledgerAccountType);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_type_no_coa() {
		LedgerAccountType ledgerAccountType = LedgerAccountType.builder().id(Ids.id())
				.name("Sample Ledger Account Type").user("Sample User")
				.parent("Sample Ledger Account Type")
				.build();
		ledgerAccountTypeRepository.save(ledgerAccountType);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_type_unique_constrain_violation_name_validFrom() {
		LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findById("805UO1hITP-HxQq16OuGvw").orElse(null);
		Assume.assumeNotNull(ledgerAccountType);
		LedgerAccountType ledgerAccountType2 = LedgerAccountType.builder().id(Ids.id())
				.name(ledgerAccountType.getName())
				.user("Sample User")
				.parent(ledgerAccountType.getName())
				.coa(ledgerAccountType.getCoa()).build();
		ledgerAccountTypeRepository.save(ledgerAccountType2);
	}

}
