package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTargetBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentTarget;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CurrencyMapper.class)
public interface PaymentMapper {
    Payment toPayment(PaymentBO payment);

    PaymentBO toPaymentBO(Payment payment);

    @Mapping(ignore = true, target = "payment")
    PaymentTargetBO toPaymentTargetBO(PaymentTarget target);

    //TODO @fpo implement and test
    PostingBO toPosting(PaymentBO payment);

    //TODO @fpo implement and test
    TransactionDetailsBO toTransaction(PostingBO posting);
}
