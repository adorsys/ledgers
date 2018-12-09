package de.adorsys.ledgers.mockbank.simple;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithPrefixGoneMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithSuffixExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;

@Configuration
@ComponentScan(basePackageClasses = MockbankSimpleBasePackage.class)
public class MockBankSimpleConfiguration {
    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    @Bean
    public MockbankInitData init()
            throws AccountNotFoundMiddlewareException, PaymentProcessingMiddlewareException, UserAlreadyExistsMiddlewareException, AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException, UserNotFoundMiddlewareException, InsufficientPermissionMiddlewareException {

        return loadTestData("mockbank-simple-init-data.yml");
    }


    private MockbankInitData loadTestData(String file) {
        InputStream inputStream = MockbankSimpleBasePackage.class.getResourceAsStream(file);
        try {
            return mapper.readValue(inputStream, MockbankInitData.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}