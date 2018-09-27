package de.adorsys.ledgers.postings.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.domain.BalanceSide;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.service.ChartOfAccountService;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.tests.PostingsApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,DbUnitTestExecutionListener.class})
@DatabaseSetup("ITChartOfAccountServiceImplTest-db-entries.xml")
@DatabaseTearDown(value={"ITChartOfAccountServiceImplTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITChartOfAccountServiceImplTest {
	
	@Autowired
	private ChartOfAccountService chartOfAccountService;

	@Test
	public void test_find_coa_ledger_account_types(){
		Optional<ChartOfAccount> found = chartOfAccountService.findChartOfAccountsByName("CoA");
		assertEquals(true, found.isPresent());
	}

	@Test
	public void test_find_coa_root_account_types(){
		ChartOfAccount coa = chartOfAccountService.findChartOfAccountsByName("CoA").orElse(null);
		assumeNotNull(coa);
		List<LedgerAccountType> found = chartOfAccountService.findCoaAccountTypesByLevel(coa, 0);
		assertEquals(2, found.size());
	}
	
	@Test
	public void test_find_child_ledger_account_types(){
		ChartOfAccount coa = chartOfAccountService.findChartOfAccountsByName("CoA").orElse(null);
		assumeNotNull(coa);

		List<LedgerAccountType> childrenAccountTypes = chartOfAccountService.findChildLedgerAccountTypes(coa,"BS");
		assertEquals(3, childrenAccountTypes.size());
	}

	@Test
	public void test_find_chart_of_accounts_by_id() {
		Optional<ChartOfAccount> coa = chartOfAccountService.findChartOfAccountsById("ci8k8PDcTrCsi-F3sT3i-g");
		assertTrue(coa.isPresent());
	}
	
	@Test
	public void test_find_chart_of_accounts_by_name(){
		Optional<ChartOfAccount> coa = chartOfAccountService.findChartOfAccountsByName("CoA");
		assertTrue(coa.isPresent());
		//ibo am Werk gewesen
	}

	@Test
	public void test_new_ledger_account_type(){
		ChartOfAccount coa = chartOfAccountService.findChartOfAccountsByName("CoA").orElse(null);
		assumeNotNull(coa);

		// root account type
		LedgerAccountType newRootLedgerAccountType = LedgerAccountType.builder()
				.id(Ids.id())
				.coa(coa)
				.created(LocalDateTime.now())
				.user("Vladimir")
				.level(1)
				.balanceSide(BalanceSide.DC)
				.name("My new test root account type")
				.shortDesc("My new test account type short description")
				.build();
		LedgerAccountType returnedClone = this.chartOfAccountService.newLedgerAccountType(newRootLedgerAccountType);
		assertNotNull(returnedClone);

		// account type with parent
		LedgerAccountType newLedgerAccountType = LedgerAccountType.builder()
				.id(Ids.id())
				.coa(coa)
				.parent(returnedClone)
				.created(LocalDateTime.now())
				.user("Vladimir")
				.level(1)
				.balanceSide(BalanceSide.D)
				.name("My new test account type")
				.shortDesc("My new test account type short description")
				.build();
		returnedClone = this.chartOfAccountService.newLedgerAccountType(newLedgerAccountType);
		assertNotNull(returnedClone);
	}

	@Test
	public void test_find_ledger_account_type_by_id(){
		Optional<LedgerAccountType> ledgerAccountType = chartOfAccountService.findLedgerAccountTypeById("805UO1hITPHxQq16OuGvw_BS");
		assertTrue(ledgerAccountType.isPresent());
	}

	@Test
	public void test_find_ledger_account_type(){
		Optional<ChartOfAccount> coa = chartOfAccountService.findChartOfAccountsByName("CoA");
		assertTrue(coa.isPresent());

		Optional<LedgerAccountType> ledgerAccountType = chartOfAccountService.findLedgerAccountType(coa.get(), "BS");
		assertTrue(ledgerAccountType.isPresent());
	}
}
