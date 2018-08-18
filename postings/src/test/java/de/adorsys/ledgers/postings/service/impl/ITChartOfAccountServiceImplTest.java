package de.adorsys.ledgers.postings.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;

import java.util.Arrays;
import java.util.List;

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

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountTypeRepository;
import de.adorsys.ledgers.postings.service.ChartOfAccountService;
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
	
	@Autowired
	private LedgerAccountTypeRepository ledgerAccountTypeRepo;
	
	@Autowired
	private ChartOfAccountRepository chartOfAccountRepository;

	@Test
	public void test_new_chart_of_account_root_account_types_reated() {
		ChartOfAccount chartOfAccount = new ChartOfAccount(null, "COA:Loans", null, null, "Chart of account for loan business");
		List<String> rootAccountTypeNames = Arrays.asList("Balance Sheet Accounts", "Profit & Lost Accounts");
		ChartOfAccount coa = chartOfAccountService.newChartOfAccount(chartOfAccount, rootAccountTypeNames);
		coa = chartOfAccountRepository.findById(coa.getId()).orElse(null);
		assumeNotNull(coa);
		List<LedgerAccountType> found = ledgerAccountTypeRepo.findByCoaAndLevel(coa, 0);
		assertEquals(2, found.size());
	}

	@Test
	public void test_find_coa_ledger_account_types(){
		List<LedgerAccountType> found = chartOfAccountService.findCoaLedgerAccountTypes("CoA");
		assertEquals(10, found.size());
	}

	@Test
	public void test_find_coa_root_account_types(){
		List<LedgerAccountType> found = chartOfAccountService.findCoaRootAccountTypes("CoA");
		assertEquals(2, found.size());
	}
	
	@Test
	public void test_find_child_ledger_account_types(){
		// TODO implements 
	}

	@Test
	public void test_find_chart_of_accounts_by_id(){
		// TODO implements 
	}
	
	@Test
	public void test_find_chart_of_accounts_by_name(){
		// TODO implements 
	}

	@Test
	public void test_new_ledger_account_type(){
		// TODO implements 
	}

	@Test
	public void test_find_ledger_account_type_by_id(){
		// TODO implements 
	}

	@Test
	public void test_find_ledger_account_type(){
		// TODO implements 
	}
}
