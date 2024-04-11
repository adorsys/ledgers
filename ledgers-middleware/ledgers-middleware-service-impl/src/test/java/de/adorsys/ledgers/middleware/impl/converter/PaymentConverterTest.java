/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.RemittanceInformationStructuredTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentConverterImpl.class, RemittanceMapper.class, ObjectMapper.class})
class PaymentConverterTest {
    private static final String PATH_SINGLE_BO = "PaymentSingle.yml";
    private static final String PATH_SINGLE_TO = "PaymentSingleTO.yml";
    private static final String PATH_PERIODIC_BO = "PaymentPeriodic.yml";
    private static final String PATH_PERIODIC_TO = "PaymentPeriodicTO.yml";
    private static final String PATH_BULK_BO = "PaymentBulk.yml";
    private static final String PATH_BULK_TO = "PaymentBulkTO.yml";

    @Autowired
    private PaymentConverter converter;

    private static ObjectMapper mapper = getObjectMapper();

    private final ObjectMapper defaultMapper = new ObjectMapper();

    @Test
    void toPaymentTO() throws JsonProcessingException {
        PaymentBO paymentBO = readYml(PaymentBO.class, PATH_SINGLE_BO);
        paymentBO.getTargets().get(0).setRemittanceInformationUnstructuredArray(defaultMapper.writeValueAsBytes(Collections.singletonList("remittance")));
        PaymentTO result = converter.toPaymentTO(paymentBO);
        assertEquals(readYml(PaymentTO.class, PATH_SINGLE_TO), result);
    }

    @Test
    void toTransactionTO() throws JsonProcessingException {
        TransactionDetailsBO transactionDetailsBO = readYml(TransactionDetailsBO.class, "TransactionBO.yml");
        RemittanceInformationStructuredTO remittanceInformationStructuredTO = new RemittanceInformationStructuredTO();
        remittanceInformationStructuredTO.setReference("reference");
        remittanceInformationStructuredTO.setReferenceType("reference type");
        remittanceInformationStructuredTO.setReferenceIssuer("reference issuer");
        transactionDetailsBO.setRemittanceInformationUnstructuredArray(defaultMapper.writeValueAsBytes(Collections.singletonList("remittance")));
        transactionDetailsBO.setRemittanceInformationStructuredArray(defaultMapper.writeValueAsBytes(Collections.singletonList(remittanceInformationStructuredTO)));
        // When
        TransactionTO result = converter.toTransactionTO(transactionDetailsBO);

        // Then
        assertEquals(readYml(TransactionTO.class, "TransactionTO.yml"), result);
    }

    @Test
    void toTransactionDetailsBO() throws JsonProcessingException {
        TransactionDetailsBO transactionDetailsBO = readYml(TransactionDetailsBO.class, "TransactionBO.yml");
        RemittanceInformationStructuredTO remittanceInformationStructuredTO = new RemittanceInformationStructuredTO();
        remittanceInformationStructuredTO.setReference("reference");
        remittanceInformationStructuredTO.setReferenceType("reference type");
        remittanceInformationStructuredTO.setReferenceIssuer("reference issuer");
        transactionDetailsBO.setRemittanceInformationUnstructuredArray(defaultMapper.writeValueAsBytes(Collections.singletonList("remittance")));
        transactionDetailsBO.setRemittanceInformationStructuredArray(defaultMapper.writeValueAsBytes(Collections.singletonList(remittanceInformationStructuredTO)));
        // When
        TransactionDetailsBO result = converter.toTransactionDetailsBO(readYml(TransactionTO.class, "TransactionTO.yml"));

        // Then
        assertEquals(transactionDetailsBO, result);
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
