package de.adorsys.ledgers.deposit.repository;

import static org.junit.Assert.fail;

import java.util.Currency;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountStatus;
import de.adorsys.ledgers.deposit.domain.DepositAccount;
import de.adorsys.ledgers.deposit.test.DapositAccountApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DapositAccountApplication.class)
//@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
//    TransactionalTestExecutionListener.class,
//    DbUnitTestExecutionListener.class})
//@DatabaseSetup("ITChartOfAccountRepositoryTest-db-entries.xml")
//@DatabaseTearDown(value={"ITChartOfAccountRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITDepositAccountRepositoryTest {

	@Autowired
	private DepositAccountRepository repository;
	@Test
	public void test_create_deposit_account_ok() {
		DepositAccount da = DepositAccount.builder()
			.id(UUID.randomUUID().toString())
			.iban("345234523")
			.currency(Currency.getInstance("EUR"))
			.accountStatus(SpiAccountStatus.ENABLED)
			.build();
		repository.save(da);

	}

}
