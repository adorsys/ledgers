package de.adorsys.ledgers.postings.db;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
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
@ComponentScan(basePackageClasses= {PostingsBasePackage.class})
@EnableJpaAuditing
@EnableJpaRepositories
//@EntityScan(basePackageClasses= {BaseEntity.class, Jsr310JpaConverters.class})
@EntityScan(basePackageClasses= {BaseEntity.class})
public class PostingsRepositoryConfiguration {
}
