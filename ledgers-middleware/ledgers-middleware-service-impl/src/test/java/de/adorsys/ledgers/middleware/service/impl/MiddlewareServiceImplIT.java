package de.adorsys.ledgers.middleware.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.middleware.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.service.MiddlewareService;
import de.adorsys.ledgers.middleware.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.service.domain.payment.BulkPaymentTO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.service.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.service.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.service.domain.um.UserTO;
import de.adorsys.ledgers.middleware.service.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.PaymentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.test.MiddlewareServiceApplication;
import de.adorsys.ledgers.postings.api.domain.AccountStmtBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.AccountStmtService;
import de.adorsys.ledgers.postings.api.service.LedgerService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MiddlewareServiceApplication.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@ActiveProfiles("h2")
@DatabaseTearDown(value = { "MiddlewareServiceImplIT-db-delete.xml" }, type = DatabaseOperation.DELETE_ALL)
public class MiddlewareServiceImplIT {

	private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	@Autowired
	private MiddlewareService middlewareService;
	@Autowired
	private MiddlewareAccountManagementService accountService;
	@Autowired
	private MiddlewareUserManagementService userService;
	@Autowired
	private AccountStmtService accountStmtService;
	@Autowired
	private LedgerService ledgerService;
	@Autowired
	private DepositAccountConfigService depositAccountConfigService;

	@Test
	public void execute_payment_read_tx_ok()
			throws AccountNotFoundMiddlewareException, TransactionNotFoundMiddlewareException,
			DepositAccountNotFoundException, PaymentProcessingMiddlewareException, PaymentNotFoundMiddlewareException,
			UserAlreadyExistsMiddlewareException, LedgerNotFoundException, BaseLineException,
			LedgerAccountNotFoundException {
		
		LedgerBO ledgerBO = loadLedger();
		
		MiddlewareTestCaseData testData = loadTestData("MiddlewareServiceImplIT-read_tx_ok.yml");

		// Create accounts
		List<AccountDetailsTO> accounts = testData.getAccounts();
		for (AccountDetailsTO depositAccount : accounts) {
			accountService.createDepositAccount(depositAccount);
		}

		// Create users
		List<UserTO> users = testData.getUsers();
		for (UserTO userTO : users) {
			userService.create(userTO);
		}

		// Execute single payments
		processSinglePayments(testData.getSinglePayments(), ledgerBO);

		// Execute bulk payments
		processBulkPayments(testData.getBulkPayments(), ledgerBO);
		
		// Check global balances
		checkBalances(testData.getBalancesList(),ledgerBO);

		// Read transaction
		readTransactions(testData.getTransactions());
	}

	private LedgerBO loadLedger() {
		String ledger = depositAccountConfigService.getLedger();
		LedgerBO ledgerBO = new LedgerBO();
		ledgerBO.setName(ledger);
		return ledgerBO;
	}

	private void processSinglePayments(List<SinglePaymentsData> singlePaymentTests, LedgerBO ledgerBO)
			throws AccountNotFoundMiddlewareException, PaymentNotFoundMiddlewareException,
			PaymentProcessingMiddlewareException, LedgerNotFoundException, LedgerAccountNotFoundException,
			BaseLineException {
		for (SinglePaymentsData singlePaymentTest : singlePaymentTests) {

			// Initiate
			SinglePaymentTO pymt = (SinglePaymentTO) middlewareService.initiatePayment(singlePaymentTest.getSinglePayment(),
					PaymentTypeTO.SINGLE);
			TransactionStatusTO initiatedPaymentStatus = middlewareService.getPaymentStatusById(pymt.getPaymentId());
			Assert.assertEquals(TransactionStatusTO.RCVD, initiatedPaymentStatus);

			// Execute
			TransactionStatusTO executedPaymentStatus = middlewareService.executePayment(pymt.getPaymentId());
			Assert.assertEquals(TransactionStatusTO.ACSP, executedPaymentStatus);

			// Check balances
			checkBalances(singlePaymentTest.getBalancesList(), ledgerBO);
		}
	}

