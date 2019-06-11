package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.exception.DepositAccountUncheckedException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigData;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import de.adorsys.ledgers.deposit.api.service.domain.LedgerAccountModel;
import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.api.exception.ChartOfAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.ChartOfAccountService;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.util.Ids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DepositAccountInitServiceImpl implements DepositAccountInitService {

    private static final String SYSTEM = "System";
    private final ChartOfAccountService coaService;
    private final LedgerService ledgerService;
    private final ASPSPConfigSource configSource;

    @Autowired
    public DepositAccountInitServiceImpl(ChartOfAccountService coaService, LedgerService ledgerService, ASPSPConfigSource configSource) {
        this.coaService = coaService;
        this.ledgerService = ledgerService;
        this.configSource = configSource;
    }

    @Override
    public void initConfigData() {
        ASPSPConfigData aspspConfigData = configSource.aspspConfigData();

        // Only process if update is required.
        if (updateRequired(aspspConfigData)) {
            try {
                processASPSPConfigData(aspspConfigData);
            } catch (LedgerNotFoundException | LedgerAccountNotFoundException | ChartOfAccountNotFoundException e) {
                throw new DepositAccountUncheckedException(e.getMessage(), e);
            }
        }
    }

    private void processASPSPConfigData(ASPSPConfigData aspspConfigData) throws LedgerNotFoundException, ChartOfAccountNotFoundException, LedgerAccountNotFoundException {
        LedgerBO ledger = loadASPSPAccounts(aspspConfigData.getLedger(), aspspConfigData.getCoaFile());

        List<LedgerAccountModel> coaExtensions = aspspConfigData.getCoaExtensions();
        for (LedgerAccountModel ledgerAccountModel : coaExtensions) {
            newLedgerAccount(ledger, ledgerAccountModel);
        }
    }

    private LedgerBO loadASPSPAccounts(String ledgerName, String coaFile) throws LedgerNotFoundException, ChartOfAccountNotFoundException, LedgerAccountNotFoundException {
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

    private LedgerBO getOrCreateLedger(final String ledgerName, final ChartOfAccountBO coa) throws LedgerNotFoundException, ChartOfAccountNotFoundException {
        Optional<LedgerBO> ledger = ledgerService.findLedgerByName(ledgerName);

        if (ledger.isPresent()) {
            return ledger.get();
        }

        LedgerBO rawLedger = buildLedger(ledgerName, coa);
        return ledgerService.newLedger(rawLedger);
    }

    private void newLedgerAccount(LedgerBO ledger, LedgerAccountModel model) throws LedgerNotFoundException, LedgerAccountNotFoundException {
        try {
            ledgerService.findLedgerAccount(ledger, model.getName());
        } catch (LedgerAccountNotFoundException ex) {
            // NoOp. Create ledger account below.
            LedgerAccountBO parent = getParentLedgerAccount(ledger, model);
            LedgerAccountBO la = newLedgerAccountObj(ledger, model, parent);
            ledgerService.newLedgerAccount(la, SYSTEM);
        }
    }

    private LedgerAccountBO getParentLedgerAccount(LedgerBO ledger, LedgerAccountModel model) {
        LedgerAccountBO parent = null;
        if (model.getParent() != null) {
            try {
                parent = ledgerService.findLedgerAccount(ledger, model.getParent());
            } catch (LedgerNotFoundException | LedgerAccountNotFoundException e) {
                throw new IllegalStateException(
                        String.format("Missing ledger account with id %s", model.getParent()));
            }
        }
        return parent;
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
                               .map(l -> checkLedgerAccountAbsent(aspspConfigData.getUpdateMarkerAccountNbr(), l))
                               .orElse(true);
    }

    private boolean checkLedgerAccountAbsent(String updateMarkerAccountNbr, LedgerBO ledger) {
        try {
            ledgerService.findLedgerAccount(ledger, updateMarkerAccountNbr);
            // Ledger account present.
            return false;
        } catch (LedgerNotFoundException e) {
            throw new DepositAccountUncheckedException(e.getMessage(), e);
        } catch (LedgerAccountNotFoundException e) {
            // ledger account non existent.
            return true;
        }
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
