package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigData;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import de.adorsys.ledgers.deposit.api.service.domain.LedgerAccountModel;
import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import de.adorsys.ledgers.postings.api.service.ChartOfAccountService;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.util.Ids;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.util.exception.PostingErrorCode.LEDGER_ACCOUNT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DepositAccountInitServiceImpl implements DepositAccountInitService {

    private static final String SYSTEM = "System";
    private final ChartOfAccountService coaService;
    private final LedgerService ledgerService;
    private final ASPSPConfigSource configSource;

    @Override
    public void initConfigData() {
        ASPSPConfigData aspspConfigData = configSource.aspspConfigData();

        // Only process if update is required.
        if (updateRequired(aspspConfigData)) {
            processASPSPConfigData(aspspConfigData);
        }
    }

    private void processASPSPConfigData(ASPSPConfigData aspspConfigData) {
        LedgerBO ledger = loadASPSPAccounts(aspspConfigData.getLedger(), aspspConfigData.getCoaFile());

        List<LedgerAccountModel> coaExtensions = aspspConfigData.getCoaExtensions();
        for (LedgerAccountModel ledgerAccountModel : coaExtensions) {
            newLedgerAccount(ledger, ledgerAccountModel);
        }
    }

    private LedgerBO loadASPSPAccounts(String ledgerName, String coaFile) {
        ChartOfAccountBO coa = getOrCreateCoa(ledgerName);
        LedgerBO ledger = getOrCreateLedger(ledgerName, coa);

        List<LedgerAccountModel> ledgerAccounts = configSource.chartOfAccount(coaFile);

        for (LedgerAccountModel model : ledgerAccounts) {
            newLedgerAccount(ledger, model);
        }
        return ledger;
    }

    private ChartOfAccountBO getOrCreateCoa(final String ledgerName) {
        return coaService.findChartOfAccountsByName(ledgerName)
                       .orElseGet(() -> {
                           ChartOfAccountBO coa = buildCoa(ledgerName);
                           return coaService.newChartOfAccount(coa);
                       });
    }

    private LedgerBO getOrCreateLedger(final String ledgerName, final ChartOfAccountBO coa) {
        Optional<LedgerBO> ledger = ledgerService.findLedgerByName(ledgerName);

        if (ledger.isPresent()) {
            return ledger.get();
        }

        LedgerBO rawLedger = buildLedger(ledgerName, coa);
        return ledgerService.newLedger(rawLedger);
    }

    private void newLedgerAccount(LedgerBO ledger, LedgerAccountModel model) {
        try {
            ledgerService.findLedgerAccount(ledger, model.getName());
        } catch (PostingModuleException ex) {
            // NoOp. Create ledger account below.
            if (ex.getErrorCode() == LEDGER_ACCOUNT_NOT_FOUND) {
                LedgerAccountBO parent = getParentLedgerAccount(ledger, model);
                LedgerAccountBO la = newLedgerAccountObj(ledger, model, parent);
                ledgerService.newLedgerAccount(la, SYSTEM);
            } else {
                throw ex;
            }
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

        return buildNewLedgerAccount(ledger, parent, shortDesc, name, balanceSide, category);
    }

    /*
     * Check if update required. If then process the config file.
     */

    private boolean updateRequired(ASPSPConfigData aspspConfigData) {
        return aspspConfigData.getUpdateMarkerAccountNbr() == null ||
                       ledgerService.findLedgerByName(aspspConfigData.getLedger())
                               .map(l -> !ledgerService.checkIfLedgerAccountExist(l, aspspConfigData.getUpdateMarkerAccountNbr()))
                               .orElse(true);
    }

    private LedgerBO buildLedger(String ledgerName, ChartOfAccountBO coa) {
        LedgerBO ledger = new LedgerBO();
        ledger.setName(ledgerName);
        ledger.setShortDesc(ledgerName);
        ledger.setCoa(coa);
        return ledger;
    }

    private ChartOfAccountBO buildCoa(String ledgerName) {
        ChartOfAccountBO coa = new ChartOfAccountBO();
        coa.setUserDetails(SYSTEM);
        coa.setName(ledgerName);
        coa.setShortDesc("COA: " + ledgerName);
        return coa;
    }

    private LedgerAccountBO buildNewLedgerAccount(LedgerBO ledger, LedgerAccountBO parent, String shortDesc, String name, BalanceSideBO balanceSide, AccountCategoryBO category) {
        LedgerAccountBO la = new LedgerAccountBO();
        la.setId(Ids.id());
        la.setCreated(LocalDateTime.now());
        la.setShortDesc(shortDesc);
        la.setName(name);
        la.setLedger(ledger);
        la.setParent(parent);
        la.setCoa(ledger.getCoa());
        la.setBalanceSide(balanceSide);
        la.setCategory(category);
        return la;
    }
}
