package de.adorsys.ledgers.postings.db.tests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.adorsys.ledgers.postings.db.EnablePostingsReporitory;

@SpringBootApplication
@EnablePostingsReporitory
public class PostingsApplication {
	public static void main(String[] args) {
		SpringApplication.run(PostingsApplication.class, args);
	}
}
