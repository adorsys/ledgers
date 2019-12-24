package de.adorsys.ledgers.deposit.db.repository;

import de.adorsys.ledgers.deposit.db.domain.*;
import de.adorsys.ledgers.deposit.db.test.DepositAccountRepositoryApplication;
import de.adorsys.ledgers.util.Ids;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DepositAccountRepositoryApplication.class)
public class PaymentRepositoryIT {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    public void test() {
        Payment payment = new Payment();
        payment.setPaymentId(Ids.id());

        payment.setDebtorAccount(newAccount("DE89370400440532013000", "EUR"));
        payment.setAccountId("accountId");
        payment.setPaymentType(PaymentType.SINGLE);

        payment.setTransactionStatus(TransactionStatus.RCVD);

        PaymentTarget t = new PaymentTarget();
        t.setPaymentId(Ids.id());
        t.setInstructedAmount(newAmount(BigDecimal.valueOf(2000), "EUR"));
        t.setCreditorAccount(newAccount("DE45370400440332013001", "EUR"));
        t.setPayment(payment);
        payment.getTargets().add(t);

        paymentRepository.save(payment);
    }

    private Amount newAmount(BigDecimal amount, String currency) {
        Amount amt = new Amount();
        amt.setAmount(amount);
        amt.setCurrency(currency);
        return amt;
    }

    private AccountReference newAccount(String iban, String currency) {
        AccountReference acc = new AccountReference();
        acc.setIban(iban);
        acc.setCurrency(currency);
        return acc;
    }

}
