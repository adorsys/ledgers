/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractServiceImplTest {
    private static final String ACCOUNT_ID = "ledger account id";
    private static final String ACCOUNT_NAME = "ledger account name";
    private static final String LEDGER_ID = "ledger id";
    private static final String LEDGER_NAME = "ledger name";

    @InjectMocks
    AbstractServiceImpl service;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;
    @Mock
    private ChartOfAccountRepository chartOfAccountRepo;
    @Mock
    private LedgerRepository ledgerRepository;

    @Test
    void loadCoa() {
        // Given
        when(chartOfAccountRepo.findById(anyString())).thenReturn(Optional.of(getCoa()));

        // When
        ChartOfAccount result = service.loadCoa(getCoaBO("id", "name"));

        // Then
        assertEquals(new ChartOfAccount(), result);
    }

    @Test
    void loadCoa_id_not_present() {
        // Given
        when(chartOfAccountRepo.findOptionalByName(anyString())).thenReturn(Optional.of(getCoa()));

        // When
        ChartOfAccount result = service.loadCoa(getCoaBO(null, "name"));

        // Then
        assertEquals(new ChartOfAccount(), result);
    }

    @Test
    void loadCoa_no_identifier_present() {
        // Then
        ChartOfAccountBO coaBO = getCoaBO(null, null);
        assertThrows(PostingModuleException.class, () -> service.loadCoa(coaBO));
    }

    @Test
    void loadCoa_by_id_not_found() {
        // Given
        when(chartOfAccountRepo.findById(anyString())).thenReturn(Optional.empty());
        ChartOfAccountBO coaBO = getCoaBO("id", "name");
        // Then
        assertThrows(PostingModuleException.class, () -> service.loadCoa(coaBO));
    }

    @Test
    void loadCoa_by_name_not_found() {
        // Given
        when(chartOfAccountRepo.findOptionalByName(anyString())).thenReturn(Optional.empty());
        ChartOfAccountBO coaBO = getCoaBO(null, "name");
        // Then
        assertThrows(PostingModuleException.class, () -> service.loadCoa(coaBO));
    }

    @Test
    void loadCoa_null_body() {
        // Then
        assertThrows(PostingModuleException.class, () -> service.loadCoa(null));
    }

    @Test
    void loadLedgerAccountBO() {
        // Given
        when(ledgerAccountRepository.findById(anyString())).thenReturn(Optional.of(new LedgerAccount()));
        LedgerBO ledger = getLedger(LEDGER_ID, LEDGER_NAME);

        // When
        LedgerAccount result = service.loadLedgerAccountBO(getLedgerAccountBO(ACCOUNT_ID, ACCOUNT_NAME, ledger));

        // Then
        assertEquals(new LedgerAccount(), result);
    }

    @Test
    void loadLedgerAccountBO_by_name_n_ledger_id() {
        // Given
        when(ledgerRepository.findById(anyString())).thenReturn(Optional.of(new Ledger()));
        when(ledgerAccountRepository.findOptionalByLedgerAndName(any(), anyString())).thenReturn(Optional.of(new LedgerAccount()));
        LedgerBO ledger = getLedger(LEDGER_ID, LEDGER_NAME);

        // When
        LedgerAccount result = service.loadLedgerAccountBO(getLedgerAccountBO(null, ACCOUNT_NAME, ledger));

        // Then
        assertEquals(new LedgerAccount(), result);
    }

    @Test
    void loadLedgerAccountBO_by_name_n_ledger_name() {
        // Given
        when(ledgerRepository.findOptionalByName(anyString())).thenReturn(Optional.of(new Ledger()));
        when(ledgerAccountRepository.findOptionalByLedgerAndName(any(), anyString())).thenReturn(Optional.of(new LedgerAccount()));
        LedgerBO ledger = getLedger(null, LEDGER_NAME);

        // When
        LedgerAccount result = service.loadLedgerAccountBO(getLedgerAccountBO(null, ACCOUNT_NAME, ledger));

        // Then
        assertEquals(new LedgerAccount(), result);
    }

    @Test
    void loadLedgerAccountBO_by_name_n_ledger_insufficient_ledger_info() {
        // Given
        LedgerBO ledger = getLedger(null, null);
        LedgerAccountBO ledgerAccountBO = getLedgerAccountBO(null, ACCOUNT_NAME, ledger);
        // Then
        assertThrows(PostingModuleException.class, () -> service.loadLedgerAccountBO(ledgerAccountBO));
    }

    @Test
    void loadLedgerAccountBO_by_id_nf() {
        // Given
        when(ledgerAccountRepository.findById(anyString())).thenReturn(Optional.empty());
        LedgerBO ledger = getLedger(null, null);
        LedgerAccountBO ledgerAccountBO = getLedgerAccountBO(ACCOUNT_ID, ACCOUNT_NAME, ledger);
        // Then
        assertThrows(PostingModuleException.class, () -> service.loadLedgerAccountBO(ledgerAccountBO));
    }

    @Test
    void loadLedgerAccountBO_by_name_nf() {
        // Given
        when(ledgerRepository.findById(anyString())).thenReturn(Optional.of(new Ledger()));
        when(ledgerAccountRepository.findOptionalByLedgerAndName(any(), anyString())).thenReturn(Optional.empty());
        LedgerBO ledger = getLedger(LEDGER_ID, null);
        LedgerAccountBO ledgerAccountBO = getLedgerAccountBO(null, ACCOUNT_NAME, ledger);
        // Then
        assertThrows(PostingModuleException.class, () -> service.loadLedgerAccountBO(ledgerAccountBO));
    }

    @Test
    void loadLedgerAccountBO_null() {
        // Then
        assertThrows(PostingModuleException.class, () -> service.loadLedgerAccountBO(null));
    }

    @Test
    void loadLedgerAccountBO_null_info() {
        // Given
        LedgerBO ledger = getLedger(LEDGER_ID, null);
        LedgerAccountBO ledgerAccountBO = getLedgerAccountBO(null, null, ledger);
        // Then
        assertThrows(PostingModuleException.class, () -> service.loadLedgerAccountBO(ledgerAccountBO));
    }

    @Test
    void loadLedger() {
        // Then
        assertThrows(PostingModuleException.class, () -> service.loadLedger((LedgerBO) null));
    }

    @Test
    void loadLedger_id_nf() {
        // Given
        when(ledgerRepository.findById(anyString())).thenReturn(Optional.empty());
        LedgerBO ledger = getLedger(LEDGER_ID, LEDGER_NAME);
        // Then
        assertThrows(PostingModuleException.class, () -> service.loadLedger(ledger));
    }

    @Test
    void loadLedger_name_nf() {
        // Given
        when(ledgerRepository.findOptionalByName(anyString())).thenReturn(Optional.empty());
        LedgerBO ledger = getLedger(null, LEDGER_NAME);
        // Then
        assertThrows(PostingModuleException.class, () -> service.loadLedger(ledger));
    }

    @Test
    void loadLedger_null() {
        // Then
        assertThrows(PostingModuleException.class, () -> service.loadLedger((Ledger) null));
    }

    private ChartOfAccount getCoa() {
        return new ChartOfAccount();
    }

    private ChartOfAccountBO getCoaBO(String id, String name) {
        return new ChartOfAccountBO(name, id, LocalDateTime.now(), "details", "short descr", "long desc");
    }

    private LedgerAccountBO getLedgerAccountBO(String id, String name, LedgerBO ledger) {
        LedgerAccountBO account = new LedgerAccountBO();
        account.setId(id);
        account.setName(name);
        account.setLedger(ledger);
        return account;
    }

    private LedgerBO getLedger(String id, String name) {
        LedgerBO ledger = new LedgerBO();
        ledger.setId(id);
        ledger.setName(name);
        return ledger;
    }

}