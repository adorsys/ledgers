package de.adorsys.ledgers.deposit.api.service;

import java.time.LocalDateTime;
import java.util.List;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.TransactionNotFoundException;

public interface DepositAccountService {

    DepositAccountBO createDepositAccount(DepositAccountBO depositAccount) throws DepositAccountNotFoundException;

	DepositAccountDetailsBO getDepositAccountByIBAN(String iban, LocalDateTime refTime, boolean withBalances) throws DepositAccountNotFoundException;

	DepositAccountDetailsBO getDepositAccountById(String accountId, LocalDateTime refTime, boolean withBalances) throws DepositAccountNotFoundException;

    TransactionDetailsBO getTransactionById(String accountId, String transactionId) throws TransactionNotFoundException;

    List<DepositAccountBO> getDepositAccountsByIBAN(List<String> ibans);

    List<TransactionDetailsBO> getTransactionsByDates(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo) throws DepositAccountNotFoundException;

}
