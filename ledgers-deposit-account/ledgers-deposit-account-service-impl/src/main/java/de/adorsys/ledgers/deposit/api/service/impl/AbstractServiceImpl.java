package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.PaymentProductBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.LedgerService;

public abstract class AbstractServiceImpl {
    protected DepositAccountConfigService depositAccountConfigService;
    protected LedgerService ledgerService;

    public AbstractServiceImpl(DepositAccountConfigService depositAccountConfigService, LedgerService ledgerService) {
        super();
        this.depositAccountConfigService = depositAccountConfigService;
        this.ledgerService = ledgerService;
    }

    protected LedgerBO loadLedger() {
        String ledgerName = depositAccountConfigService.getLedger();
        return ledgerService.findLedgerByName(ledgerName).orElseThrow(() -> new IllegalStateException(String.format("Ledger with name %s not found", ledgerName)));
    }
    
    protected LedgerAccountBO loadClearingAccount(LedgerBO ledgerBO, PaymentProductBO paymentProductBO) {
    	return loadClearing(ledgerBO, depositAccountConfigService.getClearingAccount(paymentProductBO));
    }

    protected LedgerAccountBO loadClearing(LedgerBO ledgerBO, String accountName) {
        try {
            return ledgerService.findLedgerAccount(ledgerBO, accountName);
        } catch (LedgerNotFoundException | LedgerAccountNotFoundException e) {
            throw new IllegalStateException(String.format("LedgerAccount with name %s not found from ledger with name %s", accountName, depositAccountConfigService.getLedger()));
        }
    }
}
