/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package utils;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = {"de.adorsys.ledgers.keycloak.client"})
@PropertySource(value={"classpath:application.yml"})
public class TestConfiguration {
}
