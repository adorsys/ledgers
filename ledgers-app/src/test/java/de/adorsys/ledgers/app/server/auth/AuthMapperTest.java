package de.adorsys.ledgers.app.server.auth;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.AccessToken;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class AuthMapperTest {

    private static final String SUBJECT = "Token subject";
    private static final String PREFERRED_USERNAME = "anton.brueckner";

    @InjectMocks
    private AuthMapperImpl mapper;

    @Test
    void toAccessToken_ledgersRolesPresent() {
        // When
        AccessTokenTO accessToken = mapper.toAccessToken(getAccessToken(Arrays.asList("CUSTOMER", "KING", "CLOWN")));

        // Then
        assertEquals(UserRoleTO.CUSTOMER, accessToken.getRole());
        assertEquals(PREFERRED_USERNAME, accessToken.getLogin());
        assertEquals(SUBJECT, accessToken.getSub());
    }

    @Test
    void toAccessToken_ledgersRolesPresentLowercase() {
        // When
        AccessTokenTO accessToken = mapper.toAccessToken(getAccessToken(Arrays.asList("customer", "KING", "CLOWN")));

        // Then
        assertEquals(UserRoleTO.CUSTOMER, accessToken.getRole());
        assertEquals(PREFERRED_USERNAME, accessToken.getLogin());
        assertEquals(SUBJECT, accessToken.getSub());
    }

    @Test
    void toAccessToken_noLedgersRoles() {
        // When
        AccessTokenTO accessToken = mapper.toAccessToken(getAccessToken(Arrays.asList("KING", "CLOWN")));

        // Then
        assertNull(accessToken.getRole());
        assertEquals(PREFERRED_USERNAME, accessToken.getLogin());
        assertEquals(SUBJECT, accessToken.getSub());
    }

    private AccessToken getAccessToken(List<String> roles) {
        AccessToken token = new AccessToken();

        AccessToken.Access access = new AccessToken.Access();
        roles.forEach(access::addRole);
        token.setRealmAccess(access);
        token.setSubject(SUBJECT);
        token.setPreferredUsername(PREFERRED_USERNAME);

        return token;
    }
}