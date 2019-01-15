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

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentProductBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentResultBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTargetBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.BulkPaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentResultTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PeriodicPaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
@SuppressWarnings("PMD")
@Mapper(componentModel = "spring")
public abstract class PaymentConverter {

    @Autowired
    private ObjectMapper mapper;

    public PaymentConverter() {
    }

    public abstract PaymentResultTO toPaymentResultTO(PaymentResultBO bo);

    public abstract PaymentResultBO toPaymentResultBO(PaymentResultTO to);

    public abstract PaymentTypeBO toPaymentTypeBO(PaymentTypeTO paymentType);

    public abstract PaymentTypeTO toPaymentTypeTO(PaymentTypeBO paymentType);

    public abstract PaymentProductBO toPaymentProductBO(PaymentProductTO paymentProduct);

    public abstract PaymentProductTO toPaymentProductTO(PaymentProductBO paymentProduct);

    public Object toPaymentTO(PaymentBO payment) { //TODO Consider refactoring
        if (payment.getPaymentType() == PaymentTypeBO.SINGLE) {
            return toSinglePaymentTO(payment, payment.getTargets().get(0));
        } else if (payment.getPaymentType() == PaymentTypeBO.PERIODIC) {
            return toPeriodicPaymentTO(payment, payment.getTargets().get(0));
        } else {
            return toBulkPaymentTO(payment, payment.getTargets().get(0));
        }
    }
    
    @Mappings({
    @Mapping(source = "payment.paymentId", target = "paymentId"),
    @Mapping(source = "payment.transactionStatus", target = "paymentStatus")
    })
    public abstract SinglePaymentTO toSinglePaymentTO(PaymentBO payment, PaymentTargetBO paymentTarget);

    @Mappings({
    @Mapping(source = "payment.paymentId", target = "paymentId"),
    @Mapping(source = "payment.transactionStatus", target = "paymentStatus")
    })
    public abstract PeriodicPaymentTO toPeriodicPaymentTO(PaymentBO payment, PaymentTargetBO paymentTarget);

    @Mappings({
    @Mapping(source = "payment.paymentId", target = "paymentId"),
    @Mapping(source = "payment.transactionStatus", target = "paymentStatus"),
    @Mapping(target = "paymentProduct", expression = "java(toPaymentProductTO(paymentTarget.getPaymentProduct()))"),
    @Mapping(target = "payments", expression = "java(payment.getTargets().stream().map(t -> toSingleBulkPartTO(payment, t)).collect(java.util.stream.Collectors.toList()))")
    })
    public abstract BulkPaymentTO toBulkPaymentTO(PaymentBO payment, PaymentTargetBO paymentTarget);

    @Mappings({
    @Mapping(source = "paymentTarget.paymentId", target = "paymentId"),
    @Mapping(source = "payment.transactionStatus", target = "paymentStatus")
    })
    public abstract SinglePaymentTO toSingleBulkPartTO(PaymentBO payment, PaymentTargetBO paymentTarget);

//    @SuppressWarnings("unchecked")
	public <T> PaymentBO toPaymentBO(Object payment, Class<T> tClass) {
    	Object o = payment;
    	if(o instanceof Map) {
	    	Map<String, ?> map = (Map<String, ?>) o;
	    	if(map.size()==1) {
	    		map = (Map<String, ?>) map.values().iterator().next();
	    	}
	    	o = map;
    	}
    	
        if (tClass.equals(SinglePaymentTO.class)) {
            return toPaymentBO((SinglePaymentTO) mapper.convertValue(o, tClass));
        } else if (tClass.equals(PeriodicPaymentTO.class)) {
            return toPaymentBO((PeriodicPaymentTO) mapper.convertValue(o, tClass));
        } else if (tClass.equals(BulkPaymentTO.class)) {
        	return toPaymentBO((BulkPaymentTO) mapper.convertValue(o, tClass));
        } else {
        	throw new IllegalStateException(String.format("Unknown payment type %s", tClass.getName()));
        }
    }

    @Mappings({
    @Mapping(target = "paymentType", expression = "java(PaymentTypeBO.SINGLE)"),
    @Mapping(source = "paymentStatus", target = "transactionStatus"),
    @Mapping(target = "targets", expression = "java(java.util.Collections.singletonList(toPaymentTarget(payment)))")
    })
    public abstract PaymentBO toPaymentBO(SinglePaymentTO payment);

    @Mappings({
    @Mapping(target = "paymentType", expression = "java(PaymentTypeBO.PERIODIC)"),
    @Mapping(source = "paymentStatus", target = "transactionStatus"),
    @Mapping(target = "targets", expression = "java(java.util.Collections.singletonList(toPaymentTarget(payment)))")
    })
    public abstract PaymentBO toPaymentBO(PeriodicPaymentTO payment);

    @Mappings({
    @Mapping(target = "paymentType", expression = "java(PaymentTypeBO.BULK)"),
    @Mapping(target = "requestedExecutionTime", expression = "java(java.util.Optional.ofNullable(payment.getPayments()).map(l -> l.get(0).getRequestedExecutionTime()).orElse(null))"),
    @Mapping(source = "paymentStatus", target = "transactionStatus"),
    @Mapping(target = "targets", source = "payment.payments")
    })
    public abstract PaymentBO toPaymentBO(BulkPaymentTO payment);

    public abstract PaymentTargetBO toPaymentTarget(SinglePaymentTO payment);

    public abstract PaymentTargetBO toPaymentTarget(PeriodicPaymentTO payment);

    public abstract List<PaymentTargetBO> toPaymentTarget(List<SinglePaymentTO> payment);

    public abstract List<TransactionTO> toTransactionTOList(List<TransactionDetailsBO> transactions);

    @Mapping(source = "transactionAmount", target = "amount")
    public abstract TransactionTO toTransactionTO(TransactionDetailsBO transaction);

    @Mapping(target = "transactionAmount", source = "amount")
    public abstract TransactionDetailsBO toTransactionDetailsBO(TransactionTO transaction);
}
