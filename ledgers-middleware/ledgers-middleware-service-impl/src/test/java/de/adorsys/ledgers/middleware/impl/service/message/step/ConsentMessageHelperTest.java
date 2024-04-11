/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.AisAccountAccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConsentMessageHelperTest {

    private ConsentMessageHelper helper;
    private AisConsentBO consent;

    @BeforeEach
    void setUp() {
        consent = new AisConsentBO();
        consent.setTppId("tpp-id");
        helper = new ConsentMessageHelper(consent);
    }

    @Test
    void template_accessIsNull() {
        String message = helper.template();
        assertEquals("No account access to tpp with id: tpp-id", message);
    }

    @Test
    void template_prepareTemplate1() {
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        access.setAllPsd2(AisAccountAccessTypeBO.ALL_ACCOUNTS);
        access.setAvailableAccounts(AisAccountAccessTypeBO.ALL_ACCOUNTS_WITH_BALANCES);
        access.setAccounts(List.of("DE91100000000123456781"));
        access.setBalances(List.of("DE91100000000123456782"));
        access.setTransactions(List.of("DE91100000000123456783"));
        consent.setAccess(access);
        consent.setFrequencyPerDay(2);

        String message = helper.template();
        assertEquals("Account access for TPP with id tpp-id:\n" +
                             "- Up to 2 accesses per day.\n" +
                             "Access to following accounts:\n" +
                             "All payments accounts without balances.\n" +
                             "All available accounts with balances and transactions.\n" +
                             "Without balances: DE91100000000123456781.\n" +
                             "With balances: DE91100000000123456782.\n" +
                             "With balances and transactions: DE91100000000123456783.\n", message);
    }

    @Test
    void template_prepareTemplate2() {
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        access.setAllPsd2(AisAccountAccessTypeBO.ALL_ACCOUNTS_WITH_BALANCES);
        access.setAvailableAccounts(AisAccountAccessTypeBO.ALL_ACCOUNTS);
        access.setAccounts(List.of("DE91100000000123456781"));
        access.setBalances(List.of("DE91100000000123456782"));
        access.setTransactions(List.of("DE91100000000123456783"));
        consent.setAccess(access);
        consent.setRecurringIndicator(true);
        consent.setValidUntil(LocalDate.of(2021, 5, 26));

        String message = helper.template();
        assertEquals("Account access for TPP with id tpp-id:\n" +
                             "- Up to 1 access per day.\n" +
                             "- Access valid until 26 May 2021.\n" +
                             "Access to following accounts:\n" +
                             "All payments accounts with balances and transactions.\n" +
                             "All available accounts without balances.\n" +
                             "Without balances: DE91100000000123456781.\n" +
                             "With balances: DE91100000000123456782.\n" +
                             "With balances and transactions: DE91100000000123456783.\n", message);
    }

    @Test
    void template_prepareTemplate3() {
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        access.setAvailableAccounts(AisAccountAccessTypeBO.ALL_ACCOUNTS);
        access.setAccounts(List.of("DE91100000000123456781"));
        consent.setAccess(access);
        consent.setRecurringIndicator(false);
        consent.setFrequencyPerDay(1);

        String message = helper.template();
        assertEquals("Account access for TPP with id tpp-id:\n" +
                             "- For one time access.\n" +
                             "Access to following accounts:\n" +
                             "All available accounts without balances.\n" +
                             "Without balances: DE91100000000123456781.\n", message);
    }

    @Test
    void checkNullConsentError() {
        helper = new ConsentMessageHelper(null);
        assertThrows(IllegalStateException.class, () -> helper.template());
        assertThrows(IllegalStateException.class, () -> helper.exemptedTemplate());
    }

    @Test
    void checkNullTppIdError() {
        consent.setTppId(null);
        assertThrows(IllegalStateException.class, () -> helper.template());
        assertThrows(IllegalStateException.class, () -> helper.exemptedTemplate());
    }

    @Test
    void exemptedTemplate_accessIsNull() {
        String message = helper.exemptedTemplate();
        assertEquals("No account access to tpp with id: tpp-id", message);
    }

    @Test
    void exemptedTemplate_prepareTemplate1() {
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        access.setAllPsd2(AisAccountAccessTypeBO.ALL_ACCOUNTS);
        access.setAvailableAccounts(AisAccountAccessTypeBO.ALL_ACCOUNTS_WITH_BALANCES);
        access.setAccounts(List.of("DE91100000000123456781"));
        access.setBalances(List.of("DE91100000000123456782"));
        access.setTransactions(List.of("DE91100000000123456783"));
        consent.setAccess(access);
        consent.setFrequencyPerDay(2);

        String message = helper.exemptedTemplate();
        assertEquals("Account access for TPP with id tpp-id:\n" +
                             "- Up to 2 accesses per day.\n" +
                             "Access to following accounts:\n" +
                             "All payments accounts without balances.\n" +
                             "All available accounts with balances and transactions.\n" +
                             "Without balances: DE91100000000123456781.\n" +
                             "With balances: DE91100000000123456782.\n" +
                             "With balances and transactions: DE91100000000123456783.\n" +
                             "This access has been granted. No TAN entry needed.", message);
    }

    @Test
    void exemptedTemplate_prepareTemplate2() {
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        access.setAllPsd2(AisAccountAccessTypeBO.ALL_ACCOUNTS_WITH_BALANCES);
        access.setAvailableAccounts(AisAccountAccessTypeBO.ALL_ACCOUNTS);
        access.setAccounts(List.of("DE91100000000123456781"));
        access.setBalances(List.of("DE91100000000123456782"));
        access.setTransactions(List.of("DE91100000000123456783"));
        consent.setAccess(access);
        consent.setRecurringIndicator(true);
        consent.setValidUntil(LocalDate.of(2021, 5, 26));

        String message = helper.exemptedTemplate();
        assertEquals("Account access for TPP with id tpp-id:\n" +
                             "- Up to 1 access per day.\n" +
                             "- Access valid until 26 May 2021.\n" +
                             "Access to following accounts:\n" +
                             "All payments accounts with balances and transactions.\n" +
                             "All available accounts without balances.\n" +
                             "Without balances: DE91100000000123456781.\n" +
                             "With balances: DE91100000000123456782.\n" +
                             "With balances and transactions: DE91100000000123456783.\n" +
                             "This access has been granted. No TAN entry needed.", message);
    }

    @Test
    void exemptedTemplate_prepareTemplate3() {
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        access.setAvailableAccounts(AisAccountAccessTypeBO.ALL_ACCOUNTS);
        access.setAccounts(List.of("DE91100000000123456781"));
        consent.setAccess(access);
        consent.setRecurringIndicator(false);
        consent.setFrequencyPerDay(1);

        String message = helper.exemptedTemplate();
        assertEquals("Account access for TPP with id tpp-id:\n" +
                             "- For one time access.\n" +
                             "Access to following accounts:\n" +
                             "All available accounts without balances.\n" +
                             "Without balances: DE91100000000123456781.\n" +
                             "This access has been granted. No TAN entry needed.", message);
    }
}