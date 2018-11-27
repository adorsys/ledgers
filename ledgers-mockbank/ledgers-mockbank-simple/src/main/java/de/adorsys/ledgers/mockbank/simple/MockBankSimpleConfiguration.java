package de.adorsys.ledgers.mockbank.simple;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.payment.BulkPaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.mockbank.simple.data.BulkPaymentsData;
import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;
import de.adorsys.ledgers.mockbank.simple.data.SinglePaymentsData;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;

@Configuration
@ComponentScan(basePackageClasses=MockbankSimpleBasePackage.class)
public class MockBankSimpleConfiguration {
	

	private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	@Autowired
	private MiddlewareService middlewareService;
	@Autowired
	private MiddlewareAccountManagementService accountService;
	@Autowired
	private MiddlewareUserManagementService userService;

	@Bean
	public MockbankInitData init()
			throws AccountNotFoundMiddlewareException, TransactionNotFoundMiddlewareException,
			DepositAccountNotFoundException, PaymentProcessingMiddlewareException, PaymentNotFoundMiddlewareException,
			UserAlreadyExistsMiddlewareException, LedgerNotFoundException, BaseLineException,
			LedgerAccountNotFoundException {

		MockbankInitData testData = loadTestData("mockbank-simple-init-data.yml");

		// CHeck if update is required.
		if(!updateRequired(testData)) {
			return testData;
		}
		
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
		processSinglePayments(testData.getSinglePayments());

		// Execute bulk payments
		processBulkPayments(testData.getBulkPayments());

		return testData;
	}

	private void processSinglePayments(List<SinglePaymentsData> singlePaymentTests)
			throws AccountNotFoundMiddlewareException, PaymentNotFoundMiddlewareException,
			PaymentProcessingMiddlewareException, LedgerNotFoundException, LedgerAccountNotFoundException,
			BaseLineException {
		for (SinglePaymentsData singlePaymentTest : singlePaymentTests) {

			// Initiate
			SinglePaymentTO pymt = (SinglePaymentTO) middlewareService.initiatePayment(singlePaymentTest.getSinglePayment(),
					PaymentTypeTO.SINGLE);

			// Execute
			middlewareService.executePayment(pymt.getPaymentId());
		}
	}

	private void processBulkPayments(List<BulkPaymentsData> bulkPaymentTests) throws AccountNotFoundMiddlewareException,
			PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException, LedgerNotFoundException,
			LedgerAccountNotFoundException, BaseLineException {
		if(bulkPaymentTests==null) {
			return;
		}
		for (BulkPaymentsData bulkPaymentTest : bulkPaymentTests) {

			BulkPaymentTO bulkPayment = bulkPaymentTest.getBulkPayment();

			// Initiate
			BulkPaymentTO pymt = (BulkPaymentTO) middlewareService.initiatePayment(bulkPayment, PaymentTypeTO.BULK);

			// Execute
			middlewareService.executePayment(pymt.getPaymentId());
		}
	}

	private MockbankInitData loadTestData(String file) {
		InputStream inputStream = MockbankSimpleBasePackage.class.getResourceAsStream(file);
		try {
			return mapper.readValue(inputStream, MockbankInitData.class);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/*
	 * Check if update required. If then process the config file.
	 */
	private boolean updateRequired(MockbankInitData testData) {
		try {
			accountService.getDepositAccountByIBAN(testData.getUpdateMarkerAccountNbr(), LocalDateTime.now(), false);
			return false;
		} catch (AccountNotFoundMiddlewareException e) {
			return true;
		}
	}
	
}