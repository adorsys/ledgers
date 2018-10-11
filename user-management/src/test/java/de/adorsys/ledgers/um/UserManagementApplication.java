package de.adorsys.ledgers.um;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages={"de.adorsys.ledgers.um"})
@EnableJpaRepositories(basePackages = {"de.adorsys.ledgers.um.repository","de.adorsys.ledgers.postings.repository"})
@EnableJpaAuditing
@EntityScan(basePackages = {"de.adorsys.ledgers.um.domain","de.adorsys.ledgers.postings.domain"})
public class UserManagementApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserManagementApplication.class, args);
	}
}
