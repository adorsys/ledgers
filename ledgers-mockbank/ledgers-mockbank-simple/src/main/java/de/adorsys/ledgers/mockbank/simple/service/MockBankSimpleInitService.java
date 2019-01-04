package de.adorsys.ledgers.mockbank.simple.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.client.rest.AppMgmtRestClient;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;
import de.adorsys.ledgers.mockbank.simple.data.TransactionData;
import feign.FeignException;

@Service
public class MockBankSimpleInitService {
	private boolean initialized = false;

	@Autowired	
	private AppMgmtRestClient ledgersAppMgmt;
	@Autowired	
	private UserContextService contextService;

	@Autowired	
	private MockbankInitData sampleData;
	@Autowired	
	private UserAccountService userAccountService;
	@Autowired	
	private PaymentProcessingService paymentProcessingService;
	@Autowired
	private AccountTransactionService accountTransactionService;
	@Autowired
	private DepositAccountService depositAccountService;

	public void runInit() {
		try {
			// If !hasAdmin
			BearerTokenTO bearerToken = null;
			try {
				bearerToken = userAccountService.createAdminAccount();
			} catch (ConflictRestException c) {
				bearerToken = userAccountService.authorizeAdmin();
			}
			// if !updateRequired
			initLedgers(bearerToken);
	
			// CHeck if update is required.
			updateIfRequired();
			
			if(verifyUpload(false)) {
				// no need to repeat execution of payments.
				initialized = true;
				return;
			}
	
			// Execute single payments
			paymentProcessingService.processSinglePayments();
	
			// Execute bulk payments
			paymentProcessingService.processBulkPayments();

			initialized = true;
		} catch(Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private boolean verifyUpload(boolean strict) {
		boolean valid = true;
		List<TransactionData> transactions = sampleData.getTransactions();
		for (TransactionData t : transactions) {
			try {
				valid = valid && accountTransactionService.validateTransactions(t, strict);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return valid;
	}

	private void initLedgers(BearerTokenTO accessToken) throws IOException {
		try {
			UserContext adminBag = contextService.newToken(accessToken);
			contextService.setContext(adminBag);
			HttpStatus status = null;
			try {
				ResponseEntity<Void> initApp = ledgersAppMgmt.initApp();
				status = initApp.getStatusCode();
			} catch(FeignException f) {
				status = HttpStatus.valueOf(f.status());
			}
			if(!HttpStatus.OK.equals(status)) {
				throw new IOException(String.format("Error initializing ledger user responseCode %s message %s.",
						status.value(), status.toString()));
			}
		} finally {
			contextService.unsetContext();
		}
	}

	/*
	 * Check if update required. If then process the config file.
	 */
	private void updateIfRequired()
			throws IOException {
		// No users, not
		if (sampleData.getUsers() == null || sampleData.getUsers().isEmpty()) {
			return;
		}

		List<AccountDetailsTO> accounts = sampleData.getAccounts();
		List<UserTO> users = sampleData.getUsers();
		for (UserTO userTO : users) {
			// Create user
			BearerTokenTO accessToken = userAccountService.authOrCreateCustomer(userTO);
			UserContext bag = new UserContext(userTO, accessToken);
			List<AccountDetailsTO> accessibleAccounts = depositAccountService.accessibleAccounts(bag);
			bag.getAccessibleAccounts().addAll(accessibleAccounts);
			contextService.updateCredentials(userTO.getLogin(), bag);
			
			List<String> accessibleAccountsFromDBIbans = accessibleAccounts.stream()
					                                             .map(AccountDetailsTO::getIban)
					                                             .collect(Collectors.toList());
			List<String> accessibleAccountsFromFileIbans = userTO.getAccountAccesses().stream()
					                                               .map(AccountAccessTO::getIban)
					                                               .collect(Collectors.toList());
			for (AccountDetailsTO accountDetailsTO : accounts) {
				if(!accessibleAccountsFromFileIbans.contains(accountDetailsTO.getIban())) {
					// no assignment of this account to this user in the current files.
					continue;
				}
				
				if(accessibleAccountsFromDBIbans.contains(accountDetailsTO.getIban())) {
					// account already associated with user.
					continue;
				}
				
				depositAccountService.createDepositAccount(userTO, accountDetailsTO);
			}
		}
	}

	public boolean checkInitialized() {
		return initialized;
	}
}