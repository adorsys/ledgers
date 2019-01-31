package de.adorsys.ledgers.um.db.repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.ledgers.um.db.domain.AisConsentEntity;
import de.adorsys.ledgers.um.db.test.UmRepositoryApplication;
import de.adorsys.ledgers.util.Ids;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UmRepositoryApplication.class)
public class AisConsentRepositoryIT {
	@Autowired
	private AisConsentRepository repo; 

	@Test
	public void test_create_consent() {
		AisConsentEntity bo = new AisConsentEntity();
//		AisAccountAccessInfo access = new AisAccountAccessInfo();
//		bo.setAccess(access);
		List<String> list = Arrays.asList("DE80760700240271232400");
		bo.setAccounts(list);
		bo.setTransactions(list);
		bo.setBalances(list);
		bo.setFrequencyPerDay(4);
		bo.setId(Ids.id());
		bo.setRecurringIndicator(true);
		bo.setTppId(Ids.id());
		bo.setUserId(Ids.id());
		bo.setValidUntil(LocalDate.now());
		AisConsentEntity save = repo.save(bo);
		Assert.assertNotNull(save.getAccounts());
		Assert.assertTrue(save.getAccounts().size()==1);
	}

}
