/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.tests.PostingRepositoryApplication;
import de.adorsys.ledgers.util.Ids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PostingRepositoryApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("LedgerRepositoryIT-db-entries.xml")
@DatabaseTearDown(value = {"LedgerRepositoryIT-db-entries.xml"}, type = DatabaseOperation.DELETE_ALL)
class LedgerRepositoryIT {

    @Autowired
    private ChartOfAccountRepository chartOfAccountRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Test
    void test_create_ledger_ok() {
        ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8PDcTrCsi-F3sT3i-g").orElse(null);
        assumeTrue(coa != null);
        String id = Ids.id();
        LocalDateTime created = LocalDateTime.now();
        String user = "Sample User";
        String shortDesc = null;
        String longDesc = null;
        String name = "Sample Ledger-2";
        Ledger ledger = new Ledger(id, created, user, shortDesc, longDesc, name, coa);
        Ledger saved = ledgerRepository.save(ledger);
        assertNotNull(saved);
    }

    @Test
    void test_create_ledger_no_coa() {
        // Given
        ChartOfAccount coa = null;
        String id = Ids.id();
        LocalDateTime created = null;
        String user = "Sample User";
        String shortDesc = null;
        String longDesc = null;
        String name = "Sample Ledger-2";
        Ledger ledger = new Ledger(id, created, user, shortDesc, longDesc, name, coa);

        // Then
        assertThrows(DataIntegrityViolationException.class, () -> ledgerRepository.save(ledger));
    }

    @Test
    void test_create_ledger_no_name() {
        // Given
        ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8zskTrCsi-F3sT3i-g").orElse(null);
        String id = Ids.id();
        LocalDateTime created = null;
        String user = "Sample User";
        String shortDesc = null;
        String longDesc = null;
        String name = null;
        Ledger ledger = new Ledger(id, created, user, shortDesc, longDesc, name, coa);

        // Then
        assertThrows(DataIntegrityViolationException.class, () -> ledgerRepository.save(ledger));
    }

    @Test
    void test_create_ledger_unique_constrain_violation_name() {
        // Given
        Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        assumeTrue(ledger != null);
        ChartOfAccount coa = chartOfAccountRepository.findById(ledger.getCoa().getId()).orElse(null);
        assumeTrue(coa != null);
        String id = Ids.id();
        LocalDateTime created = null;
        String user = "Sample User";
        String shortDesc = null;
        String longDesc = null;
        String name = ledger.getName();
        Ledger ledger2 = new Ledger(id, created, user, shortDesc, longDesc, name, coa);

        // Then
        assertThrows(DataIntegrityViolationException.class, () -> ledgerRepository.save(ledger2));
    }

    @Test
    void test_find_by_coa_returns_n_records() {
        // Given
        Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        assumeTrue(ledger != null);
        ChartOfAccount coa = chartOfAccountRepository.findById(ledger.getCoa().getId()).orElse(null);

        // When
        List<Ledger> list = ledgerRepository.findByCoa(coa);

        // Then
        assertEquals(2, list.size());
    }

    @Test
    void test_find_by_name_returns_one_record() {
        // Given
        Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        assumeTrue(ledger != null);

        // When
        Optional<Ledger> opt = ledgerRepository.findOptionalByName(ledger.getName());

        // Then
        assertTrue(opt.isPresent());
    }

    @Test
    void test_find_by_name_not_found() {
        // When
        Optional<Ledger> opt = ledgerRepository.findOptionalByName("wrongName");

        // Then
        assertFalse(opt.isPresent());
    }
}
