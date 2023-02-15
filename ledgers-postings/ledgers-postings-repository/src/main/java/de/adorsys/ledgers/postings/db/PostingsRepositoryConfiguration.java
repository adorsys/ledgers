/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import de.adorsys.ledgers.postings.db.domain.BaseEntity;

/**
 * Configuring what classes to scan while importing this module in 
 * a higer level module.
 * 
 * @author fpo
 *
 */
@Configuration
@ComponentScan(basePackageClasses= {PostingsDBBasePackage.class})
@EnableJpaRepositories
@EntityScan(basePackageClasses= {BaseEntity.class})
public class PostingsRepositoryConfiguration {
}
