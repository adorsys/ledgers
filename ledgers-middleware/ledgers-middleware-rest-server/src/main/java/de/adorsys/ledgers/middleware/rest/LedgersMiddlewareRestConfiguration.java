/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses= {LedgersMiddlewareRestBasePackage.class}, basePackages = "de.adorsys.ledgers.keycloak.client")
public class LedgersMiddlewareRestConfiguration {
}