	private void processBulkPayments(List<BulkPaymentsData> bulkPaymentTests, LedgerBO ledgerBO) throws AccountNotFoundMiddlewareException,
			PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException, LedgerNotFoundException,
			LedgerAccountNotFoundException, BaseLineException {
		if(bulkPaymentTests==null) {
			return;
		}
		for (BulkPaymentsData bulkPaymentTest : bulkPaymentTests) {

			BulkPaymentTO bulkPayment = bulkPaymentTest.getBulkPayment();

			// Initiate
			BulkPaymentTO pymt = (BulkPaymentTO) middlewareService.initiatePayment(bulkPayment, PaymentTypeTO.BULK);
			TransactionStatusTO initiatedPaymentStatus = middlewareService.getPaymentStatusById(pymt.getPaymentId());
			Assert.assertEquals(TransactionStatusTO.RCVD, initiatedPaymentStatus);

			// Execute
			TransactionStatusTO executedPaymentStatus = middlewareService.executePayment(pymt.getPaymentId());
			Assert.assertEquals(TransactionStatusTO.ACSP, executedPaymentStatus);

			// Check balances
			checkBalances(bulkPaymentTest.getBalancesList(), ledgerBO);
		}
	}

	private void readTransactions(List<TransactionTestData> transactions) throws AccountNotFoundMiddlewareException,
			TransactionNotFoundMiddlewareException, DepositAccountNotFoundException {
		for (TransactionTestData txTest : transactions) {
			checkTransactions(txTest);
		}
	}

	private void checkBalances(List<AccountBalances> list, LedgerBO ledgerBO) throws LedgerNotFoundException, LedgerAccountNotFoundException, BaseLineException {
		if(list==null) {
			return;
		}
		
		list.forEach(balances -> checkBalances(balances, ledgerBO));
	}

	private void checkBalances(AccountBalances balances, LedgerBO ledgerBO) {
		if(balances==null || balances.getBalances()==null) {
			return;
		}
		LocalDateTime refTime = balances.getRefTime();
		balances.getBalances().forEach(balance -> checkBalance(balance, refTime, ledgerBO));
	}

	private void checkBalance(AccountBalance balance, LocalDateTime refTime, LedgerBO ledgerBO) {
		
		try {
			LedgerAccountBO ledgerAccount = ledgerService.findLedgerAccount(ledgerBO, balance.getAccNbr());
			AccountStmtBO stmt = accountStmtService.readStmt(ledgerAccount, refTime);
			Assert.assertEquals(String.format("Wrong value for account %s at time %s. expected %s but was %s.", balance.getAccNbr(), refTime, balance.getBalance(), stmt.creditBalance()), balance.getBalance().doubleValue(), stmt.creditBalance().doubleValue(), 0d);
		} catch (LedgerAccountNotFoundException | LedgerNotFoundException | BaseLineException e) {
			throw new IllegalStateException(e);
		}
	}

	private void checkTransactions(TransactionTestData txTest) throws AccountNotFoundMiddlewareException,
			TransactionNotFoundMiddlewareException, DepositAccountNotFoundException {
		String iban = txTest.getIban();
		AccountDetailsTO depositAccount = accountService.getDepositAccountByIBAN(iban, LocalDateTime.now(), true);
		List<TransactionTO> loadedTransactions = middlewareService.getTransactionsByDates(depositAccount.getId(),
				txTest.getDateFrom(), txTest.getDateTo());
		// Now compare the transactions
		List<TransactionTO> expectedTransactions = txTest.getTransactions();
		Assert.assertEquals(expectedTransactions.size(), loadedTransactions.size());
		for (int i = 0; i < expectedTransactions.size(); i++) {
			TransactionTO expectedTransaction = expectedTransactions.get(i);
			TransactionTO loadedTransaction = loadedTransactions.get(i);
			Assert.assertEquals(expectedTransaction.getBookingDate(), loadedTransaction.getBookingDate());
			Assert.assertEquals(expectedTransaction.getAmount().getAmount().doubleValue(),
					loadedTransaction.getAmount().getAmount().doubleValue(), 0d);
			Assert.assertEquals(expectedTransaction.getCreditorName(), loadedTransaction.getCreditorName());
		}
	}

	private MiddlewareTestCaseData loadTestData(String file) {
		InputStream inputStream = MiddlewareServiceImplIT.class.getResourceAsStream(file);
		try {
			return mapper.readValue(inputStream, MiddlewareTestCaseData.class);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
