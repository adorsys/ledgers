package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.PaymentProductBO;

public interface DepositAccountConfigService {

	String getDepositParentAccount();

	String getLedger();

	String getClearingAccount(PaymentProductBO paymentProduct);

	String getCashAccount();
}
