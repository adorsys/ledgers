/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractServiceImpl {
    protected final DepositAccountConfigService depositAccountConfigService;
    protected final LedgerService ledgerService;

    protected LedgerBO loadLedger() {
        String ledgerName = depositAccountConfigService.getLedger();
        return ledgerService.findLedgerByName(ledgerName)
                       .orElseThrow(() -> new IllegalStateException(String.format("Ledger with name %s not found", ledgerName)));
    }

    protected LedgerAccountBO loadClearingAccount(LedgerBO ledgerBO, String paymentProductBO) {
        String clearingAccount = depositAccountConfigService.getClearingAccount(paymentProductBO);
        return ledgerService.findLedgerAccount(ledgerBO, clearingAccount);
    }
}
