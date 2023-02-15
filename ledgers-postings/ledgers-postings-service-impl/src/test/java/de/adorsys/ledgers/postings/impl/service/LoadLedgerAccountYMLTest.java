/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.ledgers.postings.api.domain.AccountCategoryBO;
import de.adorsys.ledgers.postings.api.domain.BalanceSideBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoadLedgerAccountYMLTest {
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void before() {
        final YAMLFactory ymlFactory = new YAMLFactory();
        mapper.registerModule(new JavaTimeModule());
        mapper = new ObjectMapper(ymlFactory);
    }

    @Test
    void testReadYml() throws IOException {
        // Given
        InputStream inputStream = LoadLedgerAccountYMLTest.class.getResourceAsStream("LoadLedgerAccountYMLTest.yml");

        // When
        LedgerAccountBO[] ledgerAccounts = mapper.readValue(inputStream, LedgerAccountBO[].class);

        // Then
        assertNotNull(ledgerAccounts);
        assertEquals(2, ledgerAccounts.length);
        assertEquals("1", ledgerAccounts[0].getName());
        assertEquals("Assets", ledgerAccounts[0].getShortDesc());
        assertEquals(AccountCategoryBO.AS, ledgerAccounts[0].getCategory());
        assertEquals(BalanceSideBO.Dr, ledgerAccounts[0].getBalanceSide());
        assertEquals("1.1", ledgerAccounts[1].getName());
        assertEquals("Property, Plant And Equipment", ledgerAccounts[1].getShortDesc());
    }
}
