package de.adorsys.ledgers.keycloak.client.impl;

import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.keycloak.client.config.KeycloakClientConfig;
import de.adorsys.ledgers.keycloak.client.mapper.KeycloakDataMapper;
import de.adorsys.ledgers.keycloak.client.mapper.KeycloakDataMapperImpl;
import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;
import de.adorsys.ledgers.keycloak.client.rest.KeycloakTokenRestClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import pro.javatar.commons.reader.JsonReader;
import pro.javatar.commons.reader.ResourceReader;
import utils.KeycloakContainerTest;
import utils.TestConfiguration;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@ContextConfiguration(classes = {TestConfiguration.class, KeycloakDataMapperImpl.class},
        initializers = KeycloakDataServiceImplIT.Initializer.class)
public class KeycloakDataServiceImplIT extends KeycloakContainerTest {

    private static final String REALM = "test-realm";
    private static final String USERNAME = "babyuk";

    @Autowired
    private KeycloakDataMapper keycloakDataMapper;

    @Autowired
    private KeycloakClientConfig config;

    @Autowired
    private KeycloakTokenRestClient keycloakTokenRestClient;

    private KeycloakDataService keycloakDataService;
    private final ResourceReader jsonReader = JsonReader.getInstance();

    @Before
    public void setUp() {
        keycloakDataService = new KeycloakDataServiceImpl(getKeycloakClient(), keycloakDataMapper, config,
                                                          keycloakTokenRestClient);
        keycloakDataService.createDefaultSchema();
    }

    @AfterClass
    public static void afterClass() {
        keycloakContainer.stop();
    }

    @Test
    public void crudUser() throws IOException {
        //create user in keycloak
        KeycloakUser keycloakUser = jsonReader.getObjectFromFile("json/keycloak/create-user.json", KeycloakUser.class);
        keycloakDataService.createUser(keycloakUser);
        assertTrue(keycloakDataService.userExists(USERNAME));
        assertEquals("Eugen", keycloakUser.getFirstName());

        //update first name in keycloak
        KeycloakUser keycloakUserToUpdate = jsonReader.getObjectFromFile("json/keycloak/update-user.json", KeycloakUser.class);
        keycloakDataService.updateUser(keycloakUserToUpdate);

        //get user by login and check first name
        Optional<KeycloakUser> userOptional = keycloakDataService.getUser(REALM, USERNAME);
        assertTrue(userOptional.isPresent());
        assertEquals("Semen", userOptional.get().getFirstName());

        keycloakDataService.removeRealmRoleFromUser(USERNAME, Collections.singletonList("STAFF"));

        //delete user from keycloak
        keycloakDataService.deleteUser(USERNAME);
        assertFalse(keycloakDataService.userExists(USERNAME));
    }
}