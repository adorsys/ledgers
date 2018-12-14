package de.adorsys.ledgers.mockbank.simple;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.rest.resource.AppManagementResource;
import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;
import de.adorsys.ledgers.mockbank.simple.data.TransactionData;

@Service
public class MockBankSimpleInitService {
	private static final String APPLICATION_JSON = "application/json;";
	private boolean initialized = false;

	private final Map<String, UserBag> usersMap = new HashMap<>();
	private final MockbankInitData sampleData;
	
	private AccountTransactionHelper accountTransactionHelper;
	private PaymentProcessingHelper paymentProcessingHelper;


	@Autowired
	public MockBankSimpleInitService(MockbankInitData sampleData) {
		this.sampleData = sampleData;
		this.accountTransactionHelper = new AccountTransactionHelper(usersMap);
		this.paymentProcessingHelper = new PaymentProcessingHelper(usersMap, this.sampleData);
	}

	public void runInit(String baseUrl) {

		try {
			// If !hasAdmin
			BearerTokenTO accessToken = UserAccountHelper.authorizeAdmin(baseUrl);
			// if !updateRequired
			initLedgers(baseUrl, accessToken);
	
			// CHeck if update is required.
			updateIfRequired(baseUrl);
			
			if(verifyUpload(baseUrl, false)) {
				// no need to repeat execution of payments.
				initialized = true;
				return;
			}
	
			// Execute single payments
			paymentProcessingHelper.processSinglePayments(baseUrl);
	
			// Execute bulk payments
			paymentProcessingHelper.processBulkPayments(baseUrl);
			
			if(!verifyUpload(baseUrl, true)) {
				throw new IllegalStateException("See logs could not verify initial transactions");
			}
			initialized = true;
		} catch(Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private boolean verifyUpload(String baseUrl, boolean strict) {
		boolean valid = true;
		List<TransactionData> transactions = sampleData.getTransactions();
		for (TransactionData t : transactions) {
			try {
				valid = valid && accountTransactionHelper.validateTransactions(baseUrl, t, strict);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return valid;
	}

	
	private void initLedgers(String baseUrl, BearerTokenTO accessToken) throws IOException {
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(AppManagementResource.BASE_PATH)
				.path(AppManagementResource.INIT_PATH).build().toUri().toURL();

		HttpURLConnection con = HttpURLConnectionHelper.postContent(url, accessToken, new byte[] {}, APPLICATION_JSON);
		if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException(String.format("Error initializing ledger user responseCode %s message %s.",
					con.getResponseCode(), con.getResponseMessage()));
		}
	}

	/*
	 * Check if update required. If then process the config file.
	 */
	private void updateIfRequired(String baseUrl)
			throws InsufficientPermissionMiddlewareException, UnsupportedEncodingException, ProtocolException, IOException {
		// No users, not
		if (sampleData.getUsers() == null || sampleData.getUsers().isEmpty()) {
			return;
		}

		List<AccountDetailsTO> accounts = sampleData.getAccounts();
		List<UserTO> users = sampleData.getUsers();
		for (UserTO userTO : users) {
			// Create user
			BearerTokenTO accessToken = UserAccountHelper.authOrCreateCustomer(baseUrl, userTO);
			UserBag userBag = new UserBag(userTO, accessToken, UserRoleTO.CUSTOMER);
			List<AccountDetailsTO> accessibleAccounts = DepositAccountHelper.readAccessibleAccounts(baseUrl, userBag);
			userBag.getAccessibleAccounts().addAll(accessibleAccounts);
			usersMap.put(userTO.getLogin(), userBag);
			
			List<String> accessibleAccountsFromDBIbans = accessibleAccounts.stream().map(a -> a.getIban()).collect(Collectors.toList());
			List<String> accessibleAccountsFromFileIbans = userTO.getAccountAccesses().stream().map(a -> a.getIban()).collect(Collectors.toList());
			for (AccountDetailsTO accountDetailsTO : accounts) {
				if(!accessibleAccountsFromFileIbans.contains(accountDetailsTO.getIban())) {
					// no assignement of this account to this user in the current files.
					continue;
				}
				
				if(accessibleAccountsFromDBIbans.contains(accountDetailsTO.getIban())) {
					// account already associated with user.
					continue;
				}
				
				DepositAccountHelper.createDepositAccount(baseUrl, userTO, userBag, accountDetailsTO);
			}
		}
	}


	public boolean checkInitialized() {
		return initialized;
	}
}