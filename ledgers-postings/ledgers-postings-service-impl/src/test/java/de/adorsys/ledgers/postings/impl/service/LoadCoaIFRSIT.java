/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adorsys.ledgers.postings.api.domain.AccountCategoryBO;
import de.adorsys.ledgers.postings.api.domain.BalanceSideBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.impl.test.PostingsApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLoadCoaIFRSTest-db-entries.xml")
@DatabaseTearDown(value = {"ITLoadCoaIFRSTest-db-delete.xml"}, type = DatabaseOperation.DELETE_ALL)
class LoadCoaIFRSIT {
    private static final String SYSTEM = "System";
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private LedgerService ledgerService;

    @BeforeEach
    void before() {
        final YAMLFactory ymlFactory = new YAMLFactory();
        mapper = new ObjectMapper(ymlFactory);
    }

    @Test
    void test_load_coa_ok() throws IOException {
        // Given
        LedgerBO ledgerBO = ledgerService.findLedgerById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
        assumeTrue(ledgerBO != null);
        InputStream inputStream = LoadLedgerAccountYMLTest.class.getResourceAsStream("ITLoadCoaIFRSTest-coa.yml");
        LedgerAccountBO[] ledgerAccounts = mapper.readValue(inputStream, LedgerAccountBO[].class);
        for (LedgerAccountBO ledgerAccount : ledgerAccounts) {

            if (ledgerAccount.getName() == null)
                fail("Missing account name for " + ledgerAccount.getShortDesc());
            String name = ledgerAccount.getName();

            LedgerAccountBO parent = null;
            if (name.contains(".")) {
                String parentName = name.substring(0, name.lastIndexOf('.'));
                parent = new LedgerAccountBO();
                parent.setLedger(ledgerBO);
                parent.setName(parentName);
            }
            ledgerAccount.setLedger(ledgerBO);
            ledgerAccount.setParent(parent);
            ledgerService.newLedgerAccount(ledgerAccount, SYSTEM);
        }

        // When
        LedgerAccountBO la = ledgerService.findLedgerAccount(ledgerBO, "2.3");

        // Then
        assumeTrue(la != null);
        assertEquals("Other Reserves (Accumulated Other Comprehensive Income)", la.getShortDesc());
        assertEquals(AccountCategoryBO.EQ, la.getCategory());
        assertEquals(BalanceSideBO.DrCr, la.getBalanceSide());
    }

}
