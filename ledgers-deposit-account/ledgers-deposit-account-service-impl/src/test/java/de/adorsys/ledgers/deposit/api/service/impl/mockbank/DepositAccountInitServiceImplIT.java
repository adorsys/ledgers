package de.adorsys.ledgers.deposit.api.service.impl.mockbank;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.impl.test.DepositAccountServiceApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DepositAccountServiceApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
public class DepositAccountInitServiceImplIT {

    @Autowired
    private DepositAccountInitService depositAccountInitService;

    @Test()
    public void should_run_init_config_data_without_exception() throws IOException {
    	depositAccountInitService.initConfigData();
    }
}
