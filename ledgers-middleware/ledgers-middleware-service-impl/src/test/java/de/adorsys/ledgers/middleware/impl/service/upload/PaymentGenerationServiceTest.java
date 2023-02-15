/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.upload;

import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.account.BalanceTypeTO;
import de.adorsys.ledgers.middleware.api.domain.general.AddressTO;
import de.adorsys.ledgers.middleware.api.domain.payment.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO.BULK;
import static de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO.SINGLE;
import static org.assertj.core.api.Java6Assertions.assertThat;


class PaymentGenerationServiceTest {
    private static final String TEST_CREDITOR_IBAN = "DE68370400440000000000";
    private static final String IBAN = "DE123456789";
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void generatePayments() {
        // Given
        Map<PaymentTypeTO, PaymentTO> result = new PaymentGenerationService().generatePayments(getAccountBalance(), "BRANCH_1");
        assertThat(result.values().size() == 2).isTrue();
        checkPayment(result, SINGLE);
        checkPayment(result, BULK);
        BigDecimal total = result.values().stream()
                                   .map(PaymentTO::getTargets)
                                   .map(this::sumUp)
                                   .reduce(BigDecimal::add)
                                   .orElse(BigDecimal.ZERO);

        // Then
        assertThat(total.compareTo(getAccountBalance().getAmount().getAmount()) < 1).isTrue();
    }

    private BigDecimal sumUp(List<PaymentTargetTO> t) {
        return t.stream()
                       .map(PaymentTargetTO::getInstructedAmount)
                       .map(AmountTO::getAmount).reduce(BigDecimal::add)
                       .orElse(BigDecimal.ZERO);
    }

    private void checkPayment(Map<PaymentTypeTO, PaymentTO> result, PaymentTypeTO paymentType) {
        PaymentTO payment = result.get(paymentType);
        assertThat(payment).isEqualToIgnoringGivenFields(getExpected(paymentType), "targets");
        assertThat(payment.getTargets().size()).isEqualTo(paymentType == SINGLE ? 1 : 2);
        payment.getTargets()
                .forEach(t -> {
                    assertThat(t).isEqualToIgnoringGivenFields(getExpected(paymentType).getTargets().iterator().next(), "endToEndIdentification", "instructedAmount");
                    assertThat(t.getInstructedAmount().getCurrency()).isEqualTo(EUR);
                    assertThat(t.getInstructedAmount().getAmount().compareTo(BigDecimal.TEN) < 1).isTrue();
                    assertThat(t.getEndToEndIdentification()).isNotEmpty();
                });
    }

    private PaymentTO getExpected(PaymentTypeTO paymentType) {
        PaymentTO payment = new PaymentTO();
        payment.setDebtorAccount(new AccountReferenceTO(IBAN, null, null, null, null, EUR));
        payment.setPaymentType(paymentType);
        payment.setPaymentProduct("instant-sepa-credit-transfers");
        payment.setTransactionStatus(TransactionStatusTO.RCVD);
        payment.setRequestedExecutionDate(LocalDate.now());
        payment.setTargets(paymentType == SINGLE ? Collections.singletonList(getTarget()) : Arrays.asList(getTarget(), getTarget()));

        return payment;
    }

    private PaymentTargetTO getTarget() {
        PaymentTargetTO target = new PaymentTargetTO();
        target.setCreditorAccount(new AccountReferenceTO(TEST_CREDITOR_IBAN, null, null, null, null, EUR));
        target.setCreditorAgent("adorsys GmbH & CO KG");
        target.setCreditorName("adorsys GmbH & CO KG");
        target.setCreditorAddress(getTestCreditorAddress());
        target.setEndToEndIdentification("new end to end id");
        target.setInstructedAmount(new AmountTO(EUR, BigDecimal.ZERO));
        return target;
    }

    private AddressTO getTestCreditorAddress() {
        return new AddressTO("Fürther Str.", "246a", "Nürnberg", "90429", "Germany", null, null);
    }

    private AccountBalanceTO getAccountBalance() {
        return new AccountBalanceTO(new AmountTO(EUR, BigDecimal.TEN), BalanceTypeTO.INTERIM_AVAILABLE, null, null, null, IBAN);
    }
}