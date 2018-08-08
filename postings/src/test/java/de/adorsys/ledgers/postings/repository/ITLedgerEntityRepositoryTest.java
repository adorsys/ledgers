package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.adorsys.ledgers.postings.PostingsApplication;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.utils.Ids;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
public class ITLedgerEntityRepositoryTest {
	@Autowired
	private ChartOfAccountRepository chartOfAccountRepository;

	@Test
	public void test_create_coa_create_timestamp_auto_generated() {
		String id = Ids.id();
		LocalDateTime now = LocalDateTime.now();
		ChartOfAccount coa = ChartOfAccount.builder().id(id).name("CoA").user("Francis").validFrom(now).build();
		chartOfAccountRepository.save(coa);
		coa = chartOfAccountRepository.findById(id).orElse(null);
		Assume.assumeNotNull(coa);
		Assert.assertNotNull(coa.getCreated());
	}
}
