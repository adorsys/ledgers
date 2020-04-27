package de.adorsys.ledgers.middleware.impl.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PaymentConverterTest {
    private static final String PATH_SINGLE_BO = "PaymentSingle.yml";
    private static final String PATH_SINGLE_TO = "PaymentSingleTO.yml";
    private static final String PATH_PERIODIC_BO = "PaymentPeriodic.yml";
    private static final String PATH_PERIODIC_TO = "PaymentPeriodicTO.yml";
    private static final String PATH_BULK_BO = "PaymentBulk.yml";
    private static final String PATH_BULK_TO = "PaymentBulkTO.yml";

    @InjectMocks
    private PaymentConverterImpl converter;

    private static ObjectMapper mapper = getObjectMapper();

    @Test
    void toPaymentTO() {
        PaymentTO result = converter.toPaymentTO(readYml(PaymentBO.class, PATH_SINGLE_BO));
        assertEquals(readYml(PaymentTO.class, PATH_SINGLE_BO), result);
    }

    @Test
    void toTransactionTO() {
        // When
        TransactionTO result = converter.toTransactionTO(readYml(TransactionDetailsBO.class, "TransactionBO.yml"));

        // Then
        assertEquals(readYml(TransactionTO.class, "TransactionTO.yml"), result);
    }

    @Test
    void toTransactionDetailsBO() {
        // When
        TransactionDetailsBO result = converter.toTransactionDetailsBO(readYml(TransactionTO.class, "TransactionTO.yml"));

        // Then
        assertEquals(readYml(TransactionDetailsBO.class, "TransactionBO.yml"), result);
    }

    private static <T> T readYml(Class<T> aClass, String file) {
        try {
            return mapper.readValue(PaymentConverterTest.class.getResourceAsStream(file), aClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }

    static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
}
