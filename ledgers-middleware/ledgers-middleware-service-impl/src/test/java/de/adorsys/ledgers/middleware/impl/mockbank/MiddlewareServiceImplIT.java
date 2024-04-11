/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.mockbank;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import de.adorsys.ledgers.keycloak.client.impl.KeycloakDataServiceImpl;
import de.adorsys.ledgers.keycloak.client.impl.KeycloakTokenServiceImpl;
import de.adorsys.ledgers.keycloak.client.mapper.KeycloakAuthMapperImpl;
import de.adorsys.ledgers.keycloak.client.rest.KeycloakTokenRestClient;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.impl.test.MiddlewareServiceApplication;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MiddlewareServiceApplication.class, KeycloakTokenServiceImpl.class, KeycloakTokenRestClient.class, KeycloakAuthMapperImpl.class, KeycloakDataServiceImpl.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@ActiveProfiles("h2")
class MiddlewareServiceImplIT {

    @Autowired
    private AppManagementService appManagementService;

    @Autowired
    private LedgerAccountRepository repo;

    @BeforeEach
    void initDepositAccount() {
        appManagementService.initApp();
    }

    @Test
    void test() {
        // DO nothing. Just run app initialization.
        long count = repo.count();
        assertEquals(26, count);
    }
}
