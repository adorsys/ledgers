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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PostingRepositoryApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("ChartOfAccountRepositoryIT-db-entries.xml")
@DatabaseTearDown(value = {"ChartOfAccountRepositoryIT-db-entries.xml"}, type = DatabaseOperation.DELETE_ALL)
class ChartOfAccountRepositoryIT {
    @Autowired
    private ChartOfAccountRepository chartOfAccountRepository;

    @Test
    void test_create_coa_ok() {
        ChartOfAccount coa = new ChartOfAccount(Ids.id(), null, "francis", null, null, Ids.id());
        ChartOfAccount chart = chartOfAccountRepository.save(coa);
        assertNotNull(chart);
    }

    @Test
    void test_load_coa() {
        // When
        ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8bcdTrCsi-F3sT3i-g").orElse(null);

        // Then
        assertNotNull(coa);
    }

    @Test
    void test_create_coa_unique_constrain_violation_name() {
        // Given
        ChartOfAccount coa = chartOfAccountRepository.findById("ci8k8bcdTrCsi-F3sT3i-g").orElse(null);
        assumeTrue(coa != null);
        ChartOfAccount coa2 = new ChartOfAccount(Ids.id(), null, "Francis", null, null, coa.getName());

        // Then
        assertThrows(DataIntegrityViolationException.class, () -> chartOfAccountRepository.save(coa2));
    }
}
