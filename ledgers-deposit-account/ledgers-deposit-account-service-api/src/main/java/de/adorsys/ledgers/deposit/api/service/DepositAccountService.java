package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.TransactionNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

public interface DepositAccountService {

    DepositAccountBO createDepositAccount(DepositAccountBO depositAccount, String userName) throws DepositAccountNotFoundException;

    DepositAccountDetailsBO getDepositAccountByIban(String iban, LocalDateTime refTime, boolean withBalances) throws DepositAccountNotFoundException;

    List<DepositAccountDetailsBO> getDepositAccountsByIban(List<String> ibans, LocalDateTime refTime, boolean withBalances) throws DepositAccountNotFoundException;

    DepositAccountDetailsBO getDepositAccountById(String accountId, LocalDateTime refTime, boolean withBalances) throws DepositAccountNotFoundException;

    TransactionDetailsBO getTransactionById(String accountId, String transactionId) throws TransactionNotFoundException;

    List<TransactionDetailsBO> getTransactionsByDates(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo) throws DepositAccountNotFoundException;

    boolean confirmationOfFunds(FundsConfirmationRequestBO requestBO) throws DepositAccountNotFoundException;

    String readIbanById(String id);

    List<DepositAccountBO> findByAccountNumberPrefix(String accountNumberPrefix);

    void depositCash(String accountId, AmountBO amount, String user) throws DepositAccountNotFoundException;
}
