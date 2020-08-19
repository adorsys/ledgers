package impl;

import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.keycloak.client.rest.KeycloakTokenRestClient;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import utils.KeycloakContainerTest;
import utils.TestConfiguration;
import utils.TestKeycloakService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableFeignClients(basePackageClasses = {KeycloakTokenRestClient.class})
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@ContextConfiguration(classes = TestConfiguration.class, initializers = KeycloakContainerTest.Initializer.class)
class KeycloakTokenServiceImplIT extends KeycloakContainerTest {

    private static final String REALM = "test-realm";
    private static final String CLIENT_ID = "ledgers-client";
    private static final String CLIENT_SECRET = "279976b2-1794-437f-9467-8ad8401f1c51";
    private static final String USERNAME = "babyuk";
    private static final String USER_PASSWORD = "SDFDFGHFGHJHKJJL#@";

    @Autowired
    private KeycloakTokenService keycloakTokenService;

    @BeforeEach
    void setUp() {
        TestKeycloakService testKeycloakService = new TestKeycloakService(keycloakContainer);
        testKeycloakService.createRealm(REALM);
        testKeycloakService.createClient(REALM, CLIENT_ID, CLIENT_SECRET);
        testKeycloakService.createUser(REALM, USERNAME, USER_PASSWORD);

        ReflectionTestUtils.setField(keycloakTokenService, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(keycloakTokenService, "clientSecret", CLIENT_SECRET);
    }

    @AfterAll
    static void afterAll() {
        keycloakContainer.stop();
    }

    @Test
    void loginAndValidate() {
        BearerTokenTO bearerTokenTO = keycloakTokenService.login(USERNAME, USER_PASSWORD);
        assertNotNull(bearerTokenTO);
        assertNotNull(bearerTokenTO.getAccess_token());

        AccessTokenTO accessTokenObject = new AccessTokenTO();
        accessTokenObject.setLogin(USERNAME);
        bearerTokenTO.setAccessTokenObject(accessTokenObject);
        assertTrue(keycloakTokenService.validate(bearerTokenTO));
    }
}