package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.PasswordEnc;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {
    private static final String USER_LOGIN = "vne";
    private static final String USER_PIN = "12345678";
    private static final String USER_EMAIL = "vne@123.de";
    private static final String USER_ID = "QWERTY";
    private static final String LOGIN_TOKEN = "eyJraWQiOiJHY1dnNlpodlNTc2p6SnpjblVzdHRjIiwiYWxnIjoiSFMyNTYifQ.eyJzdWIiOiJjMldVU2p4clIyZ3BRQkQtbzRlZFNrIiwidG9rZW5fdXNhZ2UiOiJMT0dJTiIsInJvbGUiOiJDVVNUT01FUiIsInNjYV9pZCI6IlYwMDIwMTkwNzI1MTEyMTE1YzJXVVNqeHJSMmdwUUJELW80ZWRTayIsImF1dGhvcmlzYXRpb25faWQiOiJWMDAyMDE5MDcyNTExMjExNWMyV1VTanhyUjJncFFCRC1vNGVkU2siLCJleHAiOjE1NjQwNDM0NzUsImxvZ2luIjoiNzc3IiwiaWF0IjoxNTY0MDQyODc1LCJqdGkiOiJqUjVLdkZKQlNid2c3d1VuNi04Tm5jIn0.CzO7rBTosaSgUly9OhiztOX-wfrYi-Mt1K1AvqE7qQM";
    private static final String USER_IBAN = "DE1234567890";
    private static final String TPP_ID = "ZXCVBN";

    @InjectMocks
    AuthorizationServiceImpl authorizationService;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEnc passwordEnc;
    @Mock
    BearerTokenService bearerTokenService;
    @Mock
    private HashMacSecretSource secretSource;

    @Test
    void authorise() {
        // Given
        when(userService.findByLogin(USER_LOGIN)).thenReturn(getUser());
        when(passwordEnc.verify(anyString(), anyString(), anyString())).thenReturn(true);
        when(bearerTokenService.bearerToken(anyString(), anyString(), any(), any(), any(), anyString(), anyString(), any(), any(), any(), any()))
                .thenReturn(new BearerTokenBO());

        // When
        BearerTokenBO result = authorizationService.authorise(USER_LOGIN, USER_PIN, UserRoleBO.CUSTOMER, "TEST_SCA_ID", "TEST_AUTHORIZATION_ID");

        // Then
        assertNotNull(result);
    }

    @Test
    void authorizeWithLoginAndPin() {
        // Given
        when(userService.findByLogin(USER_LOGIN)).thenThrow(UserManagementModuleException.class);

        // Then
        assertThrows(UserManagementModuleException.class, () -> authorizationService.authorise(USER_LOGIN, USER_PIN, UserRoleBO.CUSTOMER, "TEST_SCA_ID", "TEST_AUTHORIZATION_ID"));
    }

    @Test
    void validate() {
        // Given
        when(bearerTokenService.expiresIn(any(), any())).thenReturn(600);
        when(userService.findById(any())).thenReturn(getUser());
        when(bearerTokenService.toAccessTokenObject(any())).thenReturn(getToken());
        when(secretSource.getHmacSecret()).thenReturn("6VFX8YFQG5DLFKZIMNLGH9P406XR1SY4");
        when(bearerTokenService.bearerToken(anyString(), anyInt(), any())).thenReturn(new BearerTokenBO());
        Date date = new Date(123456789);

        // When
        BearerTokenBO result = authorizationService.validate(LOGIN_TOKEN, date);

        // Then
        assertNotNull(result);
    }

    @Test
    void consentToken() {
        // Given
        when(userService.findById(any())).thenReturn(getUser());
        when(bearerTokenService.bearerToken(anyString(), anyString(), any(), any(), any(), anyString(), anyString(), any(), any(), any(), any()))
                .thenReturn(new BearerTokenBO());

        // When
        BearerTokenBO result = authorizationService.consentToken(getScaInfo(), getAisConsent());

        // Then
        assertNotNull(result);
    }

    @Test
    void validateCredentials() {
        // Given
        when(userService.findByLogin(anyString())).thenReturn(getUser());
        when(passwordEnc.verify(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        boolean result = authorizationService.validateCredentials("login", "pin", UserRoleBO.CUSTOMER);

        // Then
        assertTrue(result);
    }

    @Test
    void validateCredentials_wrong_creds() {
        // Given
        when(userService.findByLogin(anyString())).thenReturn(getUser());
        when(passwordEnc.verify(anyString(), anyString(), anyString())).thenReturn(false);

        // When
        boolean result = authorizationService.validateCredentials("login", "pin", UserRoleBO.CUSTOMER);

        // Then
        assertFalse(result);
    }

    @Test
    void validateCredentials_no_role() {
        // Given
        when(userService.findByLogin(anyString())).thenReturn(getUser());
        when(passwordEnc.verify(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        boolean result = authorizationService.validateCredentials("login", "pin", UserRoleBO.STAFF);

        // Then
        assertFalse(result);
    }

    private AisConsentBO getAisConsent() {
        AisConsentBO consentBO = new AisConsentBO();
        AisAccountAccessInfoBO accessInfoBO = new AisAccountAccessInfoBO();
        accessInfoBO.setAccounts(Collections.singletonList(USER_IBAN));
        consentBO.setAccess(accessInfoBO);
        consentBO.setUserId(USER_ID);
        consentBO.setTppId(TPP_ID);
        return consentBO;
    }

    private ScaInfoBO getScaInfo() {
        return new ScaInfoBO(USER_ID, "TEST_SCA_ID", "TEST_AUTH_ID", UserRoleBO.CUSTOMER);
    }

    private UserBO getUser() {
        UserBO user = new UserBO(USER_LOGIN, USER_EMAIL, USER_PIN);
        user.setId(USER_ID);
        user.setUserRoles(Collections.singletonList(UserRoleBO.CUSTOMER));
        user.setAccountAccesses(getAccesses());
        return user;
    }

    private AccessTokenBO getToken() {
        AccessTokenBO tokenBO = new AccessTokenBO();

        tokenBO.setAccountAccesses(getAccesses());
        return tokenBO;
    }

    private List<AccountAccessBO> getAccesses() {
        AccountAccessBO accessBO = new AccountAccessBO(USER_IBAN, AccessTypeBO.OWNER);
        accessBO.setCurrency(Currency.getInstance("EUR"));
        return Collections.singletonList(accessBO);
    }
}
