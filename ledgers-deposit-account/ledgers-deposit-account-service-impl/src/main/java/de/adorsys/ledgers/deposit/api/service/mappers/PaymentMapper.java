package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentTarget;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.util.Ids;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "nextScheduledExecution", ignore = true)
    @Mapping(target = "executedDate", ignore = true)
    Payment toPayment(PaymentBO payment);

    PaymentBO toPaymentBO(Payment payment);

    TransactionStatus toTransactionStatus(TransactionStatusBO status);

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
    @Mapping(source = "rate", target = "exchangeRate")
    PaymentTargetDetailsBO toPaymentTargetDetails(String id, PaymentTargetBO paymentTarget, LocalDate postingTime, List<ExchangeRateBO> rate);

    @Mapping(source = "amount", target = "transactionAmount")
    @Mapping(source = "postingTime", target = "valueDate")
    @Mapping(source = "postingTime", target = "bookingDate")
    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "payment.paymentId", target = "paymentOrderId")
    @Mapping(constant = "multiple", target = "creditorAgent")
    @Mapping(constant = "multiple", target = "creditorName")
    @Mapping(source = "rate", target = "exchangeRate")
    PaymentTargetDetailsBO toPaymentTargetDetailsBatch(String id, PaymentBO payment, AmountBO amount, LocalDate postingTime, List<ExchangeRateBO> rate);

    @Mapping(target = "transactionId", source = "postingLineId")
    @Mapping(target = "endToEndId", source = "postingLineId")
    @Mapping(target = "bookingDate", source = "postingDate")
    @Mapping(target = "valueDate", source = "postingDate")
    @Mapping(target = "transactionAmount", source = "amount")
    TransactionDetailsBO toDepositTransactionDetails(AmountBO amount, AccountReferenceBO creditorAccount, LocalDate postingDate, String postingLineId);

    @SuppressWarnings("PMD.ShortMethodName")
    default String id() {
        return Ids.id();
    }
}
