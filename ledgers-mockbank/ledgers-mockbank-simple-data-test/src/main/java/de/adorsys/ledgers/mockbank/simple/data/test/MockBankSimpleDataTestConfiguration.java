package de.adorsys.ledgers.mockbank.simple.data.test;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithPrefixGoneMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithSuffixExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;

/**
 * Register a MockbankInitData for the data-test module.
 * 
 * This need the profile 'data-test' to be active.
 * i.e:
 * 
 * <code>mvn clean spring-boot:run -Dspring.profiles.active=h2,data-test</code>
 * 
 * @author bwa
 *
 */
@Configuration
@Profile("data-test")
@ComponentScan(basePackageClasses = MockBankSimpleDataTestBasePackage.class)
public class MockBankSimpleDataTestConfiguration {
	private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	
	
	@Bean
	public ASPSPConfigSource mockBankConfigSource() {
		return new MockBankConfigSource();
	}
    /**
     * @return MockbankInitData bean. With the experimental test data.
     * @throws AccountNotFoundMiddlewareException
     * @throws PaymentProcessingMiddlewareException
     * @throws UserAlreadyExistsMiddlewareException
     * @throws AccountWithPrefixGoneMiddlewareException
     * @throws AccountWithSuffixExistsMiddlewareException
     * @throws UserNotFoundMiddlewareException
     * @throws InsufficientPermissionMiddlewareException
     */
    @Bean
    public MockbankInitData init()
            throws AccountNotFoundMiddlewareException, PaymentProcessingMiddlewareException, UserAlreadyExistsMiddlewareException, AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException, UserNotFoundMiddlewareException, InsufficientPermissionMiddlewareException {

        return loadTestData("mockbank-simple-data-test-init-data.yml");
    }


    private MockbankInitData loadTestData(String file) {
        InputStream inputStream = MockBankSimpleDataTestBasePackage.class.getResourceAsStream(file);
        try {
            return mapper.readValue(inputStream, MockbankInitData.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
