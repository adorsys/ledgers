/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.db;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import de.adorsys.ledgers.um.db.domain.UserEntity;

@Configuration
@ComponentScan(basePackageClasses = {UserManagementBasePackage.class})
@EnableJpaRepositories
@EntityScan(basePackageClasses = {UserEntity.class, Jsr310JpaConverters.class})
public class UserManagementRepositoryConfiguration {
}
