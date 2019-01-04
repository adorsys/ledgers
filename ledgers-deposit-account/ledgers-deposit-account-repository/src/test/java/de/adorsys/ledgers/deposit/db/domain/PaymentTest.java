package de.adorsys.ledgers.deposit.db.domain;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.Assert.assertThat;

public class PaymentTest {

    @Test
    public void isInstant() {
        Stream.of(PaymentProduct.INSTANT_SEPA,PaymentProduct.TARGET2)
                .map(this::getPayment)
                .map(Payment::isInstant)
                .forEach(r->assertThat(r, Matchers.equalTo(true)));
    }

    @Test
    public void isInstant_false() {
        Stream.of(PaymentProduct.SEPA,PaymentProduct.CROSS_BORDER)
                .map(this::getPayment)
                .map(Payment::isInstant)
                .forEach(r->assertThat(r, Matchers.equalTo(false)));
    }

    private Payment getPayment(PaymentProduct product) {
        Payment payment = new Payment();
        PaymentTarget target = new PaymentTarget();
        target.setPaymentProduct(product);
        payment.setTargets(Collections.singletonList(target));
        return payment;
    }
}