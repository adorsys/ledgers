package de.adorsys.ledgers.deposit.api.service;

public interface DepositAccountConfigService {

	String getDepositParentAccount();

	String getLedger();

	String getClearingAccountTarget2();
	
	String getClearingAccountSepa();
}
