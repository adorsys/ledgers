package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.impl.DepositAccountServiceImpl;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.util.Ids;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class PaymentMapperTest {
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
    public void toPayment_Single() {
        PaymentBO result = mapper.toPaymentBO(SINGLE_PMT);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(SINGLE_PMT_BO);
    }

    @Test
    public void toPayment_Bulk() {
        PaymentBO result = mapper.toPaymentBO(BULK_PMT);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(BULK_PMT_BO);
    }

    @Test
    public void toPaymentBO_Single() {
        Payment result = mapper.toPayment(SINGLE_PMT_BO);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(SINGLE_PMT);
    }

    @Test
    public void toPaymentBO_Bulk() {
        Payment result = mapper.toPayment(BULK_PMT_BO);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(BULK_PMT);
    }

    @Test
    public void toPaymentOrder() {
        PaymentOrderDetailsBO result = mapper.toPaymentOrder(SINGLE_PMT_BO);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(PaymentOrderDetailsBO.class, "PaymentOrderSingle.yml"));
    }

    @Test
    public void toPaymentTargetDetails() {
        PaymentTargetDetailsBO result = mapper.toPaymentTargetDetails(TRANSACTION_ID, readYml(PaymentTargetBO.class, "PaymentTarget.yml"), LocalDate.of(2018, 12, 12), null, null);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(PaymentTargetDetailsBO.class, "PaymentTargetDetails.yml"));
    }

    @Test
    public void toPaymentTargetDetailsBatch() {
        PaymentBO payment = readYml(PaymentBO.class, "PaymentBulkBatchTrue.yml");
        AmountBO amount = new AmountBO(Currency.getInstance("EUR"), BigDecimal.valueOf(200));
        LocalDate date = LocalDate.of(2018, 12, 12);
        PaymentTargetDetailsBO result = mapper.toPaymentTargetDetailsBatch(TRANSACTION_ID, payment, amount, date, null, null);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(PaymentTargetDetailsBO.class, "PaymentTargetDetailsBatch.yml"));
    }

    @Test
    public void trDetailsForDepositOperation() {
        TransactionDetailsBO result = mapper.toDepositTransactionDetails(new AmountBO(EUR, BigDecimal.TEN), getDepositAccount(), new AccountReferenceBO(), POSTING_DATE, LINE_ID, null);
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isNotNull();
        assertThat(result).isEqualToIgnoringGivenFields(getDepositTrDetails(), "transactionId");
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
