package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.domain.ScaValidationBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SCAUtilsTest {
    @InjectMocks
    private SCAUtils utils;

    @Mock
    private UserService userService;
    @Mock
    private SCAOperationService scaOperationService;
    @Mock
    private UserMapper userMapper;

    @Test
    void getScaMethod_TO() {
        ScaUserDataTO result = utils.getScaMethod(getUserTO(), "id");
        assertThat(result).isEqualTo(getScaUserDataTO());
    }

    private UserTO getUserTO() {
        return new UserTO("id", "login", "email", "pin", Collections.singletonList(getScaUserDataTO()), Collections.singletonList(getAccess()), Collections.singletonList(UserRoleTO.CUSTOMER), "branch");
    }

    private AccountAccessTO getAccess() {
        return new AccountAccessTO("id", "DE123", Currency.getInstance("EUR"), AccessTypeTO.OWNER, 100, "id");
    }

    private ScaUserDataTO getScaUserDataTO() {
        return new ScaUserDataTO("id", ScaMethodTypeTO.EMAIL, "anton.brueckner@de.de", null, false, "staticTan", false, true);
    }

    @Test
    public void getScaMethod_BO_null_sca_method() {
        ScaUserDataTO result = utils.getScaMethod(new UserBO(), null);
        assertThat(result).isNull();
    }

    @Test
    public void user() {
        when(userService.findById(anyString())).thenReturn(new UserBO());
        when(userMapper.toUserTO(any())).thenReturn(getUserTO());
        UserTO result = utils.user("id");
        assertThat(result).isEqualToComparingFieldByFieldRecursively(getUserTO());
    }

    @Test
    void hasSCA() {
        boolean result = utils.hasSCA(getUserBO());
        assertThat(result).isTrue();
    }

    private UserBO getUserBO() {
        UserBO bo = new UserBO("login", "email", "pin");
        bo.setScaUserData(Collections.singletonList(new ScaUserDataBO()));
        return bo;
    }

    @Test
    void authorisationId() {
        String result = utils.authorisationId(getScaInfo());
        assertThat(result).isEqualTo("authId");
    }

    private ScaInfoTO getScaInfo() {
        return new ScaInfoTO("userId", "scaId", "authId", UserRoleTO.CUSTOMER, "methodId", "authCode", TokenUsageTO.LOGIN, "login");

    }

    @Test
    void checkScaResult() {
        utils.checkScaResult(new ScaValidationBO("code", true, ScaStatusBO.RECEIVED, 3));
    }

    @Test
    void checkScaResult_fail() {
        assertThrows(MiddlewareModuleException.class, () -> utils.checkScaResult(new ScaValidationBO("code", false, ScaStatusBO.RECEIVED, 3)));
    }
}