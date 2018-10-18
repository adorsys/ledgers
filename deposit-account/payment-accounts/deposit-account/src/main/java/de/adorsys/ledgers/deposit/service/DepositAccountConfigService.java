package de.adorsys.ledgers.deposit.service;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;

public interface DepositAccountConfigService {

	LedgerAccount getDepositParentAccount();

	Ledger getLedger();

	LedgerAccount getClearingAccount();

}
