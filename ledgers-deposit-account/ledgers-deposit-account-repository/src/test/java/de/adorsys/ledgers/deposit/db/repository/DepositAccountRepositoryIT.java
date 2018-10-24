package de.adorsys.ledgers.deposit.db.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.adorsys.ledgers.deposit.db.domain.AccountStatus;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.test.DepositAccountRepositoryApplication;
import de.adorsys.ledgers.util.Ids;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DepositAccountRepositoryApplication.class)
public class DepositAccountRepositoryIT {
	
	@Autowired
	private DepositAccountRepository repo;
	
	@Test
	public void create_da_mandatory_properties() {
		DepositAccount da = new DepositAccount();
		da.setId(Ids.id());
		da.setCurrency("EUR");
		da.setAccountStatus(AccountStatus.ENABLED);
		da.setIban("DE89370400440532013000");
		repo.save(da);
		
	}

}
