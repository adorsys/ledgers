package de.adorsys.ledgers.postings;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages={"de.adorsys.ledgers.postings"})
@EnableJpaAuditing
@EnableJpaRepositories
@EntityScan(basePackages="de.adorsys.ledgers.postings.domain")
public class PostingsConfiguration {

}
