package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.BulkPaymentBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentResultBO;
import de.adorsys.ledgers.deposit.api.domain.SinglePaymentBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;

import java.util.List;

public interface DepositAccountService {

    DepositAccountBO createDepositAccount(DepositAccountBO depositAccount) throws DepositAccountNotFoundException;

    DepositAccountBO getDepositAccountById(String accountId) throws DepositAccountNotFoundException;

    DepositAccountBO getDepositAccountByIBAN(String iban) throws DepositAccountNotFoundException;

    PaymentResultBO executeSinglePaymentWithoutSca(SinglePaymentBO payment) throws PaymentProcessingException;

    PaymentResultBO executeBulkPaymentWithoutSca(BulkPaymentBO payment) throws PaymentProcessingException;

    PaymentResultBO executeSinglePaymentsWithoutSca(List<SinglePaymentBO> paymentBOList)
            throws PaymentProcessingException;
}
