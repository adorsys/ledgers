package de.adorsys.ledgers.postings.db.repository;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.tests.PostingRepositoryApplication;
import de.adorsys.ledgers.util.Ids;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingRepositoryApplication.class)
public class LedgerEntityRepositoryIT {
	@Autowired
	private ChartOfAccountRepository chartOfAccountRepository;

	@Test
	public void test_create_coa_create_timestamp_auto_generated() {
		String id = Ids.id();
		
		LocalDateTime created = null;
		String user = "Francis";
		String shortDesc = null;
		String longDesc = null;
		String name = id;
		ChartOfAccount coa = new ChartOfAccount(id, created, user, shortDesc, longDesc, name);
		
		chartOfAccountRepository.save(coa);
		coa = chartOfAccountRepository.findById(id).orElse(null);
		Assume.assumeNotNull(coa);
		Assert.assertNotNull(coa.getCreated());
	}
}
