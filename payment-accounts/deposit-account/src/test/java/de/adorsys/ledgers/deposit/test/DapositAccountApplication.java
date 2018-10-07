package de.adorsys.ledgers.deposit.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.adorsys.ledgers.deposit.EnableDepositAccount;

@SpringBootApplication
@EnableDepositAccount
public class DapositAccountApplication {
	public static void main(String[] args) {
		SpringApplication.run(DapositAccountApplication.class, args);
	}
}
