package de.adorsys.ledgers.middleware.converter;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.middleware.service.domain.payment.*;
import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PaymentConverterTest {
    private static final String PATH_SINGLE_BO = "de/adorsys/ledgers/middleware/converter/PaymentSingle.yml";
    private static final String PATH_SINGLE_TO = "de/adorsys/ledgers/middleware/converter/PaymentSingleTO.yml";
    private static final String PATH_PERIODIC_BO = "de/adorsys/ledgers/middleware/converter/PaymentPeriodic.yml";
    private static final String PATH_PERIODIC_TO = "de/adorsys/ledgers/middleware/converter/PaymentPeriodicTO.yml";
    private static final String PATH_BULK_BO = "de/adorsys/ledgers/middleware/converter/PaymentBulk.yml";
    private static final String PATH_BULK_TO = "de/adorsys/ledgers/middleware/converter/PaymentBulkTO.yml";

    private PaymentConverter mapper;

    @Before
    public void setUp() {
        mapper = Mappers.getMapper(PaymentConverter.class);
    }

    @Test
    public void toPaymentResultTO() throws IOException {
        PaymentResultBO bo = YamlReader.getInstance().getObjectFromFile("de/adorsys/ledgers/middleware/converter/payment-result.yml", PaymentResultBO.class);
        PaymentResultTO to = mapper.toPaymentResultTO(bo);

        assertThat(to.getResponseStatus(), is(ResultStatusTO.SUCCESS));
        assertThat(to.getPaymentResult(), is(TransactionStatusTO.RCVD.name()));
        assertThat(to.getMessages().size(), is(2));
        assertThat(to.getMessages().get(0), is("message1"));
        assertThat(to.getMessages().get(1), is("message2"));
    }

    @Test
    public void toPaymentResultBO() throws IOException {
        PaymentResultTO to = YamlReader.getInstance().getObjectFromResource(PaymentConverter.class, "payment-result.yml", PaymentResultTO.class);

        PaymentResultBO bo = mapper.toPaymentResultBO(to);

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
        SinglePaymentTO payment = mapper.toSinglePaymentTO(paymentBO, paymentBO.getTargets().get(0));
        assertThat(payment).isNotNull();
        assertThat(payment).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void toPeriodicPaymentTO() {
        //Given
        PaymentBO paymentBO = getPayment(PaymentBO.class, PATH_PERIODIC_BO);
        PeriodicPaymentTO expected = getPayment(PeriodicPaymentTO.class, PATH_PERIODIC_TO);

        //When
        SinglePaymentTO payment = mapper.toPeriodicPaymentTO(paymentBO, paymentBO.getTargets().get(0));
        assertThat(payment).isNotNull();
        assertThat(payment).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void toBulkPaymentTO() {
        //Given
        PaymentBO paymentBO = getPayment(PaymentBO.class, PATH_BULK_BO);
        BulkPaymentTO expected = getPayment(BulkPaymentTO.class, PATH_BULK_TO);

        //When
        BulkPaymentTO payment = mapper.toBulkPaymentTO(paymentBO, paymentBO.getTargets().get(0));
        assertThat(payment).isNotNull();
        assertThat(payment).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void toPaymentTypeBO() {
        assertThat(PaymentTypeBO.values().length).isEqualTo(PaymentTypeTO.values().length);
        assertThat(mapper.toPaymentTypeBO(PaymentTypeTO.SINGLE)).isEqualTo(PaymentTypeBO.SINGLE);
        assertThat(mapper.toPaymentTypeBO(PaymentTypeTO.PERIODIC)).isEqualTo(PaymentTypeBO.PERIODIC);
        assertThat(mapper.toPaymentTypeBO(PaymentTypeTO.BULK)).isEqualTo(PaymentTypeBO.BULK);
    }

    @Test
    public void toPaymentTypeTO() {
        assertThat(PaymentTypeBO.values().length).isEqualTo(PaymentTypeTO.values().length);
        assertThat(mapper.toPaymentTypeTO(PaymentTypeBO.SINGLE)).isEqualTo(PaymentTypeTO.SINGLE);
        assertThat(mapper.toPaymentTypeTO(PaymentTypeBO.PERIODIC)).isEqualTo(PaymentTypeTO.PERIODIC);
        assertThat(mapper.toPaymentTypeTO(PaymentTypeBO.BULK)).isEqualTo(PaymentTypeTO.BULK);
    }

    @Test
    public void toPaymentProductBO() {
        assertThat(PaymentProductBO.values().length).isEqualTo(PaymentProductTO.values().length);
        assertThat(mapper.toPaymentProductBO(PaymentProductTO.SEPA)).isEqualTo(PaymentProductBO.SEPA);
        assertThat(mapper.toPaymentProductBO(PaymentProductTO.CROSS_BORDER)).isEqualTo(PaymentProductBO.CROSS_BORDER);
        assertThat(mapper.toPaymentProductBO(PaymentProductTO.INSTANT_SEPA)).isEqualTo(PaymentProductBO.INSTANT_SEPA);
        assertThat(mapper.toPaymentProductBO(PaymentProductTO.TARGET2)).isEqualTo(PaymentProductBO.TARGET2);
    }

    @Test
    public void toPaymentProductTO() {
        assertThat(PaymentProductTO.values().length).isEqualTo(PaymentProductBO.values().length);
        assertThat(mapper.toPaymentProductTO(PaymentProductBO.SEPA)).isEqualTo(PaymentProductTO.SEPA);
        assertThat(mapper.toPaymentProductTO(PaymentProductBO.CROSS_BORDER)).isEqualTo(PaymentProductTO.CROSS_BORDER);
        assertThat(mapper.toPaymentProductTO(PaymentProductBO.INSTANT_SEPA)).isEqualTo(PaymentProductTO.INSTANT_SEPA);
        assertThat(mapper.toPaymentProductTO(PaymentProductBO.TARGET2)).isEqualTo(PaymentProductTO.TARGET2);
    }

    @Test
    public void toPaymentTO() {
        assertThat(mapper.toPaymentTO(getPayment(PaymentBO.class,PATH_SINGLE_BO))).isExactlyInstanceOf(SinglePaymentTO.class);
        assertThat(mapper.toPaymentTO(getPayment(PaymentBO.class,PATH_PERIODIC_BO))).isExactlyInstanceOf(PeriodicPaymentTO.class);
        assertThat(mapper.toPaymentTO(getPayment(PaymentBO.class,PATH_BULK_BO))).isExactlyInstanceOf(BulkPaymentTO.class);
    }

    @Test
    public void toSingleBulkPartTO() {
        PaymentBO payment = getPayment(PaymentBO.class, PATH_BULK_BO);
        SinglePaymentTO expectedResult = getPayment(BulkPaymentTO.class, PATH_BULK_TO).getPayments().get(0);
        SinglePaymentTO result = mapper.toSingleBulkPartTO(payment, payment.getTargets().get(0));
        assertThat(result).isEqualToComparingFieldByFieldRecursively(expectedResult);
    }

    private static <T> T getPayment(Class<T> aClass, String path) {
        try {
            return YamlReader.getInstance().getObjectFromFile(path, aClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }
}