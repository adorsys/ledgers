package de.adorsys.ledgers.middleware.api.domain.um;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;

public class LoginKeyDataTOTest {

	@Test
	public void testFromOpId() {
		String userId = "q2tRswcRSr4nXqUaJCXpkQ";
		LocalDateTime time = LocalDateTime.now();
		LoginKeyDataTO loginKeyDataTO = new LoginKeyDataTO(userId, time);
		String opId = loginKeyDataTO.toOpId();
		LoginKeyDataTO lkd = LoginKeyDataTO.fromOpId(opId);
		String userId2 = lkd.getUserId();
		Assert.assertEquals(userId, userId2);
	}

}
