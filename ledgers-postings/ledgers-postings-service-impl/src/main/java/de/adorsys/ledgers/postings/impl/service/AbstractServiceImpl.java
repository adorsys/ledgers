package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.PostingModuleException;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.impl.converter.LedgerAccountMapper;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;

import static de.adorsys.ledgers.postings.api.exception.PostingErrorCode.*;

@RequiredArgsConstructor
public class AbstractServiceImpl {
    private static final String COA_NF_BY_ID_MSG = "Chart of Account with id: %s not found!";
    private static final String COA_NF_BY_NAME_MSG = "Chart of Account with name: %s not found!";
    private static final String LA_NF_BY_ID_MSG = "Ledger Account with id: %s not found!";
    protected static final String LA_NF_BY_NAME_MSG = "Ledger Account with Ledger name : %s not found!";
    private static final String LEDGER_NF_BY_ID_MSG = "Ledger with id: %s not found!";
    private static final String LEDGER_NF_BY_NAME_MSG = "Ledger with Ledger name : %s not found!";

    protected final LedgerAccountRepository ledgerAccountRepository;
    protected final ChartOfAccountRepository chartOfAccountRepo;
    protected final LedgerRepository ledgerRepository;
    protected final LedgerAccountMapper ledgerAccountMapper = Mappers.getMapper(LedgerAccountMapper.class);

    protected ChartOfAccount loadCoa(ChartOfAccountBO chartOfAccountBO) {
        if (chartOfAccountBO == null) {
            throw nullInfo();
        }
        if (chartOfAccountBO.getId() != null) {
            return chartOfAccountRepo.findById(chartOfAccountBO.getId())
                           .orElseThrow(() -> PostingModuleException.builder()
                                                      .errorCode(CHART_OF_ACCOUNT_NOT_FOUND)
                                                      .devMsg(String.format(COA_NF_BY_ID_MSG, chartOfAccountBO.getId()))
                                                      .build());
        }
        if (chartOfAccountBO.getName() != null) {
            return chartOfAccountRepo.findOptionalByName(chartOfAccountBO.getName())
                           .orElseThrow(() -> PostingModuleException.builder()
                                                      .errorCode(CHART_OF_ACCOUNT_NOT_FOUND)
                                                      .devMsg(String.format(COA_NF_BY_NAME_MSG, chartOfAccountBO.getName()))
                                                      .build());
        }
        throw insufficientInfo(chartOfAccountBO);
    }

    /*
     * Load the ledger account. Using the following logic in the given order. 1-
     * If the Id is provided, we use find by id. 2- If the ledger and the name
     * is provided, we use them to load the account.
     */
    protected LedgerAccount loadLedgerAccountBO(LedgerAccountBO ledgerAccountBO) {
        LedgerAccount ledgerAccount = ledgerAccountMapper.toLedgerAccount(ledgerAccountBO);
        return loadLedgerAccount(ledgerAccount);
    }

    protected LedgerAccount loadLedgerAccount(LedgerAccount ledgerAccount) {
        if (ledgerAccount == null) {
            throw nullInfo();
        }
        if (ledgerAccount.getId() != null) {
            return ledgerAccountRepository.findById(ledgerAccount.getId())
                           .orElseThrow(() -> PostingModuleException.builder()
                                                      .errorCode(LEDGER_ACCOUNT_NOT_FOUND)
                                                      .devMsg(String.format(LA_NF_BY_ID_MSG, ledgerAccount.getId()))
                                                      .build());
        }
        if (ledgerAccount.getLedger() != null && ledgerAccount.getName() != null) {
            Ledger loadedLedger = loadLedger(ledgerAccount.getLedger());
            return ledgerAccountRepository.findOptionalByLedgerAndName(loadedLedger, ledgerAccount.getName())
                           .orElseThrow(() -> PostingModuleException.builder()
                                                      .errorCode(LEDGER_ACCOUNT_NOT_FOUND)
                                                      .devMsg(String.format(LA_NF_BY_NAME_MSG, ledgerAccount.getName()))
                                                      .build());
        }
        throw insufficientInfo(ledgerAccount);
    }

    protected Ledger loadLedger(LedgerBO ledgerBO) {
        if (ledgerBO == null) {
            throw nullInfo();
        }
        return loadLedgerByIdOrName(ledgerBO.getId(), ledgerBO.getName());
    }

    protected Ledger loadLedger(Ledger ledger) {
        if (ledger == null) {
            throw nullInfo();
        }
        return loadLedgerByIdOrName(ledger.getId(), ledger.getName());
    }

    private Ledger loadLedgerByIdOrName(String id, String name) {
        if (id != null) {
            return ledgerRepository.findById(id)
                           .orElseThrow(() -> PostingModuleException.builder()
                                                      .errorCode(LEDGER_NOT_FOUND)
                                                      .devMsg(String.format(LEDGER_NF_BY_ID_MSG, id))
                                                      .build());
        }
        if (name != null) {
            return ledgerRepository.findOptionalByName(name)
                           .orElseThrow(() -> PostingModuleException.builder()
                                                      .errorCode(LEDGER_NOT_FOUND)
                                                      .devMsg(String.format(LEDGER_NF_BY_NAME_MSG, name))
                                                      .build());
        }
        throw insufficientInfo("Both id and name fields are NULL!");
    }

    private PostingModuleException insufficientInfo(Object modelObject) {
        return PostingModuleException.builder()
                       .errorCode(NOT_ENOUGH_INFO)
                       .devMsg(String.format("Model Object does not provide sufficient information for loading original instance. %s", modelObject.toString()))
                       .build();
    }

    private PostingModuleException nullInfo() {
        return PostingModuleException.builder()
                       .errorCode(NOT_ENOUGH_INFO)
                       .devMsg("Model object can not be null")
                       .build();
    }
}
