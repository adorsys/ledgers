package de.adorsys.ledgers.mockbank.simple.data.test.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.ledgers.mockbank.simple.data.test.EnableMockBankSimpleDataTest;
import de.adorsys.ledgers.mockbank.simple.data.test.api.DBFlushService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@EnableMockBankSimpleDataTest.MockBankSimpleDataTestResource
@Api(tags="DB Flush", description="Clear all database entries.")
public class DBFlushResource {
	
	public static final String FLUSH_PATH = "/data-test/db-flush";
	
	private final DBFlushService flushService;
	
	public DBFlushResource(DBFlushService flushService) {
		this.flushService = flushService;
	}
	
	@ApiOperation(value="Allow a user to clear database entries.")
	@DeleteMapping(DBFlushResource.FLUSH_PATH)
	public void flushDatabase() {
		flushService.flushDataBase();
	}
}
