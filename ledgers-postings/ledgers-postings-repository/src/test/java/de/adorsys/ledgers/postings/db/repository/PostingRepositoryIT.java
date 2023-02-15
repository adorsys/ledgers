/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.OperationDetails;
import de.adorsys.ledgers.postings.db.domain.Posting;
import de.adorsys.ledgers.postings.db.domain.PostingType;
import de.adorsys.ledgers.postings.db.tests.PostingRepositoryApplication;
import de.adorsys.ledgers.postings.db.utils.RecordHashHelper;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.hash.HashGenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

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
@DatabaseSetup("PostingRepositoryIT-db-entries.xml")
@DatabaseTearDown(value = {"PostingRepositoryIT-db-entries.xml"}, type = DatabaseOperation.DELETE_ALL)
class PostingRepositoryIT {
    @Autowired
    private PostingRepository postingRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Test
    void test_create_posting_ok() {
        // Given
        Optional<Ledger> ledgerOption = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg");
        assumeTrue(ledgerOption.isPresent());
        Posting p = new Posting();
        p.setRecordUser("recUser");
        p.setOprId("oprId");
        p.setPstTime(LocalDateTime.now());
        p.setPstType(PostingType.BAL_STMT);
        p.setLedger(ledgerOption.get());
        p.setOprDetails(new OperationDetails("oprDetails"));
        p.setId(Ids.id());

        // When
        Posting posting = postingRepository.save(p);
        assertNotNull(posting);
    }

    @Test
    void test_load_posting_by_id_ok() {
        // When
        Optional<Posting> posting = postingRepository.findById("Zd0ND5YwSzGwIfZilhumPg_POSTING");

        // Then
        assertTrue(posting.isPresent());
    }

    @Test
    void test_find_posting_by_operation_id() {
        // When
        List<Posting> posting = postingRepository.findByOprId("Zd0ND5YwSzGwIfZilhumPg_OPERATION");

        // Then
        assertEquals(2, posting.size());
    }

    @Test
    void test_find_first_optional_by_ledger_order_by_record_time_desc() {
        // Given
        Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        assumeTrue(ledger != null);

        // When
        Posting posting = postingRepository.findFirstByLedgerOrderByRecordTimeDesc(ledger).orElse(null);

        // Then
        assumeTrue(posting != null);
        assertEquals("Zd0ND5YwSzGwIfZilhumPg_POSTING2", posting.getId());
        System.out.println(posting.getId());
    }

    @Test
    @Transactional
    void test_posting_hash() throws HashGenerationException {
        // Given
        Optional<Ledger> ledgerOptions = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg");
        assumeTrue(ledgerOptions.isPresent());
        Posting p = new Posting();
        p.setRecordUser("recUser");
        p.setOprId("oprId");
        p.setPstTime(LocalDateTime.now());
        p.setPstType(PostingType.BAL_STMT);
        p.setLedger(ledgerOptions.get());
        p.setOprDetails(new OperationDetails("oprDetails"));
        p.setId(Ids.id());
        Posting saved = postingRepository.save(p);
        saved = postingRepository.save(saved.hash());
        Posting found = postingRepository.findById(saved.getId()).orElse(null);
        assertEquals(found, saved);

        String recHash = found.getHash();
        RecordHashHelper recordHashHelper = new RecordHashHelper();
        found.setHash(null);
        String computedRecHash = recordHashHelper.computeRecHash(found);

        // Then
        assertEquals(recHash, computedRecHash);
    }
}
