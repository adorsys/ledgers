package de.adorsys.ledgers.deposit.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.adorsys.ledgers.deposit.EnableDepositAccount;
import de.adorsys.ledgers.postings.EnablePostings;

@SpringBootApplication
@EnableDepositAccount
@EnablePostings
public class DepositAccountApplication {
	public static void main(String[] args) {
		SpringApplication.run(DepositAccountApplication.class, args);
	}
}
