/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.adorsys.ledgers.sca.db.EnableSCARepository;

@Configuration
@ComponentScan(basePackageClasses=SCAServiceBasePackage.class)
@EnableSCARepository
public class SCAServiceConfiguration {}