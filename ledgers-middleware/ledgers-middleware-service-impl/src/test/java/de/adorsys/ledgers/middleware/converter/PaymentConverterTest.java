package de.adorsys.ledgers.middleware.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.middleware.service.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.service.domain.payment.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentConverterTest {
    private static final String PATH_SINGLE_BO = "de/adorsys/ledgers/middleware/converter/PaymentSingle.yml";
    private static final String PATH_SINGLE_TO = "de/adorsys/ledgers/middleware/converter/PaymentSingleTO.yml";
    private static final String PATH_PERIODIC_BO = "de/adorsys/ledgers/middleware/converter/PaymentPeriodic.yml";
    private static final String PATH_PERIODIC_TO = "de/adorsys/ledgers/middleware/converter/PaymentPeriodicTO.yml";
    private static final String PATH_BULK_BO = "de/adorsys/ledgers/middleware/converter/PaymentBulk.yml";
    private static final String PATH_BULK_TO = "de/adorsys/ledgers/middleware/converter/PaymentBulkTO.yml";

    @InjectMocks
    private PaymentConverterImpl converter;
    @Mock
    private ObjectMapper mapper;

    @Test
    public void toPaymentResultTO() throws IOException {
        PaymentResultBO bo = YamlReader.getInstance().getObjectFromFile("de/adorsys/ledgers/middleware/converter/payment-result.yml", PaymentResultBO.class);
        PaymentResultTO to = converter.toPaymentResultTO(bo);

        assertThat(to.getResponseStatus(), is(ResultStatusTO.SUCCESS));
        assertThat(to.getPaymentResult(), is(TransactionStatusTO.RCVD.name()));
        assertThat(to.getMessages().size(), is(2));
        assertThat(to.getMessages().get(0), is("message1"));
        assertThat(to.getMessages().get(1), is("message2"));
    }

    @Test
    public void toPaymentResultBO() throws IOException {
        PaymentResultTO to = YamlReader.getInstance().getObjectFromResource(PaymentConverter.class, "payment-result.yml", PaymentResultTO.class);

        PaymentResultBO bo = converter.toPaymentResultBO(to);

        assertThat(bo.getResponseStatus(), is(ResultStatusBO.SUCCESS));
        assertThat(bo.getPaymentResult(), is(TransactionStatusBO.RCVD.name()));
        assertThat(bo.getMessages().size(), is(2));
        assertThat(bo.getMessages().get(0), is("message1"));
        assertThat(bo.getMessages().get(1), is("message2"));
    }

    @Test
    public void toSinglePaymentTO() {
        //Given
        PaymentBO paymentBO = getPayment(PaymentBO.class, PATH_SINGLE_BO);
        SinglePaymentTO expected = getPayment(SinglePaymentTO.class, PATH_SINGLE_TO);

        //When
        SinglePaymentTO payment = converter.toSinglePaymentTO(paymentBO, paymentBO.getTargets().get(0));
        assertThat(payment).isNotNull();
        assertThat(payment).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void toPeriodicPaymentTO() {
        //Given
        PaymentBO paymentBO = getPayment(PaymentBO.class, PATH_PERIODIC_BO);
        PeriodicPaymentTO expected = getPayment(PeriodicPaymentTO.class, PATH_PERIODIC_TO);

        //When
        SinglePaymentTO payment = converter.toPeriodicPaymentTO(paymentBO, paymentBO.getTargets().get(0));
        assertThat(payment).isNotNull();
        assertThat(payment).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void toBulkPaymentTO() {
        //Given
        PaymentBO paymentBO = getPayment(PaymentBO.class, PATH_BULK_BO);
        BulkPaymentTO expected = getPayment(BulkPaymentTO.class, PATH_BULK_TO);

        //When
        BulkPaymentTO payment = converter.toBulkPaymentTO(paymentBO, paymentBO.getTargets().get(0));
        assertThat(payment).isNotNull();
        assertThat(payment).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void toPaymentTypeBO() {
        assertThat(PaymentTypeBO.values().length).isEqualTo(PaymentTypeTO.values().length);
        assertThat(converter.toPaymentTypeBO(PaymentTypeTO.SINGLE)).isEqualTo(PaymentTypeBO.SINGLE);
        assertThat(converter.toPaymentTypeBO(PaymentTypeTO.PERIODIC)).isEqualTo(PaymentTypeBO.PERIODIC);
        assertThat(converter.toPaymentTypeBO(PaymentTypeTO.BULK)).isEqualTo(PaymentTypeBO.BULK);
    }

    @Test
    public void toPaymentTypeTO() {
        assertThat(PaymentTypeBO.values().length).isEqualTo(PaymentTypeTO.values().length);
        assertThat(converter.toPaymentTypeTO(PaymentTypeBO.SINGLE)).isEqualTo(PaymentTypeTO.SINGLE);
        assertThat(converter.toPaymentTypeTO(PaymentTypeBO.PERIODIC)).isEqualTo(PaymentTypeTO.PERIODIC);
        assertThat(converter.toPaymentTypeTO(PaymentTypeBO.BULK)).isEqualTo(PaymentTypeTO.BULK);
    }

    @Test
    public void toPaymentProductBO() {
        assertThat(PaymentProductBO.values().length).isEqualTo(PaymentProductTO.values().length);
        assertThat(converter.toPaymentProductBO(PaymentProductTO.SEPA)).isEqualTo(PaymentProductBO.SEPA);
        assertThat(converter.toPaymentProductBO(PaymentProductTO.CROSS_BORDER)).isEqualTo(PaymentProductBO.CROSS_BORDER);
        assertThat(converter.toPaymentProductBO(PaymentProductTO.INSTANT_SEPA)).isEqualTo(PaymentProductBO.INSTANT_SEPA);
        assertThat(converter.toPaymentProductBO(PaymentProductTO.TARGET2)).isEqualTo(PaymentProductBO.TARGET2);
    }

    @Test
    public void toPaymentProductTO() {
        assertThat(PaymentProductTO.values().length).isEqualTo(PaymentProductBO.values().length);
        assertThat(converter.toPaymentProductTO(PaymentProductBO.SEPA)).isEqualTo(PaymentProductTO.SEPA);
        assertThat(converter.toPaymentProductTO(PaymentProductBO.CROSS_BORDER)).isEqualTo(PaymentProductTO.CROSS_BORDER);
        assertThat(converter.toPaymentProductTO(PaymentProductBO.INSTANT_SEPA)).isEqualTo(PaymentProductTO.INSTANT_SEPA);
        assertThat(converter.toPaymentProductTO(PaymentProductBO.TARGET2)).isEqualTo(PaymentProductTO.TARGET2);
    }

    @Test
    public void toPaymentTO() {
        assertThat(converter.toPaymentTO(getPayment(PaymentBO.class, PATH_SINGLE_BO))).isExactlyInstanceOf(SinglePaymentTO.class);
        assertThat(converter.toPaymentTO(getPayment(PaymentBO.class, PATH_PERIODIC_BO))).isExactlyInstanceOf(PeriodicPaymentTO.class);
        assertThat(converter.toPaymentTO(getPayment(PaymentBO.class, PATH_BULK_BO))).isExactlyInstanceOf(BulkPaymentTO.class);
    }

    @Test
    public void toSingleBulkPartTO() {
        PaymentBO payment = getPayment(PaymentBO.class, PATH_BULK_BO);
        SinglePaymentTO expectedResult = getPayment(BulkPaymentTO.class, PATH_BULK_TO).getPayments().get(0);
        SinglePaymentTO result = converter.toSingleBulkPartTO(payment, payment.getTargets().get(0));
        assertThat(result).isEqualToComparingFieldByFieldRecursively(expectedResult);
    }

    @Test
    public void toPaymentBOFromGeneric() {
        when(mapper.convertValue(any(), eq(SinglePaymentTO.class))).thenReturn(new SinglePaymentTO());
        when(mapper.convertValue(any(), eq(PeriodicPaymentTO.class))).thenReturn(new PeriodicPaymentTO());
        when(mapper.convertValue(any(), eq(BulkPaymentTO.class))).thenReturn(new BulkPaymentTO());
        assertThat(converter.toPaymentBO(new SinglePaymentTO(), SinglePaymentTO.class).getPaymentType()).isEqualTo(PaymentTypeBO.SINGLE);
        assertThat(converter.toPaymentBO(new PeriodicPaymentTO(), PeriodicPaymentTO.class).getPaymentType()).isEqualTo(PaymentTypeBO.PERIODIC);
        assertThat(converter.toPaymentBO(new BulkPaymentTO(), BulkPaymentTO.class).getPaymentType()).isEqualTo(PaymentTypeBO.BULK);
    }

    @Test
    public void toPaymentBO() {
        //BulkPartMapping
        assertThat(converter.toPaymentTarget(getPayment(SinglePaymentTO.class, PATH_SINGLE_TO)))
                .isEqualToComparingFieldByFieldRecursively(getPayment(PaymentBO.class, PATH_SINGLE_BO).getTargets().get(0));
        //SinglePayment
        assertThat(converter.toPaymentBO(getPayment(SinglePaymentTO.class, PATH_SINGLE_TO)))
                .isEqualToComparingFieldByFieldRecursively(getPayment(PaymentBO.class, PATH_SINGLE_BO));
        //PeriodicPayment
        assertThat(converter.toPaymentBO(getPayment(PeriodicPaymentTO.class, PATH_PERIODIC_TO)))
                .isEqualToComparingFieldByFieldRecursively(getPayment(PaymentBO.class, PATH_PERIODIC_BO));
        //BulkPayment
        assertThat(converter.toPaymentBO(getPayment(BulkPaymentTO.class, PATH_BULK_TO)))
                .isEqualToComparingFieldByFieldRecursively(getPayment(PaymentBO.class, PATH_BULK_BO));
    }

    private static <T> T getPayment(Class<T> aClass, String path) {
        try {
            return YamlReader.getInstance().getObjectFromFile(path, aClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }

    @Test
    public void toTransactionTO() {
        TransactionTO result = converter.toTransactionTO(readYml(TransactionDetailsBO.class, "TransactionBO.yml"));
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(TransactionTO.class, "TransactionTO.yml"));
    }

    @Test
    public void toTransactionDetailsBO() {
        TransactionDetailsBO result = converter.toTransactionDetailsBO(readYml(TransactionTO.class, "TransactionTO.yml"));
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(TransactionDetailsBO.class, "TransactionBO.yml"));
    }

    private static <T> T readYml(Class<T> aClass, String file) {
        try {
            return YamlReader.getInstance().getObjectFromResource(PaymentConverter.class, file, aClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }
}