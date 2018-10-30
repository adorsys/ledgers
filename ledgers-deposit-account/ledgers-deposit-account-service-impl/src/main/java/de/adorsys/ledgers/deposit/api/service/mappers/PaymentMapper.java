package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CurrencyMapper.class)
public interface PaymentMapper {
    Payment toPayment(PaymentBO payment);
    PaymentBO toPaymentBO(Payment payment);
}
