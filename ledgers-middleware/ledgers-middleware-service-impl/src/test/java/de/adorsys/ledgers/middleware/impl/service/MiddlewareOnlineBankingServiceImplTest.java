package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.ScaInfoMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.domain.ScaValidationBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.AuthorizationService;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiddlewareOnlineBankingServiceImplTest {
    private static final String USER_ID = "userId";
    private static final String CONSENT_ID = "consentId";
    private static final String AUTHORIZATION_ID = "authorizationId";
    private static final String AUTH_CODE = "123456";
    private static final String SCA_METHOD_ID = "scaMethodId";
    private static final String SCA_ID = "scaId";
    private static final String USER_PIN = "12345";
    private static final String USER_LOGIN = "userLogin";
    private static final UserRoleTO USER_ROLE_TO_CUSTOMER = UserRoleTO.CUSTOMER;
    private static final String USER_EMAIL = "userEmail";
    private static final String ACCESS_TOKEN = "accessToken";

    @InjectMocks
    private MiddlewareOnlineBankingServiceImpl onlineBankingService;
    @Mock
    private UserService userService;
    @Mock
    private BearerTokenMapper bearerTokenMapper;
    @Mock
    private SCAOperationService scaOperationService;
    @Mock
    private SCAUtils scaUtils;
    @Mock
    private ScaInfoMapper scaInfoMapper;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private UserMapper userMapper;

    @Test
    void authorise() {
        // Given
        when(userService.findByLogin(any())).thenReturn(getUserBO());
        when(authorizationService.authorise(any(), any(), any(), any(), any())).thenReturn(getBearerTokenBO());
        when(authorizationService.scaToken(any())).thenReturn(getBearerTokenBO());
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(getBearerTokenTO());

        // When
        SCALoginResponseTO response = onlineBankingService.authorise(USER_LOGIN, USER_PIN, USER_ROLE_TO_CUSTOMER);

        // Then
        assertThat(response).isNotNull();
        assertEquals(getBearerTokenTO(), response.getBearerToken());
        verify(bearerTokenMapper, times(1)).toBearerTokenTO(getBearerTokenBO());
    }

    @Test
    void authorise_unknownCredentials() {
        // Given
        when(userService.findByLogin(any())).thenReturn(getUserBO());

        // Then
        assertThrows(MiddlewareModuleException.class, () -> onlineBankingService.authorise(USER_LOGIN, USER_PIN, USER_ROLE_TO_CUSTOMER));
    }

    @Test
    void authoriseForConsent() {
        // Given
        when(userService.findByLogin(any())).thenReturn(getUserBO());
        when(scaOperationService.checkIfExistsOrNew(any())).thenReturn(getSCAOperationBO(ScaStatusBO.EXEMPTED));
        when(authorizationService.authorise(any(), any(), any(), any(), any())).thenReturn(getBearerTokenBO());
        when(scaUtils.hasSCA(any())).thenReturn(false);
        when(authorizationService.scaToken(any())).thenReturn(getBearerTokenBO());
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(getBearerTokenTO());

        // When
        SCALoginResponseTO response = onlineBankingService.authoriseForConsent(USER_LOGIN, USER_PIN, CONSENT_ID, AUTHORIZATION_ID, OpTypeTO.CONSENT);

        // Then
        assertThat(response).isNotNull();
        assertEquals(getBearerTokenTO(), response.getBearerToken());
        verify(bearerTokenMapper, times(1)).toBearerTokenTO(getBearerTokenBO());
    }

    @Test
    void authoriseForConsent_failed_authorization() {
        //given
        when(userService.findByLogin(any())).thenReturn(getUserBO());
        when(scaOperationService.checkIfExistsOrNew(any())).thenReturn(getSCAOperationBO(ScaStatusBO.FAILED));

        // Then
        assertThrows(MiddlewareModuleException.class, () -> {
            SCALoginResponseTO response = onlineBankingService.authoriseForConsent(USER_LOGIN, USER_PIN, CONSENT_ID, AUTHORIZATION_ID, OpTypeTO.CONSENT);
            assertThat(response).isNotNull();
            assertEquals(getBearerTokenTO(), response.getBearerToken());
            verify(bearerTokenMapper, times(1)).toBearerTokenTO(getBearerTokenBO());
        });
    }

    @Test
    void authoriseForConsent_failed_cred_validation() {
        //given
        when(userService.findByLogin(any())).thenReturn(getUserBO());
        when(scaOperationService.checkIfExistsOrNew(any())).thenReturn(getSCAOperationBO(ScaStatusBO.EXEMPTED));
        when(authorizationService.authorise(any(), any(), any(), any(), any())).thenReturn(null);

        // Then
        assertThrows(MiddlewareModuleException.class, () -> {
            SCALoginResponseTO response = onlineBankingService.authoriseForConsent(USER_LOGIN, USER_PIN, CONSENT_ID, AUTHORIZATION_ID, OpTypeTO.CONSENT);

            //then
            assertThat(response).isNotNull();
            assertEquals(getBearerTokenTO(), response.getBearerToken());
            verify(bearerTokenMapper, times(1)).toBearerTokenTO(getBearerTokenBO());
        });
    }

    @Test
    void authoriseForConsentWithToken() {
        // Given
        when(userService.findByLogin(any())).thenReturn(getUserBO());
        when(scaInfoMapper.toScaInfoBO(any())).thenReturn(buildScaInfoBO());
        when(authorizationService.authorizeNewAuthorizationId(any(), any())).thenReturn(getBearerTokenBO());
        when(scaUtils.hasSCA(any())).thenReturn(true);
        when(scaOperationService.createAuthCode(any(), any())).thenReturn(getSCAOperationBO(ScaStatusBO.EXEMPTED));
        when(scaUtils.user((UserBO) any())).thenReturn(getUserTO());
        when(authorizationService.scaToken(any())).thenReturn(getBearerTokenBO());
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(getBearerTokenTO());

        // When
        SCALoginResponseTO response = onlineBankingService.authoriseForConsentWithToken(buildScaInfoTO(), CONSENT_ID, AUTHORIZATION_ID, OpTypeTO.CONSENT);

        // Then
        assertThat(response).isNotNull();
        assertEquals(getBearerTokenTO(), response.getBearerToken());
        verify(bearerTokenMapper, times(1)).toBearerTokenTO(getBearerTokenBO());
        verify(scaInfoMapper, times(1)).toScaInfoBO(buildScaInfoTO());
    }

    @Test
    void authoriseForConsentWithToken_unknownCredentials() {
        // Given
        when(userService.findByLogin(any())).thenReturn(getUserBO());

        // Then
        assertThrows(MiddlewareModuleException.class, () -> onlineBankingService.authoriseForConsentWithToken(buildScaInfoTO(), CONSENT_ID, AUTHORIZATION_ID, OpTypeTO.CONSENT));
    }

    @Test
    void validate() {
        // Given
        when(authorizationService.validate(any(), any())).thenReturn(getBearerTokenBO());
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(getBearerTokenTO());

        // When
        BearerTokenTO response = onlineBankingService.validate(ACCESS_TOKEN);

        // Then
        assertThat(response).isNotNull();
        assertEquals(getBearerTokenTO(), response);
        verify(bearerTokenMapper, times(1)).toBearerTokenTO(getBearerTokenBO());
    }

    @Test
    void register() {
        // Given
        when(userService.create(any())).thenReturn(getUserBO());

        // When
        UserTO response = onlineBankingService.register(USER_LOGIN, USER_EMAIL, USER_PIN, USER_ROLE_TO_CUSTOMER);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isEqualToComparingFieldByFieldRecursively(getUserTO());
    }

    @Test
    void generateLoginAuthCode() {
        // Given
        when(scaUtils.userBO(any())).thenReturn(getUserBO());
        when(scaOperationService.loadAuthCode(any())).thenReturn(getSCAOperationBO(ScaStatusBO.EXEMPTED));
        when(scaOperationService.generateAuthCode(any(), any(), any())).thenReturn(getSCAOperationBO(ScaStatusBO.EXEMPTED));
        when(scaUtils.user((UserBO) any())).thenReturn(getUserTO());
        when(scaInfoMapper.toScaInfoBO(any())).thenReturn(buildScaInfoBO());
        when(authorizationService.loginToken(any())).thenReturn(getBearerTokenBO());
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(getBearerTokenTO());

        // When
        SCALoginResponseTO response = onlineBankingService.generateLoginAuthCode(buildScaInfoTO(), "userMessage", 6200);

        // Then
        assertThat(response).isNotNull();
        assertEquals(getBearerTokenTO(), response.getBearerToken());
    }

    @Test
    void authenticateForLogin() {
        // Given
        when(scaUtils.userBO(any())).thenReturn(getUserBO());
        when(scaOperationService.loadAuthCode(any())).thenReturn(getSCAOperationBO(ScaStatusBO.EXEMPTED));
        when(scaOperationService.validateAuthCode(any(), any(), any(), any(), anyInt())).thenReturn(getScaValidationBO());
        when(scaUtils.user((UserBO) any())).thenReturn(getUserTO());
        when(scaInfoMapper.toScaInfoBO(any())).thenReturn(buildScaInfoBO());
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(getBearerTokenTO());

        // When
        SCALoginResponseTO response = onlineBankingService.authenticateForLogin(buildScaInfoTO());

        // Then
        assertThat(response).isNotNull();
        assertEquals(getBearerTokenTO(), response.getBearerToken());
    }

    @Test
    void authorizeForUser() {
        // Given
        when(authorizationService.validateCredentials(anyString(), anyString(), any())).thenReturn(true);
        when(userService.findByLogin(anyString())).thenReturn(new UserBO("anton.brueckner", null, null));
        when(authorizationService.scaToken(any())).thenReturn(getBearerTokenBO());
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(new BearerTokenTO());

        // When
        SCALoginResponseTO response = onlineBankingService.authorizeForUser("admin", "admin", "anton.brueckner");

        // Then
        assertNotNull(response.getBearerToken());
    }

    @Test
    void authorizeForUser_fail() {
        // Given
        when(authorizationService.validateCredentials(anyString(), anyString(), any())).thenReturn(false);

        // Then
        assertThrows(MiddlewareModuleException.class, () -> onlineBankingService.authorizeForUser("admin", "admin", "anton.brueckner"));
    }

    private UserBO getUserBO() {
        UserBO user = new UserBO();
        user.setId(USER_ID);
        user.setLogin(USER_LOGIN);
        user.setEmail(USER_EMAIL);
        user.setPin(USER_PIN);
        return user;
    }

    private UserTO getUserTO() {
        UserTO user = new UserTO();
        user.setId(USER_ID);
        user.setLogin(USER_LOGIN);
        user.setEmail(USER_EMAIL);
        user.setPin(USER_PIN);
        return user;
    }

    private BearerTokenBO getBearerTokenBO() {
        BearerTokenBO token = new BearerTokenBO();
        token.setAccess_token("access_token");
        token.setAccessTokenObject(getAccessTokenBO());
        token.setExpires_in(6200);
        token.setRefresh_token("refresh_token");
        token.setToken_type("Bearer");
        return token;
    }

    private BearerTokenTO getBearerTokenTO() {
        return new BearerTokenTO("access_token", "Bearer", 6200, "refresh_token", new AccessTokenTO());
    }

    private AccessTokenBO getAccessTokenBO() {
        AccessTokenBO token = new AccessTokenBO();
        token.setScaId(SCA_ID);
        token.setAuthorisationId("authorizationId");
        return token;
    }

    private ScaInfoBO buildScaInfoBO() {
        ScaInfoBO info = new ScaInfoBO();
        info.setUserId(USER_ID);
        info.setAuthorisationId(AUTHORIZATION_ID);
        info.setScaId(SCA_ID);
        info.setUserRole(UserRoleBO.CUSTOMER);
        info.setAuthCode(AUTH_CODE);
        info.setScaMethodId(SCA_METHOD_ID);
        info.setUserLogin(USER_LOGIN);
        return info;
    }

    private ScaInfoTO buildScaInfoTO() {
        ScaInfoTO info = new ScaInfoTO();
        info.setUserId(USER_ID);
        info.setAuthorisationId(AUTHORIZATION_ID);
        info.setScaId(SCA_ID);
        info.setUserRole(UserRoleTO.CUSTOMER);
        info.setAuthCode(AUTH_CODE);
        info.setScaMethodId(SCA_METHOD_ID);
        info.setUserLogin(USER_LOGIN);
        return info;
    }

    private SCAOperationBO getSCAOperationBO(ScaStatusBO status) {
        SCAOperationBO scaOperation = new SCAOperationBO();
        scaOperation.setId("id");
        scaOperation.setScaMethodId(SCA_METHOD_ID);
        scaOperation.setOpId("V0020200302130357q2tRswcRSr4nXqUaJCXpkQ");
        scaOperation.setScaStatus(status);
        scaOperation.setStatusTime(LocalDateTime.now());
        scaOperation.setValiditySeconds(6200);
        return scaOperation;
    }

    private ScaValidationBO getScaValidationBO() {
        return new ScaValidationBO(AUTH_CODE, true, ScaStatusBO.FINALISED, 0);
    }
}