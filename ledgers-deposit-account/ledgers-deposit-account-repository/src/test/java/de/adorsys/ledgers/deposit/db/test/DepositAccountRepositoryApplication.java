/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.db.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import de.adorsys.ledgers.deposit.db.EnableDepositAccountRepository;

@SpringBootApplication
@EnableDepositAccountRepository
@EnableJpaAuditing
public class DepositAccountRepositoryApplication {
	public static void main(String[] args) {
		SpringApplication.run(DepositAccountRepositoryApplication.class, args);
	}
}
