package de.adorsys.ledgers.um.db;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import de.adorsys.ledgers.um.db.domain.UserEntity;

@Configuration
@ComponentScan(basePackageClasses= {UserManangmentBasePackage.class})
@EnableJpaAuditing
@EnableJpaRepositories
@EntityScan(basePackageClasses= {UserEntity.class, Jsr310JpaConverters.class})
public class UserManagementRepositoryConfiguration {
}
