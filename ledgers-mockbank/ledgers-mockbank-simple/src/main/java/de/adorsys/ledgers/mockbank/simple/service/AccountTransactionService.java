package de.adorsys.ledgers.mockbank.simple.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.client.rest.AccountRestClient;
import de.adorsys.ledgers.mockbank.simple.data.TransactionData;
import feign.FeignException;

@Service
public class AccountTransactionService {
	private static final Logger logger = LoggerFactory.getLogger(MockBankSimpleInitService.class);


	@Autowired	
	private AccountRestClient ledgersAccount;
	@Autowired	
	private UserContextService contextService;
	@Autowired
	private DepositAccountService depositAccountService;

	/**
	 * Validate if all transactions listed in the upload file are in the databse.
	 * 
	 * @param t : the data holder
	 * @param strict : if there is more transaction in that time frame, return true.
	 * 
	 * @return : true if transaction match specification given.
	 * @throws IOException : error
	 */
	public boolean validateTransactions(TransactionData t, boolean strict) throws IOException {
		AccountDetailsTO accountDetailsTO = depositAccountService.account(t.getIban())
				.orElseThrow(() -> depositAccountService.numberFormater(t.getIban()));
		List<TransactionTO> loadedTransactions = loadTransactions(accountDetailsTO, t.getDateFrom(), LocalDate.now());
		
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

	private List<TransactionTO> loadTransactions(AccountDetailsTO accountDetailsTO, LocalDate from, LocalDate to)
			throws IOException {
		AccountDetailsTO account = depositAccountService.account(accountDetailsTO.getIban())
				.orElseThrow(() -> depositAccountService.numberFormater(accountDetailsTO.getIban()));
		
		try {
			contextService.setContextFromIban(accountDetailsTO.getIban());
			return ledgersAccount.getTransactionByDates(account.getId(), from, to).getBody();
		} catch(FeignException f) {
			throw new IOException(String.format("Error loading transaction for account %s responseCode %s message %s.",
					accountDetailsTO.getIban(), 
					f.status(), f.getMessage()));
		} finally {
			contextService.unsetContext();
		}
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
