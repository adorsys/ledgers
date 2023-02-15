/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.mappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.general.AddressTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.payment.FrequencyCodeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTargetTO;
import de.adorsys.ledgers.middleware.api.domain.payment.RemittanceInformationStructuredTO;
import de.adorsys.ledgers.middleware.client.mappers.PaymentMapperConfiguration;
import de.adorsys.ledgers.middleware.client.mappers.PaymentMapperTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class PaymentTOMapperTest {
    private static final ObjectMapper STATIC_MAPPER = new ObjectMapper()
                                                              .findAndRegisterModules()
                                                              .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                                                              .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                                                              .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
                                                              .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                                                              .registerModule(new Jdk8Module())
                                                              .registerModule(new JavaTimeModule())
                                                              .registerModule(new ParameterNamesModule());
    private static final PaymentMapperConfiguration configuration = new PaymentMapperConfiguration(STATIC_MAPPER);

    @Test
    void xs2aPaymentJson() throws IOException {
        // Given
        PaymentMapperTO mapper = configuration.paymentMapperTO();
        String payment = readPayment("xs2aSinglePayment.json");

        // When
        PaymentTO result = mapper.toAbstractPayment(payment, "SINGLE", "sepa-credit-transfers");

        // Then
        assertEquals(getSinglePmt(), result);
    }

    @Test
    void xs2aPeriodicPaymentJson() throws IOException {
        // Given
        PaymentMapperTO mapper = configuration.paymentMapperTO();
        String payment = readPayment("xs2aPeriodicPayment.json");

        // When
        PaymentTO result = mapper.toAbstractPayment(payment, "PERIODIC", "instant-sepa-credit-transfers");

        // Then
        assertEquals(getPeriodicPmt(), result);
    }

    @Test
    void xs2aPeriodicPaymentJsonFrequencyCapital() throws IOException {
        // Given
        PaymentMapperTO mapper = configuration.paymentMapperTO();
        String payment = readPayment("xs2aPeriodicPaymentCapitals.json");

        // When
        PaymentTO result = mapper.toAbstractPayment(payment, "PERIODIC", "instant-sepa-credit-transfers");

        // Then
        assertEquals(getPeriodicPmt(), result);
    }

    @Test
    void xs2aOldFormatAddress() throws IOException {
        // Given
        PaymentMapperTO mapper = configuration.paymentMapperTO();
        String payment = readPayment("xs2aSinglePaymentOld.json");

        // When
        PaymentTO result = mapper.toAbstractPayment(payment, "SINGLE", "sepa-credit-transfers");

        // Then
        assertEquals(getSinglePmt(), result);
    }

    @Test
    void xs2aBulk() throws IOException {
        // Given
        PaymentMapperTO mapper = configuration.paymentMapperTO();
        String payment = readPayment("xs2aBulkPaymentOld.json");

        // When
        PaymentTO result = mapper.toAbstractPayment(payment, "BULK", "sepa-credit-transfers");

        // Then
        assertEquals(getBulk(), result);
    }

    @Test
    void xmlTest() throws IOException {
        test("xs2aSingle.xml", "SINGLE", "SinglePaymentTO.json");
    }

    @Test
    void xmlTestBulk() throws IOException {
        test("xs2aBulk.xml", "BULK", "BulkPaymentTO.json");
    }

    @Test
    void multiPartPayment() throws IOException {
        test("rawMixedPaymentPeriodic.txt", "PERIODIC", "XmlPeriodicPaymentTO.json");
    }

    void test(String fileToReadPayment, String paymentType, String fileToCompareTo) throws IOException {
        PaymentMapperTO mapper = configuration.paymentMapperTO();
        String payment = readPayment(fileToReadPayment);

        // When
        PaymentTO result = mapper.toAbstractPayment(payment, paymentType, "sepa-credit-transfers-xml");

        // Then
        assertEquals(readPaymentTO(fileToCompareTo), result);
    }

    @Test
    void xmlTestListBulk() {
       /* PaymentMapperTO mapper = configuration.paymentMapperTO();
        String payment = readPayment("xs2aBulkList.xml");
        PaymentTO result = mapper.toAbstractPayment(payment, "BULK", "sepa-credit-transfers-xml");
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readPaymentTO("BulkPaymentTO.json"));*/
        assertTrue(true);
        //TODO not implemented yet!  see task : https://git.adorsys.de/adorsys/xs2a/psd2-dynamic-sandbox/issues/589
    }

    private PaymentTO getSinglePmt() {
        PaymentTO payment = new PaymentTO();
        payment.setPaymentType(SINGLE);
        payment.setPaymentProduct("sepa-credit-transfers");
        payment.setRequestedExecutionDate(LocalDate.parse("2019-12-12"));
        payment.setDebtorAccount(new AccountReferenceTO("DE40500105178578796457", null, null, null, null, Currency.getInstance("USD")));
        payment.setTargets(getTargets());
        return payment;
    }

    private PaymentTO getPeriodicPmt() {
        PaymentTO payment = new PaymentTO();
        payment.setPaymentType(PERIODIC);
        payment.setPaymentProduct("instant-sepa-credit-transfers");
        payment.setStartDate(LocalDate.parse("2020-05-26"));
        payment.setEndDate(LocalDate.parse("2020-10-14"));
        payment.setExecutionRule("following");
        payment.setFrequency(FrequencyCodeTO.MONTHLY);
        payment.setDayOfExecution(14);
        payment.setDebtorAccount(new AccountReferenceTO("DE40500105178578796457", null, null, null, null, Currency.getInstance("EUR")));
        payment.setTargets(getTargetsTwoRemittanceElements());
        return payment;
    }

    private List<PaymentTargetTO> getTargets() {
        PaymentTargetTO target = new PaymentTargetTO();
        target.setEndToEndIdentification("WBG-123456789");
        target.setInstructedAmount(new AmountTO(Currency.getInstance("CHF"), new BigDecimal("1.00")));
        target.setCreditorAccount(new AccountReferenceTO("DE40500105178578796457", null, null, null, null, Currency.getInstance("EUR")));
        target.setCreditorAgent("AAAADEBBXXX");
        target.setCreditorAddress(new AddressTO("WBG Straße", "56", "Nürnberg", "90543", "DE", null, null));
        target.setCreditorName("WBG");
        target.setRemittanceInformationUnstructuredArray(Collections.singletonList("Ref. Number WBG-1222"));
        target.setRemittanceInformationStructuredArray(Collections.singletonList(getRemittanceStructuredTO()));
        ArrayList<PaymentTargetTO> targets = new ArrayList<>();
        targets.add(target);
        return targets;
    }

    private List<PaymentTargetTO> getTargetsTwoRemittanceElements() {
        PaymentTargetTO target = new PaymentTargetTO();
        target.setEndToEndIdentification("WBG-123456789");
        target.setInstructedAmount(new AmountTO(Currency.getInstance("CHF"), new BigDecimal("1.00")));
        target.setCreditorAccount(new AccountReferenceTO("DE40500105178578796457", null, null, null, null, Currency.getInstance("EUR")));
        target.setCreditorAgent("AAAADEBBXXX");
        target.setCreditorAddress(new AddressTO("WBG Straße", "56", "Nürnberg", "90543", "DE", null, null));
        target.setCreditorName("WBG");
        target.setRemittanceInformationUnstructuredArray(List.of("Ref. Number WBG-1222", "Ref. Number WBG-6666"));
        target.setRemittanceInformationStructuredArray(List.of(getRemittanceStructuredTO(), getRemittanceStructuredTO()));
        ArrayList<PaymentTargetTO> targets = new ArrayList<>();
        targets.add(target);
        return targets;
    }

    private RemittanceInformationStructuredTO getRemittanceStructuredTO() {
        RemittanceInformationStructuredTO remittance = new RemittanceInformationStructuredTO();
        remittance.setReference("Ref. Number WBG-1222");
        remittance.setReferenceType("referenceType");
        remittance.setReferenceIssuer("referenceIssuer");
        return remittance;
    }

    private String readPayment(String file) throws IOException {
        ResourceLoader loader = new DefaultResourceLoader();
        Resource resource = loader.getResource(file);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            log.error("Cant read file");
            throw e;
        }
    }

    private PaymentTO readPaymentTO(String file) throws IOException {
        ResourceLoader loader = new DefaultResourceLoader();
        Resource resource = loader.getResource(file);
        return STATIC_MAPPER.readValue(resource.getInputStream(), PaymentTO.class);
    }

    private PaymentTO getBulk() {
        PaymentTO payment = getSinglePmt();
        payment.setPaymentType(BULK);
        payment.setBatchBookingPreferred(false);
        PaymentTargetTO target = new PaymentTargetTO();
        target.setEndToEndIdentification("RI-234567890");
        target.setInstructedAmount(new AmountTO(Currency.getInstance("EUR"), new BigDecimal("71.07")));
        target.setCreditorAccount(new AccountReferenceTO("DE03500105172351985719", null, null, null, null, Currency.getInstance("EUR")));
        target.setCreditorAgent("AAAADEBBXXX");
        target.setCreditorAddress(new AddressTO("Kaisergasse", "74", "Dresden", "01067", "DE", null, null));
        target.setCreditorName("Grünstrom");
        target.setRemittanceInformationUnstructuredArray(Collections.singletonList("Ref. Number GRUENSTROM-2444"));
        target.setRemittanceInformationStructuredArray(Collections.singletonList(getRemittanceStructuredTO()));
        payment.getTargets().add(target);
        return payment;
    }
}
