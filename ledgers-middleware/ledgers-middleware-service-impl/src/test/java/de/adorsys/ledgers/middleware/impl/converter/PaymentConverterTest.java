package de.adorsys.ledgers.middleware.impl.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

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
    void toPaymentResultTO() {
        // Given
        PaymentResultBO bo = readYml(PaymentResultBO.class, "payment-result.yml");

        // When
        PaymentResultTO to = converter.toPaymentResultTO(bo);

        // Then
        assertEquals(ResultStatusTO.SUCCESS, to.getResponseStatus());
        assertEquals(TransactionStatusTO.RCVD.name(), to.getPaymentResult());
        assertEquals(2, to.getMessages().size());
        assertEquals("message1", to.getMessages().get(0));
        assertEquals("message2", to.getMessages().get(1));
    }

    @Test
    void toPaymentResultBO() {
        // Given
        PaymentResultTO to = readYml(PaymentResultTO.class, "payment-result.yml");

        PaymentResultBO bo = converter.toPaymentResultBO(to);

        assertEquals(ResultStatusBO.SUCCESS, bo.getResponseStatus());
        assertEquals(TransactionStatusBO.RCVD.name(), bo.getPaymentResult());
        assertEquals(2, bo.getMessages().size());
        assertEquals("message1", bo.getMessages().get(0));
        assertEquals("message2", bo.getMessages().get(1));
    }

    @Test
    void toSinglePaymentTO() {
        // Given
        PaymentBO paymentBO = readYml(PaymentBO.class, PATH_SINGLE_BO);
        SinglePaymentTO expected = readYml(SinglePaymentTO.class, PATH_SINGLE_TO);

        // When
        SinglePaymentTO payment = converter.toSinglePaymentTO(paymentBO, paymentBO.getTargets().get(0));

        // Then
        assertNotNull(payment);
        assertEquals(expected, payment);
    }

    @Test
    void toPeriodicPaymentTO() {
        // Given
        PaymentBO paymentBO = readYml(PaymentBO.class, PATH_PERIODIC_BO);
        PeriodicPaymentTO expected = readYml(PeriodicPaymentTO.class, PATH_PERIODIC_TO);

        // When
        SinglePaymentTO payment = converter.toPeriodicPaymentTO(paymentBO, paymentBO.getTargets().get(0));

        // Then
        assertNotNull(payment);
        assertEquals(expected, payment);
    }

    @Test
    void toBulkPaymentTO() {
        // Given
        PaymentBO paymentBO = readYml(PaymentBO.class, PATH_BULK_BO);
        BulkPaymentTO expected = readYml(BulkPaymentTO.class, PATH_BULK_TO);

        // When
        BulkPaymentTO payment = converter.toBulkPaymentTO(paymentBO, paymentBO.getTargets().get(0));

        // Then
        assertNotNull(payment);
        assertEquals(expected, payment);
    }

    @Test
    void toPaymentTypeBO() {
        // Then
        assertEquals(PaymentTypeTO.values().length, PaymentTypeBO.values().length);
        assertEquals(PaymentTypeBO.SINGLE, converter.toPaymentTypeBO(PaymentTypeTO.SINGLE));
        assertEquals(PaymentTypeBO.PERIODIC, converter.toPaymentTypeBO(PaymentTypeTO.PERIODIC));
        assertEquals(PaymentTypeBO.BULK, converter.toPaymentTypeBO(PaymentTypeTO.BULK));
    }

    @Test
    void toPaymentTypeTO() {
        // Then
        assertEquals(PaymentTypeTO.values().length, PaymentTypeBO.values().length);
        assertEquals(PaymentTypeTO.SINGLE, converter.toPaymentTypeTO(PaymentTypeBO.SINGLE));
        assertEquals(PaymentTypeTO.PERIODIC, converter.toPaymentTypeTO(PaymentTypeBO.PERIODIC));
        assertEquals(PaymentTypeTO.BULK, converter.toPaymentTypeTO(PaymentTypeBO.BULK));
    }

    @Test
    void toPaymentTO() {
        // Then
        assertTrue(converter.toPaymentTO(readYml(PaymentBO.class, PATH_SINGLE_BO)) instanceof SinglePaymentTO);
        assertTrue(converter.toPaymentTO(readYml(PaymentBO.class, PATH_PERIODIC_BO)) instanceof PeriodicPaymentTO);
        assertTrue(converter.toPaymentTO(readYml(PaymentBO.class, PATH_BULK_BO)) instanceof BulkPaymentTO);
    }

    @Test
    void toSingleBulkPartTO() {
        // Given
        PaymentBO payment = readYml(PaymentBO.class, PATH_BULK_BO);
        SinglePaymentTO expectedResult = readYml(BulkPaymentTO.class, PATH_BULK_TO).getPayments().get(0);

        // When
        SinglePaymentTO result = converter.toSingleBulkPartTO(payment, payment.getTargets().get(0));

        // Then
        assertEquals(expectedResult, result);
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
