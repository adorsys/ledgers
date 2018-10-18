package de.adorsys.ledgers.postings.service.impl;

import de.adorsys.ledgers.postings.converter.LedgerAccountMapper;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.exception.ChartOfAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.repository.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Principal;

public class AbstractServiceImpl {
    @Autowired
    LedgerAccountMapper ledgerAccountMapper;

    @Autowired
    protected PostingRepository postingRepository;

    @Autowired
    protected LedgerAccountRepository ledgerAccountRepository;

    @Autowired
    protected ChartOfAccountRepository chartOfAccountRepo;

    @Autowired
    protected Principal principal;

    @Autowired
    protected LedgerRepository ledgerRepository;

    @Autowired
    protected PostingLineRepository postingLineRepository;


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
                           .orElseThrow(() -> new LedgerAccountNotFoundException(model.getId()));
        }
        throw insufficientInfo(model);
    }

    public Ledger loadLedger(Ledger model) throws LedgerNotFoundException {
        if (model == null) {
            throw nullInfo();
        }
        if (model.getId() != null) {
            return ledgerRepository.findById(model.getId())
                           .orElseThrow(() -> new LedgerNotFoundException(model.getId()));
        }
        if (model.getName() != null) {
            return ledgerRepository.findOptionalByName(model.getName())
                           .orElseThrow(() -> new LedgerNotFoundException(model.getName(), model.getId()));
        }
        throw insufficientInfo(model);
    }

    //TODO consider creating of exception builder with all necessary classes
    private IllegalArgumentException insufficientInfo(Object modelObject) {
        return new IllegalArgumentException(
                String.format("Model Object does not provide sufficient information for loading original instance. %s",
                        modelObject.toString()));
    }

    private IllegalArgumentException nullInfo() {
        return new IllegalArgumentException("Model object can not be null");
    }
}
