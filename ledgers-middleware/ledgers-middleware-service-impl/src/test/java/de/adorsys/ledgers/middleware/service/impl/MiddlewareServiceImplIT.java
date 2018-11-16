package de.adorsys.ledgers.middleware.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.service.MiddlewareService;
import de.adorsys.ledgers.middleware.service.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.service.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.service.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.service.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.service.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.service.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.PaymentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.test.MiddlewareServiceApplication;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MiddlewareServiceApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@ActiveProfiles("h2")
@DatabaseTearDown(value={"MiddlewareServiceImplIT-db-delete.xml"}, type=DatabaseOperation.DELETE_ALL)
public class MiddlewareServiceImplIT {

	private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	@Autowired
	private MiddlewareService middlewareService;
	@Autowired
	private DepositAccountService depositAccountService;
	@Autowired
	private UserService userService;
	
	@Test
	public void execute_payment_read_tx_ok() throws DepositAccountNotFoundException, UserAlreadyExistsException,
			PaymentProcessingMiddlewareException, PaymentNotFoundMiddlewareException,
			AccountNotFoundMiddlewareException, TransactionNotFoundMiddlewareException {
		MiddlewareTestCaseData testData = loadTestData("MiddlewareServiceImplIT-read_tx_ok.yml");
		
		List<DepositAccountBO> accounts = testData.getAccounts();
		for (DepositAccountBO depositAccount : accounts) {
			depositAccountService.createDepositAccount(depositAccount);
		}
		
		List<UserBO> users = testData.getUsers();
		for (UserBO userBO : users) {
			userService.create(userBO);
		}
		
		List<SinglePaymentTestData> singlePaymentTests = testData.getSinglePaymentTests();
		for (SinglePaymentTestData singlePaymentTest : singlePaymentTests) {
			
			SinglePaymentTO singlePayment = singlePaymentTest.getSinglePayment();
			
			// Check balance before
			if(singlePaymentTest.getBalanceDebitAccountBefore()!=null) {
				checkBalance(singlePayment.getDebtorAccount(), singlePaymentTest.getBalanceDebitAccountBefore());
			}
			if(singlePaymentTest.getBalanceCreditAccountBefore()!=null) {
				checkBalance(singlePayment.getCreditorAccount(), singlePaymentTest.getBalanceCreditAccountBefore());
			}
			
			// Initiate
			SinglePaymentTO pymt = (SinglePaymentTO)middlewareService.initiatePayment(singlePayment, PaymentTypeTO.SINGLE);
			TransactionStatusTO initiatedPaymentStatus = middlewareService.getPaymentStatusById(pymt.getPaymentId());
			Assert.assertEquals(TransactionStatusTO.RCVD, initiatedPaymentStatus);

			// Check balance before.
			if(singlePaymentTest.getBalanceDebitAccountBefore()!=null) {
				checkBalance(singlePayment.getDebtorAccount(), singlePaymentTest.getBalanceDebitAccountBefore());
			}
			if(singlePaymentTest.getBalanceCreditAccountBefore()!=null) {
				checkBalance(singlePayment.getCreditorAccount(), singlePaymentTest.getBalanceCreditAccountBefore());
			}
			
			// Execute
			TransactionStatusTO executedPaymentStatus = middlewareService.executePayment(pymt.getPaymentId());
			Assert.assertEquals(TransactionStatusTO.ACSP, executedPaymentStatus);
			
			// Check balance after.
			// Check balance before
			if(singlePaymentTest.getBalanceDebitAccountAfter()!=null) {
				checkBalance(singlePayment.getDebtorAccount(), singlePaymentTest.getBalanceDebitAccountAfter());
			}
			if(singlePaymentTest.getBalanceCreditAccountAfter()!=null) {
				checkBalance(singlePayment.getCreditorAccount(), singlePaymentTest.getBalanceCreditAccountAfter());
			}
		}
		
		List<TransactionTestData> transactions = testData.getTransactions();
		for (TransactionTestData txTest : transactions) {
			checkTransactions(txTest);
		}
	}	

	private void checkTransactions(TransactionTestData txTest) throws AccountNotFoundMiddlewareException, TransactionNotFoundMiddlewareException, DepositAccountNotFoundException {
		String iban = txTest.getIban();
		DepositAccountBO depositAccount = depositAccountService.getDepositAccountByIban(iban);
		List<TransactionTO> loadedTransactions = middlewareService.getTransactionsByDates(depositAccount.getId(), txTest.getDateFrom(), txTest.getDateTo());
		// Now compare the transactions
		List<TransactionTO> expectedTransactions = txTest.getTransactions();
		Assert.assertEquals(expectedTransactions.size(), loadedTransactions.size());
		for (int i = 0; i < expectedTransactions.size(); i++) {
			TransactionTO expectedTransaction = expectedTransactions.get(i);
			TransactionTO loadedTransaction = loadedTransactions.get(i);
			Assert.assertEquals(expectedTransaction.getBookingDate(), loadedTransaction.getBookingDate());
			Assert.assertEquals(expectedTransaction.getAmount().getAmount().doubleValue(), loadedTransaction.getAmount().getAmount().doubleValue(), 0d);
			Assert.assertEquals(expectedTransaction.getCreditorName(), loadedTransaction.getCreditorName());
		}
	}

	private void checkBalance(AccountReferenceTO account, BigDecimal creditAmount) throws AccountNotFoundMiddlewareException, DepositAccountNotFoundException {
		DepositAccountBO depositAccount = depositAccountService.getDepositAccountByIban(account.getIban());
		List<AccountBalanceTO> balances = middlewareService.getBalances(depositAccount.getId());
		Assert.assertNotNull(balances);
		Assert.assertEquals(1, balances.size());
		AccountBalanceTO accountBalanceTO = balances.get(0);
		AmountTO amount = accountBalanceTO.getAmount();
		Assert.assertNotNull(amount.getAmount());
		Assert.assertEquals(creditAmount.doubleValue(), amount.getAmount().doubleValue(), 0d);
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
