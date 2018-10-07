package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;

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

import de.adorsys.ledgers.postings.domain.FinancialStmt;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.tests.PostingsApplication;
import de.adorsys.ledgers.utils.Ids;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ITFinancialStmtRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"ITFinancialStmtRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITFinancialStmtRepositoryTest {

	@Autowired
	private LedgerRepository ledgerRepository;
	
	@Autowired
	private FinancialStmtRepository financialStmtRepository;
	
	@Test
	public void test_create_financial_statement_ok() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YabcGwIfZilhumPg").orElseThrow(()-> new IllegalStateException("Missing Ledger with id Zd0ND5YwSzGwIfZilhumPg"));
		FinancialStmt financialStmt = new FinancialStmt(Ids.id(), LocalDateTime.now(), "Francis", "Sample Financial Statement", "Sample Financial Statement",
				"Sample Financial Statement", ledger, LocalDateTime.now(), Ids.id());
		financialStmtRepository.save(financialStmt);
	}

}
