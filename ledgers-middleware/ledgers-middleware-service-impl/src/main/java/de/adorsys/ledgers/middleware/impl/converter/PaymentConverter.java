/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.deposit.api.domain.ExchangeRateBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTargetBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.middleware.api.domain.account.ExchangeRateTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTargetTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("PMD")
@Mapper(componentModel = "spring", uses = {RemittanceMapper.class})
public interface PaymentConverter {

    PaymentTargetBO toPaymentTargetBO(PaymentTargetTO paymentTargetTO);

    PaymentTargetTO toPaymentTargetTO(PaymentTargetBO paymentTargetBO);

    PaymentBO toPaymentBO(PaymentTO payment);

    List<TransactionTO> toTransactionTOList(List<TransactionDetailsBO> transactions);

    @Mapping(source = "transactionAmount", target = "amount")
    TransactionTO toTransactionTO(TransactionDetailsBO transaction);

    @Mapping(target = "currency", source = "currencyTo")
    ExchangeRateTO toExchangeRateTO(ExchangeRateBO exchangeRate);

    @Mapping(target = "transactionAmount", source = "amount")
    TransactionDetailsBO toTransactionDetailsBO(TransactionTO transaction);

    default List<PaymentTO> toPaymentTOList(List<PaymentBO> payments) {
        return payments.stream().map(this::toPaymentTO).collect(Collectors.toList());
    }

    PaymentTO toPaymentTO(PaymentBO payment);
}
