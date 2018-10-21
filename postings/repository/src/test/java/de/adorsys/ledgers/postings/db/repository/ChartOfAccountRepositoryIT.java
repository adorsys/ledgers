package de.adorsys.ledgers.postings.db.repository;

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

import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.tests.PostingsApplication;
import de.adorsys.ledgers.util.Ids;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ChartOfAccountRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"ChartOfAccountRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ChartOfAccountRepositoryIT {
	@Autowired
	private ChartOfAccountRepository chartOfAccountRepository;

	@Test
	public void test_create_coa_ok() {
		ChartOfAccount coa = new ChartOfAccount(Ids.id(), null, "francis", null, null, Ids.id()); 
		chartOfAccountRepository.save(coa);
	}

	@Test
	public void test_load_coa() {
		ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8bcdTrCsi-F3sT3i-g").orElse(null);
		Assert.assertNotNull(coa);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_coa_unique_constrain_violation_name() {
		ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8bcdTrCsi-F3sT3i-g").orElse(null);
		Assume.assumeNotNull(coa);
		ChartOfAccount coa2 = new ChartOfAccount(Ids.id(), null, "Francis", null, null, coa.getName()); 
		chartOfAccountRepository.save(coa2);
	}
}
