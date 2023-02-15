/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.db.repository;

import de.adorsys.ledgers.um.db.domain.AisConsentEntity;
import de.adorsys.ledgers.um.db.test.UmRepositoryApplication;
import de.adorsys.ledgers.util.Ids;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UmRepositoryApplication.class)
class AisConsentRepositoryIT {

    @Autowired
    private AisConsentRepository repo;

    @Test
    void test_create_consent() {
        // Given
        AisConsentEntity bo = new AisConsentEntity();
//		AisAccountAccessInfo access = new AisAccountAccessInfo();
//		bo.setAccess(access);
        List<String> list = Collections.singletonList("DE80760700240271232400");
        bo.setAccounts(list);
        bo.setTransactions(list);
        bo.setBalances(list);
        bo.setFrequencyPerDay(4);
        bo.setId(Ids.id());
        bo.setRecurringIndicator(true);
        bo.setTppId(Ids.id());
        bo.setUserId(Ids.id());
        bo.setValidUntil(LocalDate.now());

        // When
        AisConsentEntity save = repo.save(bo);

        // Then
        Assertions.assertNotNull(save.getAccounts());
        Assertions.assertEquals(1, save.getAccounts().size());
    }
}
