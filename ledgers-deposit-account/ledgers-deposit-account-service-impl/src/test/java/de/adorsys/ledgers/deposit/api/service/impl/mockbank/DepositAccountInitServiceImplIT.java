package de.adorsys.ledgers.deposit.api.service.impl.mockbank;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import de.adorsys.ledgers.deposit.api.service.impl.test.DepositAccountServiceApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DepositAccountServiceApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
public class DepositAccountInitServiceImplIT {
	

    @Configuration
    static class Config {
        @Bean
        public ASPSPConfigSource configSource() {
            return new MockBankConfigSource();
        }
    }

    @Autowired
    private DepositAccountInitService depositAccountInitService;

    @Test()
    public void should_run_init_config_data_without_exception() throws IOException {
    	depositAccountInitService.initConfigData();
    }
}
