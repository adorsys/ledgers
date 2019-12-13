package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import de.adorsys.ledgers.deposit.api.service.domain.LedgerAccountModel;
import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.api.service.ChartOfAccountService;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositAccountInitServiceImpl implements DepositAccountInitService {
    private static final String SYSTEM = "System";
    private final ChartOfAccountService coaService;
    private final LedgerService ledgerService;
    private final ASPSPConfigSource configSource;

    @Override
    public void initConfigData() {
        log.info("Initialising Ledgers");
        LedgerBO ledger = getOrCreateLedger();
        checkAndUpdateChartOfAccounts(ledger);
    }

    private void checkAndUpdateChartOfAccounts(LedgerBO ledger) {
        log.info("Check Ledger Accounts needs update");
        configSource.chartOfAccount(configSource.aspspConfigData().getCoaFile())
                .forEach(a -> checkLedgerAccountExistsOrCreate(ledger, a));
        configSource.aspspConfigData().getCoaExtensions()
                .forEach(a -> checkLedgerAccountExistsOrCreate(ledger, a));
    }

    private LedgerBO getOrCreateLedger() {
        String ledgerName = configSource.aspspConfigData().getLedger();
        ChartOfAccountBO coa = getOrCreateCoa(ledgerName);
        log.info("Checking Ledger needs update");
        return ledgerService.findLedgerByName(ledgerName)
                       .orElseGet(() -> {
                           log.info("Ledger created");
                           return ledgerService.newLedger(new LedgerBO(ledgerName, coa));
                       });
    }

    private ChartOfAccountBO getOrCreateCoa(final String ledgerName) {
        log.info("Checking Coa needs update");
        return coaService.findChartOfAccountsByName(ledgerName)
                       .orElseGet(() -> {
                           log.info("Coa created");
                           return coaService.newChartOfAccount(new ChartOfAccountBO(ledgerName));
                       });
    }

    private void checkLedgerAccountExistsOrCreate(LedgerBO ledger, LedgerAccountModel model) {
        try {
            ledgerService.findLedgerAccount(ledger, model.getName());
            log.info("Ledger Account for {} exists, no update required", model.getName());
        } catch (PostingModuleException ex) {
            // NoOp. Create ledger account below.
            log.info("Creating new Ledger Account for {}", model.getName());
            LedgerAccountBO parent = getParentLedgerAccount(ledger, model);
            LedgerAccountBO la = newLedgerAccountObj(ledger, model, parent);
            ledgerService.newLedgerAccount(la, SYSTEM);
        }
    }

    private LedgerAccountBO getParentLedgerAccount(LedgerBO ledger, LedgerAccountModel model) {
        return (model.getParent() != null)
                       ? ledgerService.findLedgerAccount(ledger, model.getParent())
                       : null;
    }

    private LedgerAccountBO newLedgerAccountObj(LedgerBO ledger, LedgerAccountModel model, LedgerAccountBO parent) {
        String shortDesc = model.getShortDesc();
        String name = model.getName();
        BalanceSideBO balanceSide = model.getBalanceSide() != null
                                            ? model.getBalanceSide()
                                            : parent.getBalanceSide();
        AccountCategoryBO category = model.getCategory() != null
                                             ? model.getCategory()
                                             : parent.getCategory();

        return new LedgerAccountBO(name, Ids.id(), LocalDateTime.now(), null, shortDesc, null, ledger, parent, ledger.getCoa(), balanceSide, category);
    }
}
