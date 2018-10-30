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

package de.adorsys.ledgers.middleware.converter;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.middleware.service.domain.payment.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentConverter {

    PaymentResultTO toPaymentResultTO(PaymentResultBO bo);

    PaymentResultBO toPaymentResultBO(PaymentResultTO to);

    PaymentTypeBO toPaymentTypeBO(PaymentTypeTO paymentType);

    PaymentTypeTO toPaymentTypeTO(PaymentTypeBO paymentType);

    PaymentProductBO toPaymentProductBO(PaymentProductTO paymentProduct);

    PaymentProductTO toPaymentProductTO(PaymentProductBO paymentProduct);

    default Object toPaymentTO(PaymentBO payment) {
        if (payment.getPaymentType() == PaymentTypeBO.SINGLE) {
            return toSinglePaymentTO(payment, payment.getTargets().get(0));
        } else if (payment.getPaymentType() == PaymentTypeBO.PERIODIC) {
            return toPeriodicPaymentTO(payment, payment.getTargets().get(0));
        } else {
            return toBulkPaymentTO(payment, payment.getTargets().get(0));
        }
    }

    @Mapping(source = "payment.paymentId", target = "paymentId")
    @Mapping(source = "payment.transactionStatus", target = "paymentStatus")
    SinglePaymentTO toSinglePaymentTO(PaymentBO payment, PaymentTargetBO paymentTarget);

    @Mapping(source = "payment.paymentId", target = "paymentId")
    @Mapping(source = "payment.transactionStatus", target = "paymentStatus")
    PeriodicPaymentTO toPeriodicPaymentTO(PaymentBO payment, PaymentTargetBO paymentTarget);

    @Mapping(source = "payment.paymentId", target = "paymentId")
    @Mapping(source = "payment.transactionStatus", target = "paymentStatus")
    @Mapping(target = "paymentProduct", expression = "java(toPaymentProductTO(paymentTarget.getPaymentProduct()))")
    @Mapping(target = "payments", expression = "java(payment.getTargets().stream().map(t -> toSingleBulkPartTO(payment, t)).collect(java.util.stream.Collectors.toList()))")
    BulkPaymentTO toBulkPaymentTO(PaymentBO payment, PaymentTargetBO paymentTarget);

    @Mapping(source = "paymentTarget.paymentId", target = "paymentId")
    @Mapping(source = "payment.transactionStatus", target = "paymentStatus")
    SinglePaymentTO toSingleBulkPartTO(PaymentBO payment, PaymentTargetBO paymentTarget);
}
