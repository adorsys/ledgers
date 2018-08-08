package de.adorsys.ledgers.postings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PostingsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostingsApplication.class, args);
	}
}
