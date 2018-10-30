package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.service.impl.DepositAccountServiceImpl;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Currency;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentMapperTest {
    private static final PaymentTypeBO PAYMENT_TYPE_SINGLE = PaymentTypeBO.SINGLE;
    private static final PaymentTypeBO PAYMENT_TYPE_BULK = PaymentTypeBO.BULK;
    private static final Currency CURRENCY = Currency.getInstance("EUR");

    @InjectMocks
    private PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);
    @Mock
    private CurrencyMapper currencyMapper;

    @Test
    public void toPayment_Single() {
        when(currencyMapper.toCurrency(anyString())).thenReturn(CURRENCY);
        PaymentBO result = mapper.toPaymentBO(getPayment(Payment.class, PAYMENT_TYPE_SINGLE));
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(getPayment(PaymentBO.class, PAYMENT_TYPE_SINGLE));
    }

    @Test
    public void toPayment_Bulk() {
        when(currencyMapper.toCurrency(anyString())).thenReturn(CURRENCY);
        PaymentBO result = mapper.toPaymentBO(getPayment(Payment.class, PAYMENT_TYPE_BULK));
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(getPayment(PaymentBO.class, PAYMENT_TYPE_BULK));
    }

    @Test
    public void toPaymentBO_Single() {
        when(currencyMapper.currencyToString(any())).thenReturn(CURRENCY.getCurrencyCode());
        Payment result = mapper.toPayment(getPayment(PaymentBO.class, PAYMENT_TYPE_SINGLE));
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(getPayment(Payment.class, PAYMENT_TYPE_SINGLE));
    }

    @Test
    public void toPaymentBO_Bulk() {
        when(currencyMapper.currencyToString(any())).thenReturn(CURRENCY.getCurrencyCode());
        Payment result = mapper.toPayment(getPayment(PaymentBO.class, PAYMENT_TYPE_BULK));
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(getPayment(Payment.class, PAYMENT_TYPE_BULK));
    }

    private <T> T getPayment(Class<T> t, PaymentTypeBO paymentType) {
        String path = paymentType == PAYMENT_TYPE_SINGLE
                              ? "PaymentSingle.yml"
                              : "PaymentBulk.yml";
        try {
            return YamlReader.getInstance().getObjectFromResource(DepositAccountServiceImpl.class, path, t);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }
}