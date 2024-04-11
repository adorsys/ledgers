/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.db.repository;

import de.adorsys.ledgers.deposit.db.domain.*;
import de.adorsys.ledgers.deposit.db.test.DepositAccountRepositoryApplication;
import de.adorsys.ledgers.util.Ids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DepositAccountRepositoryApplication.class)
class PaymentRepositoryIT {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void test() {
        Payment payment = new Payment();
        payment.setPaymentId(Ids.id());

        payment.setDebtorAccount(newAccount("DE89370400440532013000"));
        payment.setAccountId("accountId");
        payment.setPaymentType(PaymentType.SINGLE);

        payment.setTransactionStatus(TransactionStatus.RCVD);

        PaymentTarget t = new PaymentTarget();
        t.setPaymentId(Ids.id());
        t.setInstructedAmount(newAmount(BigDecimal.valueOf(2000)));
        t.setCreditorAccount(newAccount("DE45370400440332013001"));
        t.setPayment(payment);
        payment.getTargets().add(t);

        Payment savedPayment = paymentRepository.save(payment);
        assertNotNull(savedPayment);
    }

    private Amount newAmount(BigDecimal amount) {
        Amount amt = new Amount();
        amt.setAmount(amount);
        amt.setCurrency("EUR");
        return amt;
    }

    private AccountReference newAccount(String iban) {
        AccountReference acc = new AccountReference();
        acc.setIban(iban);
        acc.setCurrency("EUR");
        return acc;
    }

}
