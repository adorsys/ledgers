package de.adorsys.ledgers.postings.db.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

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

import de.adorsys.ledgers.postings.db.domain.AccountStmt;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.StmtStatus;
import de.adorsys.ledgers.postings.db.tests.PostingRepositoryApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingRepositoryApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("AccountStmtRepositoryIT-db-entries.xml")
@DatabaseTearDown(value={"AccountStmtRepositoryIT-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class AccountStmtRepositoryIT {

	@Autowired
	private LedgerAccountRepository ledgerAccountRepository;
	
	@Autowired
	private AccountStmtRepository financialStmtRepository;
	
	@Test
	public void test_create_financial_statement_ok() {
		LedgerAccount account = ledgerAccountRepository.findById("xVgaTPMcRty9ik3BTQDh1Q_BS_1_0_0").orElseThrow(()-> new IllegalStateException("Missing LedgerAccount with id xVgaTPMcRty9ik3BTQDh1Q_BS_1_0_0"));
		AccountStmt f = new AccountStmt();
		f.setStmtStatus(StmtStatus.SIMULATED);
		f.setAccount(account);
		f.setStmtSeqNbr(0);
		LocalDateTime pstTime = LocalDateTime.of(2017, Month.DECEMBER, 31, 23, 59);
		f.setPstTime(pstTime);
		f.setTotalCredit(BigDecimal.ZERO);
		f.setTotalDebit(BigDecimal.ZERO);
		financialStmtRepository.save(f);
	}

}
