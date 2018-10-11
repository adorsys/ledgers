package de.adorsys.ledgers.postingserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.adorsys.ledgers.postings.EnablePostings;

@SpringBootApplication
@EnablePostings
public class PostingserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostingserverApplication.class, args);
	}
}
