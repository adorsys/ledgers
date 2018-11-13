package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.BalanceBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.TransactionNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

public interface DepositAccountService {

    DepositAccountBO createDepositAccount(DepositAccountBO depositAccount) throws DepositAccountNotFoundException;

    DepositAccountBO getDepositAccountById(String accountId) throws DepositAccountNotFoundException;

    DepositAccountBO getDepositAccountByIBAN(String iban) throws DepositAccountNotFoundException;

    TransactionDetailsBO getTransactionById(String accountId, String transactionId) throws TransactionNotFoundException;

    List<BalanceBO> getBalances(String iban) throws LedgerAccountNotFoundException;

    List<DepositAccountBO> getDepositAccountsByIBAN(List<String> ibans);

    List<TransactionDetailsBO> getTransactionsByDates(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo) throws DepositAccountNotFoundException;
}
