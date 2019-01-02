package de.adorsys.ledgers.mockbank.simple.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = MockbankSimpleBasePackage.class)
public class MockBankSimpleConfiguration {}