package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentTarget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment toPayment(PaymentBO payment);

    PaymentBO toPaymentBO(Payment payment);

    @Mapping(ignore = true, target = "payment")
    PaymentTargetBO toPaymentTargetBO(PaymentTarget target);

    PaymentOrderDetailsBO toPaymentOrder(PaymentBO payment);

    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "paymentTarget.endToEndIdentification", target = "endToEndId")
    @Mapping(source = "postingTime", target = "bookingDate")
    @Mapping(source = "postingTime", target = "valueDate")
    @Mapping(source = "paymentTarget.instructedAmount", target = "transactionAmount")
    @Mapping(source = "paymentTarget.payment.debtorAccount", target = "debtorAccount")
    @Mapping(source = "paymentTarget.payment.paymentId", target = "paymentOrderId")
    @Mapping(source = "paymentTarget.payment.paymentType", target = "paymentType")
    PaymentTargetDetailsBO toPaymentTargetDetails(String id, PaymentTargetBO paymentTarget, LocalDate postingTime);

    @Mapping(source = "amount", target = "transactionAmount")
    @Mapping(source = "postingTime", target = "valueDate")
    @Mapping(source = "postingTime", target = "bookingDate")
    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "payment.paymentId", target = "paymentOrderId")
    @Mapping(constant = "multiple", target = "creditorAgent")
    @Mapping(constant = "multiple", target = "creditorName")
    PaymentTargetDetailsBO toPaymentTargetDetailsBatch(String id, PaymentBO payment, AmountBO amount, LocalDate postingTime);
}
