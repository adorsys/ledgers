package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;

public interface DepositAccountService {

    DepositAccountBO createDepositAccount(DepositAccountBO depositAccount) throws DepositAccountNotFoundException;

    DepositAccountBO getDepositAccountById(String accountId) throws DepositAccountNotFoundException;

    DepositAccountBO getDepositAccountByIBAN(String iban) throws DepositAccountNotFoundException;
}
