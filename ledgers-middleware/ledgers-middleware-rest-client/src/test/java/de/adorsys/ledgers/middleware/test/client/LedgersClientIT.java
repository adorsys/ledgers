package de.adorsys.ledgers.middleware.test.client;

import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountStatusTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountTypeTO;
import de.adorsys.ledgers.middleware.api.domain.account.UsageTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.client.rest.AccountRestClient;
import de.adorsys.ledgers.middleware.client.rest.AppMgmtRestClient;
import de.adorsys.ledgers.middleware.client.rest.AuthRequestInterceptor;
import de.adorsys.ledgers.middleware.client.rest.UserMgmtRestClient;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Currency;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LedgersClientApplication.class, webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("h2")
public class LedgersClientIT {
	@Autowired
	private AccountRestClient accountRestClient;
	@Autowired
	private UserMgmtRestClient userMgmtRestClient;
	@Autowired
	private AppMgmtRestClient appMgmtRestClient;
	@Autowired
	private AuthRequestInterceptor authHeader;
	@Autowired
	private DepositAccountInitService depositAccountInitService;
	
	@LocalServerPort
	private int port;
	
	@Test
	public void test() throws ConflictRestException {
		initApp();
		ResponseEntity<UserTO> user = userMgmtRestClient.register("francis.pouatcha", "fpo@mail.ledgers", "12345", UserRoleTO.CUSTOMER);
		AccountDetailsTO a = new AccountDetailsTO();
		a.setIban("DE69760700240340283600");
		a.setAccountStatus(AccountStatusTO.ENABLED);
		a.setCurrency(Currency.getInstance("EUR"));
		a.setProduct("Cash24");
		a.setAccountType(AccountTypeTO.CASH);
		a.setUsageType(UsageTypeTO.PRIV);
		a.setName("Francis Pouatcha");

		ResponseEntity<SCALoginResponseTO> response = userMgmtRestClient.authorise("francis.pouatcha", "12345", UserRoleTO.CUSTOMER);
		SCALoginResponseTO scaLoginResponseTO = response.getBody();
		BearerTokenTO token = scaLoginResponseTO.getBearerToken();
		
		authHeader.setAccessToken(token.getAccess_token());
		ResponseEntity<Void> createDepositAccountResponse = accountRestClient.createDepositAccount(a);
		Assert.assertTrue(HttpStatus.OK.equals(createDepositAccountResponse.getStatusCode()));
	}

	private void initApp() throws ConflictRestException {
		depositAccountInitService.initConfigData();
		UserTO adminUser = new UserTO("admin", "admin@ledgers.ldg", "12345");
		ResponseEntity<BearerTokenTO> responseEntity = appMgmtRestClient.admin(adminUser);
		BearerTokenTO bearerTokenTO = responseEntity.getBody();
		try {
			authHeader.setAccessToken(bearerTokenTO.getAccess_token());
			appMgmtRestClient.initApp();
		} finally {
			authHeader.setAccessToken(null);
		}
	}
}
