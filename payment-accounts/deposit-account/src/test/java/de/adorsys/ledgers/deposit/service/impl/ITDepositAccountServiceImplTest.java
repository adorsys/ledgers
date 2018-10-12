package de.adorsys.ledgers.deposit.service.impl;

import static org.junit.Assert.*;

import java.util.Currency;

import org.junit.Ignore;
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

import de.adorsys.ledgers.deposit.domain.AccountStatus;
import de.adorsys.ledgers.deposit.domain.AccountType;
import de.adorsys.ledgers.deposit.domain.AccountUsage;
import de.adorsys.ledgers.deposit.domain.DepositAccount;
import de.adorsys.ledgers.deposit.service.DepositAccountService;
import de.adorsys.ledgers.deposit.test.DepositAccountApplication;
import de.adorsys.ledgers.postings.exception.NotFoundException;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DepositAccountApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ITDepositAccountServiceImplTest-db-entries.xml")
@DatabaseTearDown(value={"ITDepositAccountServiceImplTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITDepositAccountServiceImplTest extends DepositAccountServiceImpl {

	@Autowired
	private DepositAccountService depositAccountService;
	
	@Test
	public void test_create_customer_bank_account_ok() throws NotFoundException {
		DepositAccount da = DepositAccount.builder()
			.iban("DE91100000000123456789")
			.currency(Currency.getInstance("EUR"))
			.name("Account mykola")
			.product("Deposit Account")
			.accountType(AccountType.CASH)
			.accountStatus(AccountStatus.ENABLED)
			.usageType(AccountUsage.PRIV)
			.details("Mykola's Account").build();
		
		DepositAccount createdDepositAccount = depositAccountService.createDepositAccount(da);
		assertNotNull(createdDepositAccount);
	}
}
