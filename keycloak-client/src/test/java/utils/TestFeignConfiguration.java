package utils;

import de.adorsys.ledgers.keycloak.client.rest.KeycloakTokenRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackageClasses = {KeycloakTokenRestClient.class})
public class TestFeignConfiguration {
}
