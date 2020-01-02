package de.adorsys.ledgers.middleware.client;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses= {LedgersMiddlewareRestClientBasePackage.class})
public class LedgersMiddlewareRestClientConfiguration {
}
