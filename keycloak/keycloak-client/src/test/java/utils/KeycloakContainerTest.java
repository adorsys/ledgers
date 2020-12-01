package utils;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.jetbrains.annotations.NotNull;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SuppressWarnings("java:S2187")
@Testcontainers
public class KeycloakContainerTest {

    @Container
    public static final KeycloakContainer keycloakContainer = new KeycloakContainer("jboss/keycloak:11.0.0")
                                                                      .withAdminUsername("admin")
                                                                      .withAdminPassword("admin")
                                                                      .withEnv("DB_VENDOR", "h2");

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {
            if (!keycloakContainer.isRunning()) {
                keycloakContainer.start();
            }
            TestPropertyValues.of(
                    "keycloak.auth-server-url=" + keycloakContainer.getAuthServerUrl()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    public Keycloak getKeycloakClient() {
        return KeycloakBuilder.builder()
                       .serverUrl(keycloakContainer.getAuthServerUrl())
                       .realm("master")
                       .clientId("admin-cli")
                       .username(keycloakContainer.getAdminUsername())
                       .password(keycloakContainer.getAdminPassword())
                       .build();
    }
}
