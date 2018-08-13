package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
import com.github.springtestdbunit.annotation.DatabaseSetup;

import de.adorsys.ledgers.postings.PostingsApplication;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.utils.Ids;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLedgerAccountTypeRepositoryTest-db-entries.xml")
public class ITLedgerAccountTypeRepositoryTest {

	@Autowired
	private ChartOfAccountRepository chartOfAccountRepository;
	
	@Autowired
	private LedgerAccountTypeRepository ledgerAccountTypeRepository;
	private DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	@Test
	public void test_create_ledger_account_type_ok() {
		ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8PDcTrCsi-F3sT3i-g").orElse(null);
		Assume.assumeNotNull(coa);
		LedgerAccountType ledgerAccountType = LedgerAccountType.builder().id(Ids.id())
				.name("Sample Ledger Account Type").user("Sample User")
				.validFrom(LocalDateTime.now()).coa(coa.getName()).parent("Sample Ledger Account Type").build();
		ledgerAccountTypeRepository.save(ledgerAccountType);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_type_no_coa() {
		LedgerAccountType ledgerAccountType = LedgerAccountType.builder().id(Ids.id())
				.name("Sample Ledger Account Type").user("Sample User")
				.parent("Sample Ledger Account Type")
				.validFrom(LocalDateTime.now()).build();
		ledgerAccountTypeRepository.save(ledgerAccountType);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_type_unique_constrain_violation_name_validFrom() {
		LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findById("805UO1hITP-HxQq16OuGvw").orElse(null);
		Assume.assumeNotNull(ledgerAccountType);
		LedgerAccountType ledgerAccountType2 = LedgerAccountType.builder().id(Ids.id())
				.name(ledgerAccountType.getName())
				.user("Sample User")
				.validFrom(ledgerAccountType.getValidFrom())
				.parent(ledgerAccountType.getName())
				.coa(ledgerAccountType.getCoa()).build();
		ledgerAccountTypeRepository.save(ledgerAccountType2);
	}
	
	String valid_before_08_12_id="805UO1hITPHxQq16OuGvw_BS_A_RC";
	String valid_after_08_12_id="805UO1hITPHxQq16OuGvw_BS_A_RC0";
	String payablesAccountTypeName = "Payables";
	String liabilitiesAccountTypeName = "Liabilities";
	@Test
	public void test_find_by_parent_Liabilities_on_2018_08_10_returns_valid_to_2018_08_12() {
		LocalDateTime refDate = LocalDateTime.parse("2018-08-10 20:58:24.232", formater);
		List<LedgerAccountType> found = ledgerAccountTypeRepository.findByParentAndValidFromBeforeAndValidToAfterOrderByLevelDescValidFromDesc(liabilitiesAccountTypeName, refDate, refDate);
		Assert.assertEquals(3, found.size());
		LedgerAccountType payablesAccType = null;
		for (LedgerAccountType lat : found) {
			if(payablesAccountTypeName.equals(lat.getName())) {
				payablesAccType = lat;
			}
		}
		Assert.assertNotNull(payablesAccType);
		Assert.assertEquals(valid_before_08_12_id, payablesAccType.getId());
	}
	
	@Test
	public void test_find_by_parent_Liabilities_on_2018_08_14_returns_valid_to_2199_01_01() {
		LocalDateTime refDate = LocalDateTime.parse("2018-08-14 20:58:24.232", formater);
		List<LedgerAccountType> found = ledgerAccountTypeRepository.findByParentAndValidFromBeforeAndValidToAfterOrderByLevelDescValidFromDesc(liabilitiesAccountTypeName, refDate, refDate);
		Assert.assertEquals(3, found.size());
		LedgerAccountType payablesAccType = null;
		for (LedgerAccountType lat : found) {
			if(payablesAccountTypeName.equals(lat.getName())) {
				payablesAccType = lat;
			}
		}
		Assert.assertNotNull(payablesAccType);
		Assert.assertEquals(valid_after_08_12_id, payablesAccType.getId());
	}
}
