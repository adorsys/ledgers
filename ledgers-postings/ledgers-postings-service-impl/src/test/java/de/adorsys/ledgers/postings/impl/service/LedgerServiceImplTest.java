/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.db.domain.*;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.impl.converter.LedgerAccountMapper;
import de.adorsys.ledgers.postings.impl.converter.LedgerMapper;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerServiceImplTest {
    private static final LocalDateTime DATE_TIME = LocalDateTime.now();
    private static final String USER_NAME = "Mr. Jones";
    private static final ChartOfAccount COA = new ChartOfAccount(Ids.id(), DATE_TIME, USER_NAME,
                                                                 "Some short description", "Some long description", "COA");
    private static final Ledger LEDGER = new Ledger(Ids.id(), DATE_TIME, USER_NAME, "Some short description",
                                                    "Some long description", "Ledger", COA);
    private static final LedgerAccount LEDGER_ACCOUNT = new LedgerAccount(Ids.id(), DATE_TIME, USER_NAME,
                                                                          "Some short description", "Some long description", USER_NAME, LEDGER, null, COA, BalanceSide.Cr,
                                                                          AccountCategory.AS);
    private static final LedgerMapper LEDGER_MAPPER = Mappers.getMapper(LedgerMapper.class);
    private static final LedgerAccountMapper LEDGER_ACCOUNT_MAPPER = Mappers.getMapper(LedgerAccountMapper.class);
    private static final String SYSTEM = "System";

    @InjectMocks
    private LedgerServiceImpl ledgerService;

    @Mock
    private ChartOfAccountRepository chartOfAccountRepository;
    @Mock
    private LedgerRepository ledgerRepository;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Test
    void new_ledger_must_produce_id_created_user_copy_other_fields() {
        // Given
        when(chartOfAccountRepository.findById(COA.getId())).thenReturn(Optional.of(COA));
        when(ledgerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        LedgerBO result = ledgerService.newLedger(LEDGER_MAPPER.toLedgerBO(LEDGER));

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getCreated());
        assertEquals(LEDGER.getName(), result.getUserDetails());
        assertEquals(LEDGER.getName(), result.getName());
        assertEquals(LEDGER.getShortDesc(), result.getShortDesc());
        assertEquals(LEDGER.getLongDesc(), result.getLongDesc());

        assertNotNull(result.getCoa());
    }

    @Test
    void new_ledgerAccount_must_produce_id_created_user_copy_other_fields() {
        // Given
        when(ledgerAccountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(LEDGER));

        // When
        LedgerAccountBO result = ledgerService.newLedgerAccount(LEDGER_ACCOUNT_MAPPER.toLedgerAccountBO(LEDGER_ACCOUNT), SYSTEM);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getCreated());
        assertEquals(SYSTEM, result.getUserDetails());
        assertEquals(LEDGER_ACCOUNT.getName(), result.getName());
        assertEquals(LEDGER.getShortDesc(), result.getShortDesc());
        assertEquals(LEDGER.getLongDesc(), result.getLongDesc());

        assertNotNull(result.getCoa());
        assertNotNull(result.getLedger());
    }

    @Test
    void new_ledgerAccount_ledger_account_name_absent() {
        LedgerAccountBO ledgerAccountBO = new LedgerAccountBO();
        // Then
        assertThrows(PostingModuleException.class, () -> ledgerService.newLedgerAccount(ledgerAccountBO, USER_NAME));
    }

    @Test
    void findLedgerAccountById() {
        // Given
        when(ledgerAccountRepository.findById(anyString())).thenReturn(Optional.of(new LedgerAccount()));

        // When
        LedgerAccountBO result = ledgerService.findLedgerAccountById(LEDGER_ACCOUNT.getId());

        // Then
        assertEquals(new LedgerAccountBO(), result);
    }

    @Test
    void findLedgerAccountById_nf() {
        // Given
        when(ledgerAccountRepository.findById(anyString())).thenReturn(Optional.empty());
        String id = LEDGER_ACCOUNT.getId();
        // Then
        assertThrows(PostingModuleException.class, () -> ledgerService.findLedgerAccountById(id));
    }

    @Test
    void findLedgerAccount() {
        // Given
        when(ledgerAccountRepository.findOptionalByLedgerAndName(any(), anyString())).thenReturn(Optional.of(new LedgerAccount()));
        when(ledgerRepository.findOptionalByName(anyString())).thenReturn(Optional.of(new Ledger()));

        // When
        LedgerAccountBO result = ledgerService.findLedgerAccount(new LedgerBO("name", null), LEDGER_ACCOUNT.getName());

        // Then
        assertEquals(new LedgerAccountBO(), result);
    }

    @Test
    void findLedgerAccount_nf() {
        when(ledgerAccountRepository.findOptionalByLedgerAndName(any(), anyString())).thenReturn(Optional.empty());
        when(ledgerRepository.findOptionalByName(anyString())).thenReturn(Optional.of(new Ledger()));
        LedgerBO ledgerBO = new LedgerBO("name", null);
        String name = LEDGER_ACCOUNT.getName();
        assertThrows(PostingModuleException.class, () -> ledgerService.findLedgerAccount(ledgerBO, name));
    }

    @Test
    void finLedgerAccountsByIbans() {
        // Given
        when(ledgerRepository.findOptionalByName(anyString())).thenReturn(Optional.of(new Ledger()));
        when(ledgerAccountRepository.getAccountsByIbans(anySet(), any())).thenReturn(Collections.emptyList());

        // When
        Map<String, LedgerAccountBO> result = ledgerService.finLedgerAccountsByIbans(new HashSet<>(), new LedgerBO("name", null));

        // Then
        assertEquals(new HashMap<>(), result);
    }

    @Test
    void checkIfLedgerAccountExist() {
        // Given
        when(ledgerAccountRepository.findOptionalByLedgerAndName(any(), anyString()))
                .thenReturn(Optional.of(new LedgerAccount()));
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(new Ledger()));
        LedgerBO testLedger = new LedgerBO(null, "id", null, null, null, null, null);

        // When
        boolean result = ledgerService.checkIfLedgerAccountExist(testLedger, "test name");

        // Then
        assertTrue(result);
    }

    @Test
    void checkIfLedgerAccountExist_account_not_present() {
        // Given
        when(ledgerAccountRepository.findOptionalByLedgerAndName(any(), anyString()))
                .thenReturn(Optional.empty());
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(new Ledger()));
        LedgerBO testLedger = new LedgerBO(null, "id", null, null, null, null, null);

        // When
        boolean result = ledgerService.checkIfLedgerAccountExist(testLedger, "test name");

        // Then
        assertFalse(result);
    }

    @Test
    void checkIfLedgerAccountExist_ledger_not_present() {
        // Given
        when(ledgerRepository.findById(any())).thenReturn(Optional.empty());
        LedgerBO testLedger = new LedgerBO(null, "id", null, null, null, null, null);

        // When
        boolean result = ledgerService.checkIfLedgerAccountExist(testLedger, "test name");

        // Then
        assertFalse(result);
    }
}
