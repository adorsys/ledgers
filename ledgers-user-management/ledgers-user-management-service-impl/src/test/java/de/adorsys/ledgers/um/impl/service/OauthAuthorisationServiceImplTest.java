package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.oauth.*;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.db.domain.OauthCodeEntity;
import de.adorsys.ledgers.um.db.repository.OauthCodeRepository;
import de.adorsys.ledgers.um.impl.service.config.OauthConfigurationProperties;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    private OauthCodeRepository oauthCodeRepository;
    @Mock
    private OauthConfigurationProperties oauthConfigProp;

    @Test
    void oauthCode_code_present() {
        // Given
        when(userService.findById(anyString())).thenReturn(new UserBO());
        OauthConfigurationProperties.OauthLifeTime time = new OauthConfigurationProperties.OauthLifeTime();
        time.setAuthCode(1);
        when(oauthConfigProp.getLifeTime()).thenReturn(time);
        when(oauthCodeRepository.findByUserId(null)).thenReturn(Optional.of(new OauthCodeEntity(USER_ID, CODE, OffsetDateTime.MAX, "token", true)));

        // When
        OauthCodeResponseBO result = service.oauthCode(LOGIN, PIN, false);

        // Then
        assertNotNull(result);
    }

    @Test
    void oauthCode_new_code() {
        // Given
        when(userService.findById(anyString())).thenReturn(new UserBO());
        OauthConfigurationProperties.OauthLifeTime time = new OauthConfigurationProperties.OauthLifeTime();
        time.setAuthCode(1);
        when(oauthConfigProp.getLifeTime()).thenReturn(time);
        when(oauthCodeRepository.findByUserId(null)).thenReturn(Optional.empty());
        when(oauthCodeRepository.save(any())).then(a -> a.getArgument(0));

        // When
        OauthCodeResponseBO result = service.oauthCode(LOGIN, PIN, false);

        // Then
        assertNotNull(result);
    }

    @Test
    void testOauthCode() {
        // Given
        when(userService.findById(anyString())).thenReturn(new UserBO());
        OauthConfigurationProperties.OauthLifeTime time = new OauthConfigurationProperties.OauthLifeTime();
        time.setAuthCode(1);
        when(oauthConfigProp.getLifeTime()).thenReturn(time);
        when(oauthCodeRepository.findByUserId(null)).thenReturn(Optional.of(new OauthCodeEntity(USER_ID, CODE, OffsetDateTime.MAX, "token", true)));

        // When
        OauthCodeResponseBO result = service.oauthCode(USER_ID, "token", true);

        // Then
        assertNotNull(result);
    }

    @Test
    void oauthToken() {
        // Given
        OauthCodeEntity codeEntity = new OauthCodeEntity();
        codeEntity.setExpiryTime(OffsetDateTime.MAX);
        codeEntity.setToken("token");
        when(oauthCodeRepository.findByCodeAndUsed(anyString(), eq(false))).thenReturn(Optional.of(codeEntity));

        // When
        OauthTokenHolder result = service.oauthToken(CODE);

        // Then
        assertEquals(new OauthTokenHolder("token", false), result);
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
    void oauthServerInfo() {
        ReflectionTestUtils.setField(service, "oauthConfigProp", getProps());
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