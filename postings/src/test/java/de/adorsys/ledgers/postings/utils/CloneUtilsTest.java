package de.adorsys.ledgers.postings.utils;

import java.time.LocalDateTime;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;

public class CloneUtilsTest {

	@Test
	public void test() {
		ChartOfAccount coa = ChartOfAccount.builder()
			.created(LocalDateTime.now())
			.id(Ids.id())
			.name("Sample coa")
			.user("Sample user")
			.shortDesc("Sample coa short desc")
			.longDesc("Sample coa long desc")
			.build();
		
		ChartOfAccount clone = CloneUtils.cloneObject(coa, ChartOfAccount.class);
		
		Assert.assertTrue(Objects.deepEquals(coa, clone));
	}

}
