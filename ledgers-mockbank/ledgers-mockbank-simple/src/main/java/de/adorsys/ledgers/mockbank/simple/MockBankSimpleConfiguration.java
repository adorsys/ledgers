package de.adorsys.ledgers.mockbank.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.payment.BulkPaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.mockbank.simple.data.BulkPaymentsData;
import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;
import de.adorsys.ledgers.mockbank.simple.data.SinglePaymentsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@ComponentScan(basePackageClasses = MockbankSimpleBasePackage.class)
public class MockBankSimpleConfiguration {
    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private final MiddlewareService middlewareService;
    private final MiddlewareAccountManagementService accountService;
    private final MiddlewareUserManagementService userService;

    @Autowired
    public MockBankSimpleConfiguration(MiddlewareService middlewareService, MiddlewareAccountManagementService accountService, MiddlewareUserManagementService userService) {
        this.middlewareService = middlewareService;
        this.accountService = accountService;
        this.userService = userService;
    }

    @Bean
    public MockbankInitData init()
            throws AccountNotFoundMiddlewareException, PaymentProcessingMiddlewareException, UserAlreadyExistsMiddlewareException {

        MockbankInitData testData = loadTestData("mockbank-simple-init-data.yml");

        // CHeck if update is required.
        if (!updateRequired(testData)) {
            return testData;
        }

        // Create accounts
        createAccounts(testData);

        // Create users
        createUsers(testData);

        // Execute single payments
        processSinglePayments(testData.getSinglePayments());

        // Execute bulk payments
        processBulkPayments(testData.getBulkPayments());

        return testData;
    }

    private void createUsers(MockbankInitData testData) throws UserAlreadyExistsMiddlewareException {
        List<UserTO> users = testData.getUsers();
        for (UserTO userTO : users) {
            userService.create(userTO);
        }
    }

    private void createAccounts(MockbankInitData testData) throws AccountNotFoundMiddlewareException {
        List<AccountDetailsTO> accounts = testData.getAccounts();
        for (AccountDetailsTO depositAccount : accounts) {
            accountService.createDepositAccount(depositAccount);
        }
    }

    private void processSinglePayments(List<SinglePaymentsData> singlePaymentTests)
            throws AccountNotFoundMiddlewareException, PaymentProcessingMiddlewareException {
        for (SinglePaymentsData singlePaymentTest : singlePaymentTests) {

            // Initiate
            SinglePaymentTO pymt = (SinglePaymentTO) middlewareService.initiatePayment(singlePaymentTest.getSinglePayment(),
                    PaymentTypeTO.SINGLE);

            // Execute
            middlewareService.executePayment(pymt.getPaymentId());
        }
    }

    private void processBulkPayments(List<BulkPaymentsData> bulkPaymentTests) throws AccountNotFoundMiddlewareException, PaymentProcessingMiddlewareException {
        if (bulkPaymentTests == null) {
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
            accountService.getDepositAccountByIban(testData.getUpdateMarkerAccountNbr(), LocalDateTime.now(), false);
            return false;
        } catch (AccountNotFoundMiddlewareException e) {
            return true;
        }
    }

}