package utils;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = {"de.adorsys.ledgers.keycloak.client"})
@PropertySource(value={"classpath:application.yml"})
public class TestConfiguration {
}
