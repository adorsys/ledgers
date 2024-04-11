/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.service.DepositAccountCleanupService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.middleware.api.domain.general.RecoveryPointTO;
import de.adorsys.ledgers.middleware.impl.converter.RecoveryPointMapperTO;
import de.adorsys.ledgers.sca.domain.RecoveryPointBO;
import de.adorsys.ledgers.sca.service.RecoveryPointService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.javatar.commons.reader.JsonReader;
import pro.javatar.commons.reader.ResourceReader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiddlewareRecoveryPointServiceImplTest {

    private static final Long ID = 123L;
    private static final String BRANCH = "branch";
    private static final String USER_ID = "user-id";

    @InjectMocks
    private MiddlewareRecoveryPointServiceImpl service;

    @Mock
    private RecoveryPointService pointService;
    @Mock
    private UserService userService;
    @Mock
    private DepositAccountService depositAccountService;
    @Mock
    private KeycloakDataService keycloakDataService;
    @Mock
    private DepositAccountCleanupService depositAccountCleanupService;
    @Spy
    private RecoveryPointMapperTO recoveryPointMapperTO = Mappers.getMapper(RecoveryPointMapperTO.class);

    private ResourceReader jsonReader = JsonReader.getInstance();

    @Test
    void createRecoveryPoint() throws IOException {
        RecoveryPointTO recoveryPointTO = jsonReader.getObjectFromFile("json/service/recovery-point.json", RecoveryPointTO.class);

        service.createRecoveryPoint(BRANCH, recoveryPointTO);

        verify(pointService, times(1)).createRecoveryPoint(recoveryPointMapperTO.toBO(recoveryPointTO));
    }

    @Test
    void getAll() throws IOException {
        RecoveryPointBO recoveryPointBO = jsonReader.getObjectFromFile("json/service/recovery-point.json", RecoveryPointBO.class);
        when(pointService.getAllByBranch(BRANCH)).thenReturn(Collections.singletonList(recoveryPointBO));

        List<RecoveryPointTO> actual = service.getAll(BRANCH);

        RecoveryPointTO expected = jsonReader.getObjectFromFile("json/service/recovery-point.json", RecoveryPointTO.class);
        assertEquals(Collections.singletonList(expected), actual);
    }

    @Test
    void getPointById() throws IOException {
        RecoveryPointBO recoveryPointBO = jsonReader.getObjectFromFile("json/service/recovery-point.json", RecoveryPointBO.class);
        when(pointService.getById(ID, BRANCH)).thenReturn(recoveryPointBO);

        RecoveryPointTO actual = service.getPointById(BRANCH, ID);

        RecoveryPointTO expected = jsonReader.getObjectFromFile("json/service/recovery-point.json", RecoveryPointTO.class);
        assertEquals(expected, actual);
    }

    @Test
    void deleteById() {
        service.deleteById(BRANCH, ID);
        verify(pointService, times(1)).deleteRecoveryPoint(ID, BRANCH);
    }

    @Test
    void revertDatabase() throws IOException, InterruptedException {
        RecoveryPointBO recoveryPointBO = jsonReader.getObjectFromFile("json/service/recovery-point.json", RecoveryPointBO.class);
        when(pointService.getById(ID, USER_ID)).thenReturn(recoveryPointBO);

        UserBO userBO = jsonReader.getObjectFromFile("json/service/user.json", UserBO.class);
        when(userService.findUsersByBranchAndCreatedAfter(USER_ID, recoveryPointBO.getRollBackTime()))
                .thenReturn(Collections.singletonList(userBO));

        service.revertDatabase(USER_ID, ID);
        Thread.sleep(500);

        verify(userService, times(1)).setBranchBlockedStatus(USER_ID, true, true);
        verify(depositAccountService, times(1)).changeAccountsBlockedStatus(USER_ID, true, true);

        verify(keycloakDataService, times(1)).deleteUser("superman");
        verify(depositAccountCleanupService, times(1)).rollBackBranch(USER_ID, recoveryPointBO.getRollBackTime());
    }
}