package de.adorsys.ledgers.mockbank.simple.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.adorsys.ledgers.mockbank.simple.MockBankSimpleInitService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LedgersApplication.class, webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MockBankSimpleInitServiceIT {
	@Autowired
	private ApplicationContext context;

	@Test
	public void test() {
		Assert.assertTrue(context.getBean(MockBankSimpleInitService.class).checkInitialized());
	}

}
