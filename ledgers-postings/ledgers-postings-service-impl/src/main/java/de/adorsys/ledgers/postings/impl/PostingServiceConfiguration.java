/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.adorsys.ledgers.postings.db.EnablePostingsReporitory;

@Configuration
@ComponentScan(basePackageClasses=PostingServiceBasePackage.class)
@EnablePostingsReporitory
public class PostingServiceConfiguration {}