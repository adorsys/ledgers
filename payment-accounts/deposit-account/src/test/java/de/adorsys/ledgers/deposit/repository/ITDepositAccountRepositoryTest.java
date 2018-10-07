package de.adorsys.ledgers.deposit.repository;

import java.util.Currency;

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

import de.adorsys.ledgers.deposit.domain.AccountStatus;
import de.adorsys.ledgers.deposit.domain.DepositAccount;
import de.adorsys.ledgers.deposit.test.DepositAccountApplication;
import de.adorsys.ledgers.utils.Ids;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DepositAccountApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ITDepositAccountRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"ITDepositAccountRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITDepositAccountRepositoryTest {

	@Autowired
	private DepositAccountRepository repository;
	@Test
	public void test_create_deposit_account_ok() {

		DepositAccount da = DepositAccount.builder()
			.id(Ids.id())
			.iban("345234523")
			.currency(Currency.getInstance("EUR"))
			.accountStatus(AccountStatus.ENABLED)
			.build();

		DepositAccount depositAccount = repository.save(da);
		repository.delete(depositAccount);
	}
	
	@Test
	public void test_load_account_returns_ok () {
		DepositAccount depositAccount = repository.findById("ci8k8PDcTrCsiF3sT3ig").orElse(null);
		Assert.assertNotNull(depositAccount);
	}

	@Test
	public void test_find_all_account_returns_n () {
		Iterable<DepositAccount> iter = repository.findAll();
		Assert.assertTrue(iter.iterator().hasNext());
	}
	
	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_deposit_account_unique_constrain_violation_iban_currency() {
		DepositAccount depositAccount = repository.findById("ci8k8PDcTrCsiF3sT3ig").orElse(null);
		Assume.assumeNotNull(depositAccount);
		DepositAccount da = DepositAccount.builder()
				.id(Ids.id())
				.iban(depositAccount.getIban())
				.currency(depositAccount.getCurrency())
				.accountStatus(AccountStatus.ENABLED)
				.build();

			repository.save(da);
	}
	
}
