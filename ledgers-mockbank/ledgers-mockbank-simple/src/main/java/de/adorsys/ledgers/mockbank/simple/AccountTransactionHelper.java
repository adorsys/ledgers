package de.adorsys.ledgers.mockbank.simple;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.rest.resource.AccountResource;
import de.adorsys.ledgers.mockbank.simple.data.TransactionData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountTransactionHelper {
	private static ObjectMapper jsonMapper = new ObjectMapper();
	private static DateTimeFormatter accountResourceDateFormat = DateTimeFormatter.ofPattern(AccountResource.LOCAL_DATE_YYYY_MM_DD_FORMAT);
	private static final Logger logger = LoggerFactory.getLogger(MockBankSimpleInitService.class);
	private static final TypeReference<ArrayList<TransactionTO>> transctionsTOListRef = new TypeReference<ArrayList<TransactionTO>>() {};

	private Map<String, UserBag> usersMap = new HashMap<>();
	
	public AccountTransactionHelper(Map<String, UserBag> usersMap) {
		this.usersMap = usersMap;
	}

	/**
	 * Validate if all transactions listed in the upload file are in the databse.
	 * 
	 * @param baseUrl : server address
	 * @param t : the data holder
	 * @param strict : if there is more transaction in that time frame, return true.
	 * 
	 * @return
	 * @throws IOException
	 */
	boolean validateTransactions(String baseUrl, TransactionData t, boolean strict) throws IOException {
		UserBag bag = usersMap.values().stream()
				.filter(b -> DepositAccountHelper.isAccountOwner(b, t.getIban())).findFirst()
				.orElseThrow(() -> new IllegalStateException(String.format("Owner of this account with iban %s not found.", t.getIban())));
		
		AccountDetailsTO accountDetailsTO = DepositAccountHelper.loadAccountDetailsByIban(baseUrl, t.getIban(), bag);
		List<TransactionTO> loadedTransactions = loadTransactions(baseUrl, bag, accountDetailsTO, t.getDateFrom(), LocalDate.now());
		
		// Now compare the transactions
		List<TransactionTO> expectedTransactions = t.getTransactions();
		if(loadedTransactions.size() < expectedTransactions.size() || 
				strict && loadedTransactions.size() != expectedTransactions.size()) {
			logger.error(String.format("For account %s loaded transactions of size %s differs from configured transactions of size %s.", t.getIban(), loadedTransactions.size(), expectedTransactions.size()));
			return false;
		}
		
		boolean good =stripExpectedTransactions(loadedTransactions, expectedTransactions);
		if(!good) {
			return false;
		}

		if(strict && !loadedTransactions.isEmpty()) {
			for (TransactionTO lt : loadedTransactions) {
				logger.error(String.format("Loaded transaction not specified: date %s and amount %s and creditor %s", lt.getBookingDate(), lt.getAmount().getAmount(), lt.getCreditorName()));
			}
			logger.error("Logged trasanctions are not supposed to be present in the database.");
			return false;
		}
		return true;
	}

	/*
	 * Remove all expected transactions from the list of loaded. Return false if an expected transaction is not
	 * in the list of loaded transactions.
	 */
	private boolean stripExpectedTransactions(List<TransactionTO> loadedTransactions,
			List<TransactionTO> expectedTransactions) {
		for (int i = 0; i < expectedTransactions.size(); i++) {
			TransactionTO expectedTransaction = expectedTransactions.get(i);
			TransactionTO transactionTO = hasLoadedTransaction(expectedTransaction, loadedTransactions);
			if(transactionTO!=null) {
				loadedTransactions.remove(transactionTO);
			} else {
				logger.error(String.format("Missing transaction with: date %s and amount %s and creditor %s", expectedTransaction.getBookingDate(), expectedTransaction.getAmount().getAmount(), expectedTransaction.getCreditorName()));
				return false;
			}
		}
		return true;
	}

	private List<TransactionTO> loadTransactions(String baseUrl, UserBag bag, AccountDetailsTO accountDetailsTO, LocalDate from, LocalDate to)
			throws IOException {
		List<TransactionTO> loadedTransactions = null;
		HttpURLConnection con = null;
		try {
			URL url = UriComponentsBuilder.fromUriString(baseUrl).path(AccountResource.BASE_PATH)
					.path(AccountResource.ACCOUNT_ID__TRANSACTIONS_PATH)
					.queryParam(AccountResource.DATE_FROM_QUERY_PARAM, from.format(accountResourceDateFormat))
					.queryParam(AccountResource.DATE_TO_QUERY_PARAM, to.format(accountResourceDateFormat))
					.buildAndExpand(accountDetailsTO.getId()).toUri().toURL();
			con = HttpURLConnectionHelper.getContent(url, bag.getAccessToken());
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				loadedTransactions = jsonMapper.readValue(HttpURLConnectionHelper.readToString(con), transctionsTOListRef);
			} else {
				throw new IOException(String.format("Error loading transaction for account %s responseCode %s message %s.",
						accountDetailsTO.getIban(), con.getResponseCode(), con.getResponseMessage()));
			}

		} finally {
			if(con!=null) {
				con.disconnect();
			}
		}
		return loadedTransactions;
	}

	/*
	 * Returns the matching tx
	 */
	private TransactionTO hasLoadedTransaction(TransactionTO expectedTx, List<TransactionTO> loadedTransactions) {
		return loadedTransactions.stream().filter(loadedTx -> matchTx(expectedTx, loadedTx))
		.findFirst().orElse(null);
	}
	
	private boolean matchTx(TransactionTO expectedTransaction, TransactionTO loadedTransaction) {
		return expectedTransaction.getBookingDate().equals(loadedTransaction.getBookingDate()) 
			&&
		expectedTransaction.getAmount().getAmount().compareTo(loadedTransaction.getAmount().getAmount())==0 
			&&
		StringUtils.equals(expectedTransaction.getCreditorName(), loadedTransaction.getCreditorName());
	}
}
