package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import de.adorsys.ledgers.postings.PostingsApplication;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ITChartOfAccountRepositoryQuerryTest-db-entries.xml")
public class ITChartOfAccountRepositoryQuerryTest {
	@Autowired
	private ChartOfAccountRepository repo;
	private String name = "CoA";
	private DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	@Test
	public void test2_query_coa_refdate_0808_returns_ci8k8PDcTrCsiF3sT3ig1() {
		LocalDateTime refDate = LocalDateTime.parse("2018-08-08 20:58:24.232", formater);
		Optional<ChartOfAccount> found = repo.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(name, refDate, refDate);
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals("ci8k8PDcTrCsiF3sT3ig1", found.get().getId());
	}

	@Test
	public void test2_query_coa_refdate_0813_returns_ci8k8PDcTrCsiF3sT3ig2() {
		LocalDateTime refDate = LocalDateTime.parse("2018-08-13 20:58:24.232", formater);
		Optional<ChartOfAccount> found = repo.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(name, refDate, refDate);
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals("ci8k8PDcTrCsiF3sT3ig2", found.get().getId());
	}

	@Test
	public void test2_query_coa_refdate_0816_returns_ci8k8PDcTrCsiF3sT3ig3() {
		LocalDateTime refDate = LocalDateTime.parse("2018-08-16 20:58:24.232", formater);
		Optional<ChartOfAccount> found = repo.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(name, refDate, refDate);
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals("ci8k8PDcTrCsiF3sT3ig3", found.get().getId());
	}

	@Test
	public void test2_query_coa_refdate_0902_returns_ci8k8PDcTrCsiF3sT3ig4() {
		LocalDateTime refDate = LocalDateTime.parse("2018-09-02 20:58:24.000", formater);
		Optional<ChartOfAccount> found = repo.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(name, refDate, refDate);
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals("ci8k8PDcTrCsiF3sT3ig4", found.get().getId());
	}

	@Test
	public void test2_query_coa_refdate_0702_returns_optional_empty() {
		LocalDateTime refDate = LocalDateTime.parse("2018-07-02 20:58:24.000", formater);
		Optional<ChartOfAccount> found = repo.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(name, refDate, refDate);
		Assert.assertFalse(found.isPresent());
	}

}
