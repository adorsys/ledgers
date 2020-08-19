package utils;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class KeycloakContainerTest {

    @Container
    public static final KeycloakContainer keycloakContainer = new KeycloakContainer("jboss/keycloak:11.0.0")
                                                                      .withAdminUsername("admin")
                                                                      .withAdminPassword("admin")
                                                                      .withEnv("DB_VENDOR", "h2");

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            if (!keycloakContainer.isRunning()) {
                keycloakContainer.start();
            }
            TestPropertyValues.of(
                    "keycloak.auth-server-url=" + keycloakContainer.getAuthServerUrl()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}
