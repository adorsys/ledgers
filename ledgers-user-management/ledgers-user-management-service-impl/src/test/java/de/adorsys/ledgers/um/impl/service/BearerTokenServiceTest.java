package de.adorsys.ledgers.um.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import de.adorsys.ledgers.um.api.domain.AccessTokenBO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.TokenUsageBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.db.domain.UserRole;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BearerTokenServiceTest {
    private static final String TOKEN = "TOKEN";
    private static final String LOGIN = "login";
    private static final String USER_ID = "user id";
    private static final Date TODAY_00 = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    private static final Date TODAY_06 = Date.from(LocalDate.now().atStartOfDay().plusSeconds(600).atZone(ZoneId.systemDefault()).toInstant());
    private static final Date FIXED_DAY = Date.from(LocalDate.of(2020, 1, 1).atStartOfDay().minusSeconds(600).atZone(ZoneId.systemDefault()).toInstant());
    private static final String ROLE = "role";
    private static final String USAGE = "token_usage";
    @InjectMocks
    private BearerTokenService service;

    @Mock
    private HashMacSecretSource secretSource;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void bearerToken() {
        BearerTokenBO result = service.bearerToken(TOKEN, 600, new AccessTokenBO());
        assertThat(result).isEqualToComparingFieldByFieldRecursively(getExpectedBearer());
    }

    private BearerTokenBO getExpectedBearer() {
        BearerTokenBO token = new BearerTokenBO();
        token.setAccess_token(TOKEN);
        token.setExpires_in(600);
        token.setAccessTokenObject(new AccessTokenBO());
        return token;
    }

    @Test
    public void testBearerToken() {
        when(secretSource.getHmacSecret()).thenReturn("6VFX8YFQG5DLFKZIMNLGH9P406XR1SY5");
        when(objectMapper.convertValue(any(),any(Class.class))).thenReturn(new AccessTokenBO());
        BearerTokenBO result = service.bearerToken(USER_ID, LOGIN, Collections.emptyList(), null, UserRole.CUSTOMER, null, null, TODAY_00, TODAY_06, TokenUsageBO.LOGIN, null);
        assertThat(result).isEqualToIgnoringGivenFields(getExpectedBearer(),"access_token");
        assertThat(result.getAccess_token()).isNotEmpty();
    }

    @Test(expected = UserManagementModuleException.class)
    public void testBearerToken_signException() {
        when(secretSource.getHmacSecret()).thenReturn("");
        when(objectMapper.convertValue(any(),any(Class.class))).thenReturn(new AccessTokenBO());
        service.bearerToken(USER_ID, LOGIN, Collections.emptyList(), null, UserRole.CUSTOMER, null, null, TODAY_00, TODAY_06, TokenUsageBO.LOGIN, null);

    }

    @Test
    public void expiresIn() {
        int result = service.expiresIn(TODAY_00, getClaimSet(TODAY_06));
        assertThat(result).isEqualTo(600);
    }

    @Test
    public void expiresIn_no_exp_date() {
        int result = service.expiresIn(TODAY_00, getClaimSet(null));
        assertThat(result).isEqualTo(-1);
    }

    private JWTClaimsSet getClaimSet(Date expirationDate) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                                               .subject(Objects.requireNonNull(USER_ID, "Missing userId"))
                                               .jwtID(Ids.id())
                                               .issueTime(TODAY_00)
                                               .expirationTime(expirationDate)
                                               .claim(LOGIN, USER_ID)
                                               .claim(ROLE, "CUSTOMER")
                                               .claim(USAGE, "LOGIN");
        return builder.build();
    }

    @Test
    public void toAccessTokenObject() {
        Whitebox.setInternalState(service, "objectMapper", new ObjectMapper());
        AccessTokenBO result = service.toAccessTokenObject(getClaimSet(FIXED_DAY));
        assertThat(result).isEqualToIgnoringGivenFields(getExpectedAccessToken(), "jti","exp","iat");
        assertThat(result.getJti()).isNotEmpty();
        assertThat(result.getExp()).isEqualToIgnoringHours(getExpectedAccessToken().getExp());
        assertThat(result.getIat()).isEqualToIgnoringHours(getExpectedAccessToken().getIat());
    }

    private AccessTokenBO getExpectedAccessToken() {
        AccessTokenBO token = new AccessTokenBO();
        token.setSub(USER_ID);
        token.setLogin(USER_ID);
        token.setAccountAccesses(Collections.emptyList());
        token.setRole(UserRoleBO.CUSTOMER);
        token.setIat(Date.from(LocalDate.of(1970, 1, 19).atTime(11, 21).atZone(ZoneId.systemDefault()).toInstant()));
        token.setExp(Date.from(LocalDate.of(1970, 1, 19).atTime(9, 17, 9).atZone(ZoneId.systemDefault()).toInstant()));
        token.setAct(new HashMap<>());
        token.setTokenUsage(TokenUsageBO.LOGIN);
        return token;
    }
}
