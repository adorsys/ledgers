package de.adorsys.ledgers.postings.service.impl;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.adorsys.ledgers.postings.domain.AccountCategory;
import de.adorsys.ledgers.postings.domain.BalanceSide;
import de.adorsys.ledgers.postings.domain.LedgerAccount;

public class LoadLedgerAccountYMLTest {
	private ObjectMapper mapper = new ObjectMapper();
	
	@Before
	public void before(){
        final YAMLFactory ymlFactory = new YAMLFactory();
        mapper.registerModule(new JavaTimeModule());
        mapper = new ObjectMapper(ymlFactory);
	}
	
	@Test
	public void testReadYml() throws JsonParseException, JsonMappingException, IOException{
		InputStream inputStream = LoadLedgerAccountYMLTest.class.getResourceAsStream("LoadLedgerAccountYMLTest.yml");
		LedgerAccount[] ledgerAccounts = mapper.readValue(inputStream, LedgerAccount[].class);
		Assert.assertNotNull(ledgerAccounts);
		Assert.assertTrue(ledgerAccounts.length==2);
		Assert.assertEquals("1",ledgerAccounts[0].getName());
		Assert.assertEquals("Assets",ledgerAccounts[0].getShortDesc());
		Assert.assertEquals(AccountCategory.AS, ledgerAccounts[0].getCategory());
		Assert.assertEquals(BalanceSide.Dr, ledgerAccounts[0].getBalanceSide());
		Assert.assertEquals("1.1",ledgerAccounts[1].getName());
		Assert.assertEquals("Property, Plant And Equipment",ledgerAccounts[1].getShortDesc());
	} 
}
