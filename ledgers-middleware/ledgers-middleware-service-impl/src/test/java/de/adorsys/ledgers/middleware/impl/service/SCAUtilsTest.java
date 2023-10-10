/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SCAUtilsTest {

    private static final String ID = "id";
    private static final String LOGIN = "login";
    private static final String EMAIL = "email";
    private static final String PIN = "pin";

    @InjectMocks
    private SCAUtils utils;

    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;

    @Test
    void getScaMethod_TO() {
        ScaUserDataTO result = utils.getScaMethod(getUserTO(), ID);
        assertThat(result).isEqualTo(getScaUserDataTO());
    }

    private UserTO getUserTO() {
        return new UserTO(ID, LOGIN, EMAIL, PIN, Collections.singletonList(getScaUserDataTO()), Collections.singletonList(getAccess()), Collections.singletonList(UserRoleTO.CUSTOMER), "branch", false, false);
    }

    private AccountAccessTO getAccess() {
        return new AccountAccessTO(ID, "DE123", Currency.getInstance("EUR"), AccessTypeTO.OWNER, 100, ID);
    }

    private ScaUserDataTO getScaUserDataTO() {
        return new ScaUserDataTO(ID, ScaMethodTypeTO.SMTP_OTP, "anton.brueckner@de.de", new UserTO(LOGIN, EMAIL, PIN), false, "staticTan", false, true);
    }

    @Test
    void getScaMethod_BO_null_sca_method() {
        ScaUserDataTO result = utils.getScaMethod(new UserBO(), null);
        assertThat(result).isNull();
    }

    @Test
    void user() {
        when(userService.findByLogin(anyString())).thenReturn(getUserBO());
        when(userMapper.toUserTO(any())).thenReturn(getUserTO());
        UserTO result = utils.user(ID);
        assertThat(result).isEqualToComparingFieldByFieldRecursively(getUserTO());
    }

    @Test
    void hasSCA() {
        boolean result = utils.hasSCA(getUserBO());
        assertThat(result).isTrue();
    }

    @Test
    void authorisationId() {
        String result = utils.authorisationId(getScaInfo());
        assertThat(result).isEqualTo("authId");
    }

    private UserBO getUserBO() {
        UserBO bo = new UserBO(LOGIN, EMAIL, PIN);
        bo.setScaUserData(Collections.singletonList(new ScaUserDataBO(ScaMethodTypeBO.SMTP_OTP, "anton.brueckner@de.de")));
        return bo;
    }

    private ScaInfoTO getScaInfo() {
        return new ScaInfoTO("userId", "scaId", "authId", UserRoleTO.CUSTOMER, "methodId", "authCode", TokenUsageTO.LOGIN, LOGIN, null, null);
    }
}