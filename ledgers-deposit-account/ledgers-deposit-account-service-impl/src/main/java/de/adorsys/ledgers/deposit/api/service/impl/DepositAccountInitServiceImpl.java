package de.adorsys.ledgers.deposit.api.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.exception.DepositAccountUncheckedException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigData;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import de.adorsys.ledgers.deposit.api.service.domain.LedgerAccountModel;
import de.adorsys.ledgers.postings.api.domain.AccountCategoryBO;
import de.adorsys.ledgers.postings.api.domain.BalanceSideBO;
import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.ChartOfAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.ChartOfAccountService;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.util.Ids;

@Service
public class DepositAccountInitServiceImpl implements DepositAccountInitService{

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
    public void initConfigData() throws IOException {
        ASPSPConfigData aspspConfigData = configSource.aspspConfigData();

        // Only process if update is required.
        if(updateRequired(aspspConfigData)) {
	        try {
	        	processASPSPConfigData(aspspConfigData);
	        } catch (LedgerNotFoundException | LedgerAccountNotFoundException | ChartOfAccountNotFoundException e) {
	        	throw new DepositAccountUncheckedException(e.getMessage(), e);
	        }
        }
    }

    private void processASPSPConfigData(ASPSPConfigData aspspConfigData) throws IOException, LedgerNotFoundException, LedgerAccountNotFoundException, ChartOfAccountNotFoundException {
        LedgerBO ledger = loadASPSPAccounts(aspspConfigData.getLedger(), aspspConfigData.getCoaFile());

        List<LedgerAccountModel> coaExtensions = aspspConfigData.getCoaExtensions();
        for (LedgerAccountModel ledgerAccountModel : coaExtensions) {
            newLedgerAccount(ledger, ledgerAccountModel);
        }
    }

    private LedgerBO loadASPSPAccounts(String ledgerName, String coaFile) throws IOException, LedgerNotFoundException, LedgerAccountNotFoundException, ChartOfAccountNotFoundException {
        ChartOfAccountBO coa = createCoa(ledgerName);
        coa = coaService.findChartOfAccountsByName(ledgerName).orElse(coaService.newChartOfAccount(coa));

        LedgerBO ledger = createLedger(ledgerName, coa);
        ledger = ledgerService.findLedgerByName(ledgerName).orElse(ledgerService.newLedger(ledger));

        List<LedgerAccountModel> ledgerAccounts = configSource.chartOfAccount(coaFile);
        for (LedgerAccountModel model : ledgerAccounts) {
            newLedgerAccount(ledger, model);
        }
        return ledger;
    }

    // todo: @fpo do we really need to return results by this method? because it never used
    private LedgerAccountBO newLedgerAccount(LedgerBO ledger, LedgerAccountModel model) throws LedgerNotFoundException, LedgerAccountNotFoundException {
    	try {
	        return ledgerService.findLedgerAccount(ledger, model.getName());
    	} catch (LedgerAccountNotFoundException ex) {
    		// NoOp. Create ledger account below.
            LedgerAccountBO parent = null;
            if (model.getParent() != null) {
                try {
                    parent = ledgerService.findLedgerAccount(ledger, model.getParent());
                } catch (LedgerNotFoundException | LedgerAccountNotFoundException e) {
                    throw new IllegalStateException(String.format("Missing ledger account with id %s", model.getParent()));
                }
            }

            LedgerAccountBO la = newLedgerAccountObj(ledger, model, parent);
            return ledgerService.newLedgerAccount(la);
    	}
    }

	private LedgerAccountBO newLedgerAccountObj(LedgerBO ledger, LedgerAccountModel model, LedgerAccountBO parent) {
		String shortDesc = model.getShortDesc();
		String name = model.getName();
		BalanceSideBO balanceSide = model.getBalanceSide() != null ? model.getBalanceSide() : parent.getBalanceSide();
		AccountCategoryBO category = model.getCategory() != null ? model.getCategory() : parent.getCategory();
		String longDesc = null;
		LocalDateTime created = LocalDateTime.now();

		LedgerAccountBO la = new LedgerAccountBO();
		la.setId(Ids.id());
		la.setCreated(created);
		la.setShortDesc(shortDesc);
		la.setLongDesc(longDesc);
		la.setName(name);
		la.setLedger(ledger);
		la.setParent(parent);
		la.setCoa(ledger.getCoa());
		la.setBalanceSide(balanceSide);
		la.setCategory(category);
		return la;
	}
	
	/*
	 * Check if update required. If then process the config file.
	 */
	private boolean updateRequired(ASPSPConfigData aspspConfigData) {
		// Check ledger present
		Optional<LedgerBO> ledgerOption = ledgerService.findLedgerByName(aspspConfigData.getLedger());
		if(!ledgerOption.isPresent()) {
			return true;
		}
		
		String updateMarkerAccountNbr = aspspConfigData.getUpdateMarkerAccountNbr();
		if(updateMarkerAccountNbr==null) {
			return true;
		}

		return checkLedgerAccountPresent(updateMarkerAccountNbr, ledgerOption.get());
	}

	private boolean checkLedgerAccountPresent(String updateMarkerAccountNbr, LedgerBO ledger) {
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

	private LedgerBO createLedger(String ledgerName, ChartOfAccountBO coa) {
		LedgerBO ledger = new LedgerBO();
        ledger.setName(ledgerName);
        ledger.setShortDesc(ledgerName);
        ledger.setCoa(coa);
		return ledger;
	}

	private ChartOfAccountBO createCoa(String ledgerName) {
		ChartOfAccountBO coa = new ChartOfAccountBO();
        coa.setName(ledgerName);
        coa.setShortDesc("COA: " + ledgerName);
		return coa;
	}

}
