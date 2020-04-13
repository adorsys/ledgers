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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OauthAuthorisationServiceImplTest {
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
    public void oauthCode_code_present() {
        when(userService.findByLogin(anyString())).thenReturn(new UserBO());
        when(passwordEnc.verify(anyString(), anyString(), anyString())).thenReturn(true);
        OauthConfigurationProperties.OauthLifeTime time = new OauthConfigurationProperties.OauthLifeTime();
        time.setAuthCode(1);
        when(oauthConfigProp.getLifeTime()).thenReturn(time);
        when(oauthCodeRepository.findByUserId(anyString())).thenReturn(Optional.of(new OauthCodeEntity(USER_ID, CODE, OffsetDateTime.MAX)));
        OauthCodeResponseBO result = service.oauthCode(LOGIN, PIN);
        assertThat(result).isNotNull();
    }

    @Test
    public void oauthCode_new_code() {
        when(userService.findByLogin(anyString())).thenReturn(new UserBO());
        when(passwordEnc.verify(anyString(), anyString(), anyString())).thenReturn(true);
        OauthConfigurationProperties.OauthLifeTime time = new OauthConfigurationProperties.OauthLifeTime();
        time.setAuthCode(1);
        when(oauthConfigProp.getLifeTime()).thenReturn(time);
        when(oauthCodeRepository.findByUserId(anyString())).thenReturn(Optional.empty());
        when(oauthCodeRepository.save(any())).then(a -> a.getArgumentAt(0, OauthCodeEntity.class));
        OauthCodeResponseBO result = service.oauthCode(LOGIN, PIN);
        assertThat(result).isNotNull();
    }

    @Test(expected = UserManagementModuleException.class)
    public void oauthCode_not_successful() {
        when(userService.findByLogin(anyString())).thenReturn(new UserBO());
        when(passwordEnc.verify(anyString(), anyString(), anyString())).thenReturn(false);
        service.oauthCode(LOGIN, PIN);
    }

    @Test
    public void testOauthCode() {
        when(userService.findById(anyString())).thenReturn(new UserBO());
        when(passwordEnc.verify(anyString(), anyString(), anyString())).thenReturn(true);
        OauthConfigurationProperties.OauthLifeTime time = new OauthConfigurationProperties.OauthLifeTime();
        time.setAuthCode(1);
        when(oauthConfigProp.getLifeTime()).thenReturn(time);
        when(oauthCodeRepository.findByUserId(anyString())).thenReturn(Optional.of(new OauthCodeEntity(USER_ID, CODE, OffsetDateTime.MAX)));
        OauthCodeResponseBO result = service.oauthCode(USER_ID);
        assertThat(result).isNotNull();
    }

    @Test
    public void oauthToken() {
        OauthCodeEntity codeEntity = new OauthCodeEntity();
        codeEntity.setExpiryTime(OffsetDateTime.MAX);
        when(oauthCodeRepository.findByCodeAndUsed(anyString(), eq(false))).thenReturn(Optional.of(codeEntity));
        when(userService.findById(anyString())).thenReturn(new UserBO());
        OauthConfigurationProperties.OauthLifeTime time = new OauthConfigurationProperties.OauthLifeTime();
        time.setAuthCode(1);
        when(oauthConfigProp.getLifeTime()).thenReturn(time);
        when(bearerTokenService.bearerToken(anyString(), anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new BearerTokenBO());
        OauthTokenResponseBO result = service.oauthToken(CODE);
        assertThat(result).isEqualToComparingFieldByField(new OauthTokenResponseBO(new BearerTokenBO()));
    }

    @Test(expected = UserManagementModuleException.class)
    public void oauthToken_nf() {
        when(oauthCodeRepository.findByCodeAndUsed(anyString(), eq(false))).thenReturn(Optional.empty());
        service.oauthToken(CODE);
    }

    @Test(expected = UserManagementModuleException.class)
    public void oauthToken_expired_operation() {
        OauthCodeEntity codeEntity = new OauthCodeEntity();
        codeEntity.setExpiryTime(OffsetDateTime.MIN);
        when(oauthCodeRepository.findByCodeAndUsed(anyString(), eq(false))).thenReturn(Optional.of(codeEntity));
        service.oauthToken(CODE);
    }

    @Test
    public void oauthServerInfo() {
        Whitebox.setInternalState(service, "oauthConfigProp", getProps());
        OauthServerInfoBO result = service.oauthServerInfo();
        assertThat(result).isEqualTo(getServerInfo());
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