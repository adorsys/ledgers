package de.adorsys.ledgers.deposit.api.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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

@Configuration
@ComponentScan(basePackages = {"de.adorsys.ledgers.deposit"})
@EnableJpaAuditing
@EnableJpaRepositories
@EntityScan(basePackages = "de.adorsys.ledgers.deposit.domain")
public class DepositAccountConfiguration {

    private final ChartOfAccountService coaService;
    private final LedgerService ledgerService;
    private final ASPSPConfigSource configSource;

    @Autowired
    public DepositAccountConfiguration(ChartOfAccountService coaService, LedgerService ledgerService, ASPSPConfigSource configSource) {
        this.coaService = coaService;
        this.ledgerService = ledgerService;
        this.configSource = configSource;
    }

    @Bean
    public ASPSPConfigData initConfigData() throws IOException {
        ASPSPConfigData aspspConfigData = configSource.aspspConfigData();
        try {
            processASPSPConfigData(aspspConfigData);
        } catch (LedgerNotFoundException | LedgerAccountNotFoundException | ChartOfAccountNotFoundException e) {
            throw new IllegalStateException(e);
        }
        return aspspConfigData;
    }

    private void processASPSPConfigData(ASPSPConfigData aspspConfigData) throws IOException, LedgerNotFoundException, LedgerAccountNotFoundException, ChartOfAccountNotFoundException {
        LedgerBO ledger = loadASPSPAccounts(aspspConfigData.getLedger(), aspspConfigData.getCoaFile());

        List<LedgerAccountModel> coaExtensions = aspspConfigData.getCoaExtensions();
        for (LedgerAccountModel ledgerAccountModel : coaExtensions) {
            newLedgerAccount(ledger, ledgerAccountModel);
        }
    }

    private LedgerBO loadASPSPAccounts(String ledgerName, String coaFile) throws IOException, LedgerNotFoundException, LedgerAccountNotFoundException, ChartOfAccountNotFoundException {
        ChartOfAccountBO coa = new ChartOfAccountBO();
        coa.setName(ledgerName);
        coa.setShortDesc("COA: " + ledgerName);
        coa = coaService.findChartOfAccountsByName(ledgerName).orElse(coaService.newChartOfAccount(coa));

        LedgerBO ledger = new LedgerBO();
        ledger.setName(ledgerName);
        ledger.setShortDesc(ledgerName);
        ledger.setCoa(coa);
        ledger = ledgerService.findLedgerByName(ledgerName).orElse(ledgerService.newLedger(ledger));

        List<LedgerAccountModel> ledgerAccounts = configSource.chartOfAccount(coaFile);
        for (LedgerAccountModel model : ledgerAccounts) {
            newLedgerAccount(ledger, model);
        }
        return ledger;
    }

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
            return ledgerService.newLedgerAccount(la);
    	}
    }
}
