package de.adorsys.ledgers.middleware;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses=MiddlewareServiceBasePackage.class)
public class MiddlewareServiceConfiguration {}