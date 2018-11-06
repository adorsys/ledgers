package de.adorsys.ledgers.deposit.api.service.impl.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.adorsys.ledgers.deposit.api.service.EnableDepositAccount;
import de.adorsys.ledgers.postings.impl.EnablePostingService;

@SpringBootApplication
@EnableDepositAccount
@EnablePostingService
public class DepositAccountServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(DepositAccountServiceApplication.class, args);
	}
}
