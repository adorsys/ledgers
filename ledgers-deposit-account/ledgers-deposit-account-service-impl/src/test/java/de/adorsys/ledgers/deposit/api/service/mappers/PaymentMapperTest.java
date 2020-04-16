package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.impl.DepositAccountServiceImpl;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.util.Ids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class PaymentMapperTest {

    private static final String SINGLE_PATH = "PaymentSingle.yml";
    private static final String BULK_PATH = "PaymentBulk.yml";
    private static final String TRANSACTION_ID = "TR_1";
    private static final String LINE_ID = "LINE_1";
    private static final LocalDate POSTING_DATE = LocalDate.now();
    private static final Currency EUR = Currency.getInstance("EUR");
    private final Payment SINGLE_PMT = readYml(Payment.class, SINGLE_PATH);
    private final PaymentBO SINGLE_PMT_BO = readYml(PaymentBO.class, SINGLE_PATH);
    private final Payment BULK_PMT = readYml(Payment.class, BULK_PATH);
    private final PaymentBO BULK_PMT_BO = readYml(PaymentBO.class, BULK_PATH);

    @InjectMocks
    private PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

    @Test
    void toPayment_Single() {
        // When
        PaymentBO result = mapper.toPaymentBO(SINGLE_PMT);

        // Then
        assertNotNull(result);
        assertEquals(SINGLE_PMT_BO, result);
    }

    @Test
    void toPayment_Bulk() {
        // When
        PaymentBO result = mapper.toPaymentBO(BULK_PMT);

        // Then
        assertNotNull(result);
        assertEquals(BULK_PMT_BO, result);
    }

    @Test
    void toPaymentBO_Single() {
        // When
        Payment result = mapper.toPayment(SINGLE_PMT_BO);

        // Then
        assertNotNull(result);
        assertEquals(SINGLE_PMT, result);
    }

    @Test
    void toPaymentBO_Bulk() {
        // When
        Payment result = mapper.toPayment(BULK_PMT_BO);

        // Then
        assertNotNull(result);
        assertEquals(BULK_PMT, result);
    }

    @Test
    void toPaymentOrder() {
        // When
        PaymentOrderDetailsBO result = mapper.toPaymentOrder(SINGLE_PMT_BO);

        // Then
        assertNotNull(result);
        assertEquals(readYml(PaymentOrderDetailsBO.class, "PaymentOrderSingle.yml"), result);
    }

    @Test
    void toPaymentTargetDetails() {
        // When
        PaymentTargetDetailsBO result = mapper.toPaymentTargetDetails(TRANSACTION_ID, readYml(PaymentTargetBO.class, "PaymentTarget.yml"), LocalDate.of(2018, 12, 12), null, null);

        // Then
        assertNotNull(result);
        assertEquals(readYml(PaymentTargetDetailsBO.class, "PaymentTargetDetails.yml"), result);
    }

    @Test
    void toPaymentTargetDetailsBatch() {
        // Given
        PaymentBO payment = readYml(PaymentBO.class, "PaymentBulkBatchTrue.yml");
        AmountBO amount = new AmountBO(Currency.getInstance("EUR"), BigDecimal.valueOf(200));
        LocalDate date = LocalDate.of(2018, 12, 12);

        // When
        PaymentTargetDetailsBO result = mapper.toPaymentTargetDetailsBatch(TRANSACTION_ID, payment, amount, date, null, null);

        // Then
        assertNotNull(result);
        assertEquals(readYml(PaymentTargetDetailsBO.class, "PaymentTargetDetailsBatch.yml"), result);
    }

    @Test
    void trDetailsForDepositOperation() {
        // When
        TransactionDetailsBO result = mapper.toDepositTransactionDetails(new AmountBO(EUR, BigDecimal.TEN), getDepositAccount(), new AccountReferenceBO(), POSTING_DATE, LINE_ID, null);

        TransactionDetailsBO expected = getDepositTrDetails();
        expected.setTransactionId(result.getTransactionId());

        // Then
        assertNotNull(result);
        assertNotNull(result.getTransactionId());
        assertEquals(expected, result);
    }

    private DepositAccountBO getDepositAccount() {
        return new DepositAccountBO("id", "IBAN", null, null, null, null, EUR, "Anton Brueckner", null, null, null, null, null, null, null);
    }

    private TransactionDetailsBO getDepositTrDetails() {
        TransactionDetailsBO t = new TransactionDetailsBO();
        t.setTransactionId(Ids.id());
        t.setEndToEndId(LINE_ID);
        t.setBookingDate(POSTING_DATE);
        t.setValueDate(POSTING_DATE);
        t.setTransactionAmount(new AmountBO(EUR, BigDecimal.TEN));
        t.setCreditorAccount(new AccountReferenceBO());
        t.setCreditorName(getDepositAccount().getName());
        t.setDebtorName(getDepositAccount().getName());
        t.setDebtorAccount(getDepositAccount().getReference());
        t.setBankTransactionCode("PMNT-MCOP-OTHR");
        t.setProprietaryBankTransactionCode("PMNT-MCOP-OTHR");
        t.setRemittanceInformationUnstructured("Cash deposit through Bank ATM");
        return t;
    }

    private <T> T readYml(Class<T> t, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(DepositAccountServiceImpl.class, fileName, t);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }
}
