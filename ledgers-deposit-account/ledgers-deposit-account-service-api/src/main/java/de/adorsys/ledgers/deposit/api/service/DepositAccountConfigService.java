package de.adorsys.ledgers.deposit.api.service;

public interface DepositAccountConfigService {

    String getDepositParentAccount();

    String getLedger();

    String getClearingAccount(String paymentProduct);

    String getCashAccount();
}
