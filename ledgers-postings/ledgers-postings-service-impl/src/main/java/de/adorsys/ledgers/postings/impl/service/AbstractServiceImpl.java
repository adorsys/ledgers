package de.adorsys.ledgers.postings.impl.service;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.ChartOfAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;

public class AbstractServiceImpl {

    @Autowired
    protected LedgerAccountRepository ledgerAccountRepository;

    @Autowired
    protected ChartOfAccountRepository chartOfAccountRepo;

    @Autowired
    protected Principal principal;

    @Autowired
    protected LedgerRepository ledgerRepository;

    protected ChartOfAccount loadCoa(ChartOfAccountBO model) throws ChartOfAccountNotFoundException {
        if (model == null) {
            throw nullInfo();
        }
        if (model.getId() != null) {
            return chartOfAccountRepo.findById(model.getId())
                           .orElseThrow(() -> new ChartOfAccountNotFoundException(model));
        }
        if (model.getName() != null) {
            return chartOfAccountRepo.findOptionalByName(model.getName())
                           .orElseThrow(() -> new ChartOfAccountNotFoundException(model));
        }
        throw insufficientInfo(model);
    }

    /*
     * Load the ledger account. Using the following logic in the given order. 1-
     * If the Id is provided, we use find by id. 2- If the ledger and the name
     * is provided, we use them to load the account.
     */
    protected LedgerAccount loadLedgerAccount(LedgerAccountBO model) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        if (model == null) {
            throw nullInfo();
        }
        if (model.getId() != null) {
            return ledgerAccountRepository.findById(model.getId())
                           .orElseThrow(() -> new LedgerAccountNotFoundException(model.getId()));
        }
        if (model.getLedger() != null && model.getName() != null) {
            Ledger loadedLedger = loadLedger(model.getLedger());
            return ledgerAccountRepository.findOptionalByLedgerAndName(loadedLedger, model.getName())
                           .orElseThrow(() -> new LedgerAccountNotFoundException(model.getId()));
        }
        throw insufficientInfo(model);
    }
    protected LedgerAccount loadLedgerAccount(LedgerAccount model) throws LedgerAccountNotFoundException, LedgerNotFoundException {
        if (model == null) {
            throw nullInfo();
        }
        if (model.getId() != null) {
            return ledgerAccountRepository.findById(model.getId())
                           .orElseThrow(() -> new LedgerAccountNotFoundException(model.getId()));
        }
        if (model.getLedger() != null && model.getName() != null) {
            Ledger loadedLedger = loadLedger(model.getLedger());
            return ledgerAccountRepository.findOptionalByLedgerAndName(loadedLedger, model.getName())
                           .orElseThrow(() -> new LedgerAccountNotFoundException(model.getName(), model.getId()));
        }
        throw insufficientInfo(model);
    }

    protected Ledger loadLedger(LedgerBO model) throws LedgerNotFoundException {
        if (model == null) {
            throw nullInfo();
        }
        return loadLedgerByIdOrName(model.getId(), model.getName());
    }
    protected Ledger loadLedger(Ledger model) throws LedgerNotFoundException {
        if (model == null) {
            throw nullInfo();
        }
        return loadLedgerByIdOrName(model.getId(), model.getName());
    }

    private Ledger loadLedgerByIdOrName(String id, String name) throws LedgerNotFoundException {
        if (id != null) {
            return ledgerRepository.findById(id)
                           .orElseThrow(() -> new LedgerNotFoundException(id));
        }
        if (name != null) {
            return ledgerRepository.findOptionalByName(name)
                           .orElseThrow(() -> new LedgerNotFoundException(name,id));
        }
        throw insufficientInfo(String.format("id %s and name %s", id, name));
    }
    
    //TODO consider creating of exception builder with all necessary classes
    protected IllegalArgumentException insufficientInfo(Object modelObject) {
        return new IllegalArgumentException(
                String.format("Model Object does not provide sufficient information for loading original instance. %s",
                        modelObject.toString()));
    }

    private IllegalArgumentException nullInfo() {
        return new IllegalArgumentException("Model object can not be null");
    }
}
