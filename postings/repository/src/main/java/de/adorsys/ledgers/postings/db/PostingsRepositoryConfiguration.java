package de.adorsys.ledgers.postings.db;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuring what classes to scan while importing this module in 
 * a higer level module.
 * 
 * @author fpo
 *
 */
@Configuration
@ComponentScan(basePackages={"de.adorsys.ledgers.postings.db"})
@EnableJpaAuditing
@EnableJpaRepositories
@EntityScan(basePackages="de.adorsys.ledgers.postings.db.domain")
public class PostingsRepositoryConfiguration {

}
