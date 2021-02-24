/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.deposit.api.domain.ExchangeRateBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.middleware.api.domain.account.ExchangeRateTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("PMD")
@Mapper(componentModel = "spring")
public interface PaymentConverter {
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
