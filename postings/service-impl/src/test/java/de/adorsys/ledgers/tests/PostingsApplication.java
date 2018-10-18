package de.adorsys.ledgers.tests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.adorsys.ledgers.postings.EnablePostings;

@SpringBootApplication
@EnablePostings
public class PostingsApplication {
	public static void main(String[] args) {
		SpringApplication.run(PostingsApplication.class, args);
	}
}
