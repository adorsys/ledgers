package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;

public interface DepositAccountConfigService {

	LedgerAccountBO getDepositParentAccount();

	LedgerBO getLedger();

	LedgerAccountBO getClearingAccount();

}
