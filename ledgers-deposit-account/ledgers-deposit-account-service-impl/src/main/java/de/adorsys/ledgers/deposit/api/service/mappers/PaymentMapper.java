package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentTarget;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
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

    @Mapping(target = "transactionStatus", source = "paymentTarget.payment.transactionStatus")
    @Mapping(target = "remittanceInformationUnstructured", source = "paymentTarget.remittanceInformationUnstructured")
    @Mapping(target = "remittanceInformationStructured", source = "paymentTarget.remittanceInformationStructured")
    @Mapping(target = "debtorName", source = "paymentTarget.payment.debtorName")
    @Mapping(target = "debtorAgent", source = "paymentTarget.payment.debtorAgent")
    @Mapping(target = "creditorName", source = "paymentTarget.creditorName")
    @Mapping(target = "creditorAgent", source = "paymentTarget.creditorAgent")
    @Mapping(target = "creditorAddress", source = "paymentTarget.creditorAddress")
    @Mapping(target = "creditorAccount", source = "paymentTarget.creditorAccount")
    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "paymentTarget.endToEndIdentification", target = "endToEndId")
    @Mapping(source = "postingTime", target = "bookingDate")
    @Mapping(source = "postingTime", target = "valueDate")
    @Mapping(source = "paymentTarget.instructedAmount", target = "transactionAmount")
    @Mapping(source = "paymentTarget.payment.debtorAccount", target = "debtorAccount")
    @Mapping(source = "paymentTarget.payment.paymentId", target = "paymentOrderId")
    @Mapping(source = "paymentTarget.payment.paymentType", target = "paymentType")
    @Mapping(source = "paymentTarget.payment.paymentProduct", target = "paymentProduct")
    @Mapping(source = "rate", target = "exchangeRate")
    @Mapping(target = "bankTransactionCode", expression = "java(de.adorsys.ledgers.deposit.api.domain.BankTransactionCode.getByPaymentProduct(paymentTarget.getPayment().getPaymentProduct()))")
    @Mapping(target = "proprietaryBankTransactionCode", expression = "java(de.adorsys.ledgers.deposit.api.domain.BankTransactionCode.getByPaymentProduct(paymentTarget.getPayment().getPaymentProduct()))")
    PaymentTargetDetailsBO toPaymentTargetDetails(String id, PaymentTargetBO paymentTarget, LocalDate postingTime, List<ExchangeRateBO> rate, BalanceBO balanceAfterTransaction);

    @Mapping(target = "remittanceInformationUnstructured", constant = "Batch booking, no remittance information available")
    @Mapping(target = "transactionStatus", source = "payment.transactionStatus")
    @Mapping(target = "debtorName", source = "payment.debtorName")
    @Mapping(target = "debtorAgent", source = "payment.debtorAgent")
    @Mapping(target = "debtorAccount", source = "payment.debtorAccount")
    @Mapping(target = "paymentType", source = "payment.paymentType")
    @Mapping(target = "endToEndId", expression = "java(payment.getTargets().stream().map(PaymentTargetBO::getEndToEndIdentification).reduce(\"\", (accum, s) -> accum + \", \" + s).replaceFirst(\", \",\"\" ))")
    @Mapping(target = "proprietaryBankTransactionCode", expression = "java(de.adorsys.ledgers.deposit.api.domain.BankTransactionCode.getByPaymentProduct(payment.getPaymentProduct()))")
    @Mapping(target = "bankTransactionCode", expression = "java(de.adorsys.ledgers.deposit.api.domain.BankTransactionCode.getByPaymentProduct(payment.getPaymentProduct()))")
    @Mapping(source = "amount", target = "transactionAmount")
    @Mapping(source = "postingTime", target = "valueDate")
    @Mapping(source = "postingTime", target = "bookingDate")
    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "payment.paymentId", target = "paymentOrderId")
    @Mapping(source = "payment.paymentProduct", target = "paymentProduct")
    @Mapping(constant = "multiple", target = "creditorAgent")
    @Mapping(constant = "multiple", target = "creditorName")
    @Mapping(source = "rate", target = "exchangeRate")
    PaymentTargetDetailsBO toPaymentTargetDetailsBatch(String id, PaymentBO payment, AmountBO amount, LocalDate postingTime, List<ExchangeRateBO> rate, BalanceBO balanceAfterTransaction);

    @Mapping(target = "remittanceInformationUnstructured", constant = "Cash deposit through Bank ATM")
    @Mapping(target = "debtorName", source = "depositAccount.name")
    @Mapping(target = "debtorAccount", source = "depositAccount.reference")
    @Mapping(target = "creditorName", source = "depositAccount.name")
    @Mapping(target = "proprietaryBankTransactionCode", constant = "PMNT-MCOP-OTHR")
    @Mapping(target = "bankTransactionCode", constant = "PMNT-MCOP-OTHR")
    @Mapping(target = "transactionId", source = "postingLineId")
    @Mapping(target = "endToEndId", source = "postingLineId")
    @Mapping(target = "bookingDate", source = "postingDate")
    @Mapping(target = "valueDate", source = "postingDate")
    @Mapping(target = "transactionAmount", source = "amount")
    TransactionDetailsBO toDepositTransactionDetails(AmountBO amount, DepositAccountBO depositAccount, AccountReferenceBO creditorAccount, LocalDate postingDate, String postingLineId, BalanceBO balanceAfterTransaction);

    List<PaymentBO> toPaymentBOList(List<Payment> payments);
}
