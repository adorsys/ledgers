package de.adorsys.ledgers.mockbank.simple.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.adorsys.ledgers.mockbank.simple.server.LedgersApplication;
import de.adorsys.ledgers.mockbank.simple.service.MockBankSimpleInitService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LedgersApplication.class, webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MockBankSimpleInitServiceIT {
	@Autowired
	private MockBankSimpleInitService mockBank;

	@Test
	public void test(){
		mockBank.runInit();
		Assert.assertTrue(mockBank.checkInitialized());
	}
	/*@Test
	public void test() { //TODO fix this @fpo
		Assert.assertTrue(context.getBean(MockBankSimpleInitService.class).checkInitialized());
	}*/

}
