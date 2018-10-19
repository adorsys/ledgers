package de.adorsys.ledgers.deposit.service;

import de.adorsys.ledgers.postings.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.domain.LedgerBO;

public interface DepositAccountConfigService {

	LedgerAccountBO getDepositParentAccount();

	LedgerBO getLedger();

	LedgerAccountBO getClearingAccount();

}
