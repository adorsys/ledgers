package de.adorsys.ledgers.deposit.db.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.adorsys.ledgers.deposit.db.EnableDepositAccountRepository;

@SpringBootApplication
@EnableDepositAccountRepository
public class DepositAccountRepositoryApplication {
	public static void main(String[] args) {
		SpringApplication.run(DepositAccountRepositoryApplication.class, args);
	}
}
