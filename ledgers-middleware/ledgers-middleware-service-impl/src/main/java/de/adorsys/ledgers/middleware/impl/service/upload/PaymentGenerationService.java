/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.upload;

import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.general.AddressTO;
import de.adorsys.ledgers.middleware.api.domain.payment.*;
import de.adorsys.ledgers.util.random.RandomUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO.BULK;
import static de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO.SINGLE;

@Service
@RequiredArgsConstructor
public class PaymentGenerationService {
    private static final String TEST_CREDITOR_IBAN = "DE68370400440000000000";
    private static final Random random = new SecureRandom();

    public Map<PaymentTypeTO, PaymentTO> generatePayments(AccountBalanceTO balance, String branch) {
        EnumMap<PaymentTypeTO, PaymentTO> map = new EnumMap<>(PaymentTypeTO.class);
        map.put(SINGLE, generatePaymentTO(balance, branch, false));
        map.put(BULK, generatePaymentTO(balance, branch, true));
        return map;
    }

    private PaymentTO generatePaymentTO(AccountBalanceTO balance, String branch, boolean isBulk) {
        PaymentTO payment = new PaymentTO();
        payment.setPaymentType(isBulk ? BULK : SINGLE);
        payment.setDebtorAccount(generateReference(balance.getIban(), balance.getAmount().getCurrency()));
        payment.setTransactionStatus(TransactionStatusTO.RCVD);
        payment.setPaymentProduct("instant-sepa-credit-transfers");
        payment.setRequestedExecutionDate(LocalDate.now());

        List<PaymentTargetTO> targets = new ArrayList<>();
        targets.add(generateTarget(balance, branch));
        if (isBulk) {
            targets.add(generateTarget(balance, branch));
        }
        payment.setTargets(targets);

        return payment;
    }

    private PaymentTargetTO generateTarget(AccountBalanceTO balance, String branch) {
        PaymentTargetTO target = new PaymentTargetTO();
        String endToEndId = generateEndToEndId(branch);
        target.setEndToEndIdentification(endToEndId);
        target.setInstructedAmount(generateAmount(balance));
        target.setCreditorAccount(generateReference(TEST_CREDITOR_IBAN, balance.getAmount().getCurrency()));
        target.setCreditorAgent("adorsys GmbH & CO KG");
        target.setCreditorName("adorsys GmbH & CO KG");
        target.setCreditorAddress(getTestCreditorAddress());
        return target;
    }

    private AmountTO generateAmount(AccountBalanceTO balance) {
        AmountTO amount = new AmountTO();
        amount.setCurrency(balance.getAmount().getCurrency());
        int balanceAmount = balance.getAmount().getAmount().intValue();
        int maxAmount = balanceAmount * 100 / 3;
        int rand = random.nextInt(maxAmount - 1) + 1;
        amount.setAmount(BigDecimal.valueOf(rand / 100d));
        return amount;
    }

    private AddressTO getTestCreditorAddress() {
        return new AddressTO("Fürther Str.", "246a", "Nürnberg", "90429", "Germany", null, null);
    }

    private AccountReferenceTO generateReference(String iban, Currency currency) {
        AccountReferenceTO reference = new AccountReferenceTO();
        reference.setIban(iban);
        reference.setCurrency(currency);
        return reference;
    }

    private String generateEndToEndId(String branchId) {
        return String.join("_", branchId,
                           String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) + random.nextInt(9),
                           String.valueOf(RandomUtils.threadRandomLong(10000, 99999)));
    }
}
