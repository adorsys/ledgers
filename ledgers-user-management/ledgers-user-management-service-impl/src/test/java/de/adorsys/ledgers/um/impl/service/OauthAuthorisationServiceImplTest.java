package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.oauth.*;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.db.domain.OauthCodeEntity;
import de.adorsys.ledgers.um.db.repository.OauthCodeRepository;
import de.adorsys.ledgers.um.impl.service.config.OauthConfigurationProperties;
import de.adorsys.ledgers.util.PasswordEnc;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OauthAuthorisationServiceImplTest {
    private static final String LOGIN = "login";
    private static final String PIN = "pin";
    private static final String USER_ID = "user id";
    private static final String CODE = "123456";
    private static final String AUTH_ENDP = "http://auth/";
    private static final String TOKEN_ENDP = "http://token/";

    @InjectMocks
    private OauthAuthorisationServiceImpl service;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEnc passwordEnc;
    @Mock
    private OauthCodeRepository oauthCodeRepository;
    @Mock
    private BearerTokenService bearerTokenService;
    @Mock
    private OauthConfigurationProperties oauthConfigProp;

    @Test
    void oauthCode_code_present() {
        // Given
        when(userService.findByLogin(anyString())).thenReturn(new UserBO());
        when(passwordEnc.verify(null, PIN, null)).thenReturn(true);
        OauthConfigurationProperties.OauthLifeTime time = new OauthConfigurationProperties.OauthLifeTime();
        time.setAuthCode(1);
        when(oauthConfigProp.getLifeTime()).thenReturn(time);
        when(oauthCodeRepository.findByUserId(null)).thenReturn(Optional.of(new OauthCodeEntity(USER_ID, CODE, OffsetDateTime.MAX)));

        // When
        OauthCodeResponseBO result = service.oauthCode(LOGIN, PIN);

        // Then
        assertNotNull(result);
    }

    @Test
    void oauthCode_new_code() {
        // Given
        when(userService.findByLogin(anyString())).thenReturn(new UserBO());
        when(passwordEnc.verify(null, PIN, null)).thenReturn(true);
        OauthConfigurationProperties.OauthLifeTime time = new OauthConfigurationProperties.OauthLifeTime();
        time.setAuthCode(1);
        when(oauthConfigProp.getLifeTime()).thenReturn(time);
        when(oauthCodeRepository.findByUserId(null)).thenReturn(Optional.empty());
        when(oauthCodeRepository.save(any())).then(a -> a.getArgument(0));

        // When
        OauthCodeResponseBO result = service.oauthCode(LOGIN, PIN);

        // Then
        assertNotNull(result);
    }

    @Test
    void oauthCode_not_successful() {
        // Given
        when(userService.findByLogin(anyString())).thenReturn(new UserBO());
        when(passwordEnc.verify(null, PIN, null)).thenReturn(false);

        // Then
        assertThrows(UserManagementModuleException.class, () -> service.oauthCode(LOGIN, PIN));
    }

    @Test
    void testOauthCode() {
        // Given
        when(userService.findById(anyString())).thenReturn(new UserBO());
        OauthConfigurationProperties.OauthLifeTime time = new OauthConfigurationProperties.OauthLifeTime();
        time.setAuthCode(1);
        when(oauthConfigProp.getLifeTime()).thenReturn(time);
        when(oauthCodeRepository.findByUserId(null)).thenReturn(Optional.of(new OauthCodeEntity(USER_ID, CODE, OffsetDateTime.MAX)));

        // When
        OauthCodeResponseBO result = service.oauthCode(USER_ID);

        // Then
        assertNotNull(result);
    }

    @Test
    void oauthToken() {
        // Given
        OauthCodeEntity codeEntity = new OauthCodeEntity();
        codeEntity.setExpiryTime(OffsetDateTime.MAX);
        when(oauthCodeRepository.findByCodeAndUsed(anyString(), eq(false))).thenReturn(Optional.of(codeEntity));
        when(userService.findById(null)).thenReturn(new UserBO());
        OauthConfigurationProperties.OauthLifeTime time = new OauthConfigurationProperties.OauthLifeTime();
        time.setAuthCode(1);
        when(oauthConfigProp.getLifeTime()).thenReturn(time);
        when(bearerTokenService.bearerToken(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new BearerTokenBO());

        // When
        OauthTokenResponseBO result = service.oauthToken(CODE);

        // Then
        assertEquals(new OauthTokenResponseBO(new BearerTokenBO()), result);
    }

    @Test
    void oauthToken_nf() {
        // Given
        when(oauthCodeRepository.findByCodeAndUsed(anyString(), eq(false))).thenReturn(Optional.empty());

        // Then
        assertThrows(UserManagementModuleException.class, () -> service.oauthToken(CODE));
    }

    @Test
    void oauthToken_expired_operation() {
        // Given
        OauthCodeEntity codeEntity = new OauthCodeEntity();
        codeEntity.setExpiryTime(OffsetDateTime.MIN);
        when(oauthCodeRepository.findByCodeAndUsed(anyString(), eq(false))).thenReturn(Optional.of(codeEntity));

        // Then
        assertThrows(UserManagementModuleException.class, () -> service.oauthToken(CODE));
    }

    @Test
    void oauthServerInfo() throws NoSuchFieldException {
        FieldSetter.setField(service, service.getClass().getDeclaredField("oauthConfigProp"), getProps());
        OauthServerInfoBO result = service.oauthServerInfo();

        // Then
        assertEquals(getServerInfo(), result);
    }

    private OauthConfigurationProperties getProps() {
        OauthConfigurationProperties props = new OauthConfigurationProperties();
        props.setAuthorizationEndpoint(AUTH_ENDP);
        props.setGrantTypesSupported(Collections.singletonList(GrantTypeBO.AUTHORISATION_CODE));
        props.setResponseTypesSupported(Collections.singletonList(ResponseTypeBO.CODE));
        props.setTokenEndpoint(TOKEN_ENDP);
        return props;
    }

    private OauthServerInfoBO getServerInfo() {
        return new OauthServerInfoBO(AUTH_ENDP, TOKEN_ENDP, Collections.singletonList(ResponseTypeBO.CODE), Collections.singletonList(GrantTypeBO.AUTHORISATION_CODE));
    }
}