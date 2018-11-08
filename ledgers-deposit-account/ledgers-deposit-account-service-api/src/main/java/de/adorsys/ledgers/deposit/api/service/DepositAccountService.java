package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;

import java.util.List;

public interface DepositAccountService {

    DepositAccountBO createDepositAccount(DepositAccountBO depositAccount) throws DepositAccountNotFoundException;

    DepositAccountBO getDepositAccountById(String accountId) throws DepositAccountNotFoundException;

    DepositAccountBO getDepositAccountByIBAN(String iban) throws DepositAccountNotFoundException;

    List<DepositAccountBO> getDepositAccountsByIBAN(List<String> ibans);
}
