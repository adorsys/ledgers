package de.adorsys.ledgers.middleware.rest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses= {LedgersMiddlewareRestBasePackage.class}, basePackages = "de.adorsys.ledgers.keycloak.client")
public class LedgersMiddlewareRestConfiguration {
}
