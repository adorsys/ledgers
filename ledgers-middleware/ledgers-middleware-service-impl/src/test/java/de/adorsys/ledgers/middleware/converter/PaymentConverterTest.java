package de.adorsys.ledgers.middleware.converter;

import de.adorsys.ledgers.deposit.api.domain.PaymentResultBO;
import de.adorsys.ledgers.deposit.api.domain.ResultStatusBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentResultTO;
import de.adorsys.ledgers.middleware.service.domain.payment.ResultStatusTO;
import de.adorsys.ledgers.middleware.service.domain.payment.TransactionStatusTO;
import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PaymentConverterTest {

    private PaymentConverter mapper;

    @Before
    public void setUp() {
        mapper = Mappers.getMapper(PaymentConverter.class);
    }

    @Test
    public void toPaymentResultTO() {
        PaymentResultBO bo = YamlReader.getInstance().getObjectFromFile("de/adorsys/ledgers/middleware/converter/payment-result.yml", PaymentResultBO.class);

        PaymentResultTO to = mapper.toPaymentResultTO(bo);

        assertThat(to.getResponseStatus(), is(ResultStatusTO.SUCCESS));
        assertThat(to.getPaymentResult(), is(TransactionStatusTO.RCVD.name()));
        assertThat(to.getMessages().size(), is(2));
        assertThat(to.getMessages().get(0), is("message1"));
        assertThat(to.getMessages().get(1), is("message2"));
    }

    @Test
    public void toPaymentResultBO() {
        PaymentResultTO to = YamlReader.getInstance().getObjectFromFile("de/adorsys/ledgers/middleware/converter/payment-result.yml", PaymentResultTO.class);

        PaymentResultBO bo = mapper.toPaymentResultBO(to);

        assertThat(bo.getResponseStatus(), is(ResultStatusBO.SUCCESS));
        assertThat(bo.getPaymentResult(), is(TransactionStatusBO.RCVD.name()));
        assertThat(bo.getMessages().size(), is(2));
        assertThat(bo.getMessages().get(0), is("message1"));
        assertThat(bo.getMessages().get(1), is("message2"));
    }
}