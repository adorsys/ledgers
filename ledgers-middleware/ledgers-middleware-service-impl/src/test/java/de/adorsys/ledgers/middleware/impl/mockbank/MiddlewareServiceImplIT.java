package de.adorsys.ledgers.middleware.impl.mockbank;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.impl.test.MiddlewareServiceApplication;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MiddlewareServiceApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@ActiveProfiles("h2")
@DatabaseTearDown(value = {"MiddlewareServiceImplIT-db-delete.xml"}, type = DatabaseOperation.DELETE_ALL)
public class MiddlewareServiceImplIT {

    @Autowired
    private AppManagementService appManagementService;
    
    @Autowired
    private LedgerAccountRepository repo;
    
    @Before
    public void initDepositAccount() throws IOException {
        appManagementService.initApp();
    }

    @Test
    public void test(){
    	// DO nothing. Just run app initialization.
    	long count = repo.count();
    	Assert.assertEquals(26, count);
    }
}
